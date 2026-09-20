# secure-api-hardening-showcase

A Spring Boot API that starts out deliberately insecure, then gets hardened step by step; with each fix documented like a real security audit. Built to demonstrate the same kind of dependency remediation, access-control, and hardening work done professionally on a large enterprise platform, applied here to a small, self-contained project anyone can clone and run.

*Status:* Step 1 of the hardening plan is complete; the vulnerable baseline. Authentication, rate limiting, security headers, and the dependency upgrade are upcoming steps (see [Roadmap](#roadmap) below).

## Problem

Production APIs regularly ship with two quiet risks that don't show up until something goes wrong:
- *Sensitive endpoints with no access control*: easy to add during development, easy to forget to lock down before shipping.
- *Outdated dependencies with known CVEs*: pulled in once, then never revisited, sitting on the classpath as a live attack surface.

This repo builds a small API with both problems on purpose, then fixes them one at a time, so each fix has a clear before/after.

## Approach

1. Stand up a minimal domain (a user directory) with one endpoint that's intentionally left open.
2. Pin a dependency to a version with a real, documented CVE.
3. Tag that state (v0-vulnerable) so the "before" is provable, not just described.
4. Run a vulnerability scan, fix each finding one at a time (auth, rate limiting, headers, dependency upgrade), and re-scan.
5. Write up every finding as Finding → Risk → Fix → Verified by, the way a real audit would.

## What I built

- User domain: JPA entity, Flyway migration (seeds an admin and two regular users), Spring Data repository.
- GET /api/users/{id} — looks up a single user by id.
- GET /admin/users — *the intentional vulnerability*: returns every user's name, email, and role, with no authentication or authorization check at all.
- commons-text pinned to version 1.9, affected by [CVE-2022-42889 ("Text4Shell")](https://nvd.nist.gov/vuln/detail/CVE-2022-42889) — used for real (a StringSubstitutor-based greeting in getUser), not just declared and left idle.

## Key decisions & tradeoffs

- *Chose Apache Commons Text over Log4j-core for the vulnerable dependency.* Log4j-core pinned to an old CVE-affected version was the original plan, but it conflicted with Spring Boot's own logging bridge (log4j-to-slf4j) already on the classpath, crashing the app before startup. Commons Text is a standalone utility library with no logging-framework entanglement, so it demonstrates the same "old dependency, real CVE" scenario without fighting the framework.
- *Used a real domain object (users) instead of a toy endpoint* so the "over-exposure" finding (/admin/users leaking emails/roles) reads as a realistic data-exposure risk, not a contrived example.
- *Made the vulnerable dependency load-bearing, not dead code.* commons-text's StringSubstitutor is actually called, so a vulnerability scanner and a reviewer both see genuine usage, not an unused jar that would never really ship.

## How to run it

Requires a local PostgreSQL instance.

````bash
# clone
git clone <repo-url>
cd secure-api-hardening-showcase

# configure your local Postgres connection in
# src/main/resources/application.properties

# run
./mvnw spring-boot:run        # macOS/Linux/Git Bash
mvnw.cmd spring-boot:run       # Windows cmd/PowerShell
````

Try it:
````bash
curl http://localhost:8080/api/users/1
curl http://localhost:8080/admin/users   # no auth required — this is the vulnerability
````

## Tests

Test coverage for the hardening fixes (red-before/green-after for auth, rate limiting, and headers) will be added alongside each fix as the roadmap progresses.

````bash
./mvnw test
````

## Tech stack

- Java 21, Spring Boot 4.1.1
- Spring Data JPA, Hibernate, Flyway
- PostgreSQL
- Spring Security, OAuth2 Resource Server (wired in upcoming steps)

## Roadmap

Confirmed by [OWASP Dependency-Check scan](docs/scan-before.html) against the v0-vulnerable baseline. Note: the scan only catches known-CVE dependency issues (the commons-text row); the access-control, rate-limiting, and header findings come from manual review, since no dependency scanner can detect a missing feature.

To reproduce the scan yourself:
bash
./mvnw org.owasp:dependency-check-maven:check 

First run downloads the NVD CVE feed and can take several minutes.

| Finding | CVSS | Risk | Fix | Status |
|---|---|---|---|---|
| Unauthenticated /admin/users | — | High — full user data exposure | OAuth2 resource server + role-based access control | Planned |
| commons-text 1.9 ([CVE-2022-42889](https://nvd.nist.gov/vuln/detail/CVE-2022-42889)) | 9.8 (Critical) | Script injection via StringSubstitutor | Upgrade to patched version | Planned |
| No rate limiting | — | Medium — brute force / DoS | Bucket4j token-bucket filter | Planned |
| Missing security headers | — | Medium — clickjacking / MIME sniffing | CSP, HSTS, X-Frame-Options via Spring Security headers DSL | Planned |

### Pre-existing framework CVEs (out of scope)

The same scan also flagged CVEs in tomcat-embed-core, hibernate-validator, and angus-activation: these are bundled transitively by Spring Boot 4.1.1's own starters (spring-boot-starter-webmvc, spring-boot-starter-validation), not dependencies pinned deliberately for this exercise. They're a useful real-world illustration that even a current, correctly-configured framework version can carry newly-disclosed vulnerabilities in its bundled libraries; which is why continuous scanning matters, not just a one-time check. Fixing these means bumping the Spring Boot parent version rather than a targeted dependency change, so they're tracked here but treated as out of scope for this repo's specific hardening exercise.
# Manually Testing the OAuth2 / Keycloak Flow

Verifies that /admin/users is genuinely locked down to the ADMIN role, using three terminals. Commands below are PowerShell (Windows); Git Bash/macOS/Linux equivalents are noted where they differ.

## Terminal 1 — Keycloak

````powershell
docker compose up -d
````
-d runs it in the background so you get your terminal back. Use docker compose down first if you've changed keycloak/realm-export.json and need a fresh re-import.

## Terminal 2 — Spring Boot app

Requires a local PostgreSQL instance running first (see main README).
````powershell
mvnw.cmd spring-boot:run        # Windows
./mvnw spring-boot:run           # Git Bash/macOS/Linux
````
Leave this running: it's the API on port 8080.

## Terminal 3 — Test commands

*1. Get a token for the admin user (alice-admin, has ADMIN + USER roles):*
````powershell
curl.exe -s -X POST http://localhost:8081/realms/secure-api-hardening/protocol/openid-connect/token -d "client_id=secure-api-hardening-client" -d "grant_type=password" -d "username=alice-admin" -d "password=adminpass123"
````
Copy the access_token value from the JSON response.

*2. Store it in a variable:*
````powershell
$TOKEN = "paste-the-access_token-value-here"
````

*3. Call the protected endpoint with the admin token — expect the user list (200 OK):*
powershell
curl.exe http://localhost:8080/admin/users -H "Authorization: Bearer $TOKEN"


*4. Call it with no token at all; expect 401 Unauthorized:*
````powershell
curl.exe -i http://localhost:8080/admin/users
````

*5. Get a token for the non-admin user (bob-user, has only USER):*
````powershell
curl.exe -s -X POST http://localhost:8081/realms/secure-api-hardening/protocol/openid-connect/token -d "client_id=secure-api-hardening-client" -d "grant_type=password" -d "username=bob-user" -d "password=userpass123"
````

*6. Repeat step 3's call using Bob's token; expect 403 Forbidden:*
````powershell
$TOKEN = "paste-bob's-access_token-here"
curl.exe -i http://localhost:8080/admin/users -H "Authorization: Bearer $TOKEN"
````
This is the important one: it proves role-based restriction is working, not just "any valid token gets in."

## Expected results summary

| Request | Expected result |
|---|---|
| /admin/users with alice-admin token (ADMIN) | 200 OK, returns user list |
| /admin/users with no token | 401 Unauthorized |
| /admin/users with bob-user token (USER only) | 403 Forbidden |

## Git Bash / macOS / Linux equivalent

Same commands work with curl instead of curl.exe, and ./mvnw instead of mvnw.cmd. Variable assignment differs:
````bash
TOKEN="paste-the-access_token-value-here"
curl http://localhost:8080/admin/users -H "Authorization: Bearer $TOKEN"
````

## Troubleshooting

If the token request fails with error="resolve_required_actions" / reason="Account is not fully set up", the imported user is missing required profile fields (firstName/lastName) or hasn't been marked emailVerified: true — check keycloak/realm-export.json, then docker compose down && docker compose up to force a fresh re-import.
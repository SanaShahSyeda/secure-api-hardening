package project.secure_api_hardening.user;

import org.apache.commons.text.StringSubstitutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/api/users/{id}")
    public User getUser(@PathVariable Long id) {
        log.info("Fetching user with id {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        String greeting = new StringSubstitutor(Map.of("name", user.getName()))
                .replace("Hello, ${name}!");
        log.info(greeting);
        return user;
    }

    // Baseline vulnerability (v0): returns every user's name/email/role with no
    // authentication or authorization check. Fixed in a later step with an
    // OAuth2 resource server + role-based access control.
    @GetMapping("/admin/users")
    public List<User> getAllUsers() {
        log.info("Fetching all users (admin listing)");
        return userRepository.findAll();
    }
}
package Coco.Products.backend.controller;

import java.time.Duration;
import java.util.Map;

import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import Coco.Products.backend.entity.Customer;
import Coco.Products.backend.repository.CustomerRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final CustomerRepository customerRepository;

    private final PasswordEncoder passwordEncoder;

    public AuthController(
            CustomerRepository customerRepository,
            PasswordEncoder passwordEncoder) {

        this.customerRepository =
                customerRepository;

        this.passwordEncoder =
                passwordEncoder;
    }

    // ==========================================
    // REGISTER
    // ==========================================

    @PostMapping("/register")
    public ResponseEntity<?> register(
            @RequestBody Map<String, String> request) {

        String name =
                request.get("name");

        String email =
                request.get("email");

        String password =
                request.get("password");

        // Check fields
        if (name == null ||
                email == null ||
                password == null ||
                name.isBlank() ||
                email.isBlank() ||
                password.isBlank()) {

            return ResponseEntity.badRequest()
                    .body(
                        Map.of(
                            "message",
                            "All fields are required"
                        )
                    );
        }

        // Check existing email
        if (customerRepository
                .findByEmail(email)
                .isPresent()) {

            return ResponseEntity.badRequest()
                    .body(
                        Map.of(
                            "message",
                            "Email already registered"
                        )
                    );
        }

        // Encrypt password
        String encryptedPassword =
                passwordEncoder.encode(password);

        Customer customer =
                new Customer(
                        name,
                        email,
                        encryptedPassword
                );

        customerRepository.save(customer);

        return ResponseEntity.ok(
                Map.of(
                    "message",
                    "Registration successful"
                )
        );
    }

    // ==========================================
    // LOGIN
    // ==========================================

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody Map<String, String> request,
            HttpServletRequest httpRequest) {

        String email =
                request.get("email");

        String password =
                request.get("password");

        if (email == null ||
                password == null) {

            return ResponseEntity.badRequest()
                    .body(
                        Map.of(
                            "message",
                            "Email and password are required"
                        )
                    );
        }

        // Find customer
        Customer customer =
                customerRepository
                        .findByEmail(email)
                        .orElse(null);

        if (customer == null) {

            return ResponseEntity
                    .status(401)
                    .body(
                        Map.of(
                            "message",
                            "Invalid email or password"
                        )
                    );
        }

        // Check password
        boolean passwordMatches =
                passwordEncoder.matches(
                        password,
                        customer.getPassword()
                );

        if (!passwordMatches) {

            return ResponseEntity
                    .status(401)
                    .body(
                        Map.of(
                            "message",
                            "Invalid email or password"
                        )
                    );
        }

        // ======================================
        // CREATE SERVER-SIDE HTTP SESSION
        // ======================================

        HttpSession session =
                httpRequest.getSession(true);

        // Rotate the identifier after authentication to prevent session fixation.
        httpRequest.changeSessionId();

        // Store customer information
        session.setAttribute(
                "customerId",
                customer.getId()
        );

        session.setAttribute(
                "customerName",
                customer.getName()
        );

        session.setAttribute(
                "customerEmail",
                customer.getEmail()
        );

        return ResponseEntity.ok(
                Map.of(
                    "message",
                    "Login successful",

                    "name",
                    customer.getName()
                )
        );
    }

    // ==========================================
    // CURRENT USER
    // ==========================================

    @GetMapping("/me")
    public ResponseEntity<?> currentCustomer(
            HttpServletRequest request) {

        // false means:
        // DON'T create a new session
        // if the user doesn't already have one.

        HttpSession session =
                request.getSession(false);

        // Check session
        if (session == null ||
                session.getAttribute(
                        "customerId"
                ) == null) {

            return ResponseEntity
                    .status(401)
                    .body(
                        Map.of(
                            "message",
                            "Not logged in"
                        )
                    );
        }

        return ResponseEntity.ok(
                Map.of(
                    "customerId",
                    session.getAttribute(
                            "customerId"
                    ),

                    "name",
                    session.getAttribute(
                            "customerName"
                    ),

                    "email",
                    session.getAttribute(
                            "customerEmail"
                    )
                )
        );
    }

    // ==========================================
    // LOGOUT
    // ==========================================

    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            HttpServletRequest request,
            jakarta.servlet.http.HttpServletResponse response) {

        // Get existing session without creating a new one.
        HttpSession session =
                request.getSession(false);

        // Destroy the server-side session so the identifier cannot be reused.
        if (session != null) {
            session.invalidate();
        }

        // Clear the browser's JSESSIONID cookie after destroying its session.
        clearSessionCookie(request, response);

        return ResponseEntity.ok(
                Map.of(
                    "message",
                    "Logout successful"
                )
        );
    }

        private void clearSessionCookie(
                        HttpServletRequest request,
                        jakarta.servlet.http.HttpServletResponse response) {

                ResponseCookie clearedCookie = ResponseCookie.from(
                                                "JSESSIONID",
                                                "")
                                .httpOnly(true)
                                .secure(request.isSecure())
                                .sameSite("Lax")
                                .path("/")
                                .maxAge(Duration.ZERO)
                                .build();

                response.addHeader("Set-Cookie", clearedCookie.toString());
        }
}
package org.application.bookstorage.controller.login;


import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.application.bookstorage.dao.Administrator;
import org.application.bookstorage.repository.AdministratorRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class LoginController {

    private final AdministratorRepository administratorRepository;
    private final PasswordEncoder passwordEncoder;
    // секретный ключ для подписи JWT (в продакшене храните в надежном месте)
    private final String jwtSecret = "mySecretKeyForJWTSignaturesjhgfdsdfghjklkjhgfdsadfghjkljhgfdsasdfghjklkjhgfdsaASDFGHJKJHGFDSAasdfghjkjhgfdsaASDFGHJKJHGFDSAasdfghjkhgfdsaASDFGJHGFDSDFGHJKJHGFDSAasdfghjkhgfdsaASDFGHJKJHGFDSAasdfghjkjhgfdsaASDFGHJHGFDSAasdfghjgfdsaASDFGHGFDSAasdfghgfdsaASDFGHJKJHGFDSASDFGHJKJHGFDSADFGHJKJHGFDSASDFGHNJGFDSasdfghjkjhgfdsadfghjklkjhgfdsfghjm";
    // время жизни токена (например, 1 час)
    private final long jwtExpirationMs = 3600000;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest authRequest) {

        Optional<Administrator> adminOpt = administratorRepository.findByLogin(authRequest.getUsername());
        if (adminOpt.isPresent()) {
            Administrator admin = adminOpt.get();
            // Если пароль храним не в открытом виде – используйте метод passwordEncoder.matches()
            if (authRequest.getPassword().equals(admin.getPassword())) {
                String token = Jwts.builder()
                        .setSubject(admin.getLogin())
                        .setIssuedAt(new Date())
                        .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
                        .signWith(SignatureAlgorithm.HS512, jwtSecret)
                        .compact();

                return ResponseEntity.ok(new AuthResponse(token));
            }
        }
        return ResponseEntity.status(401).body("Неверный логин или пароль");
    }

    // DTO классы для запроса и ответа
    public static class AuthRequest {
        private String username;
        private String password;
        // геттеры/сеттеры
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class AuthResponse {
        private String token;
        public AuthResponse(String token) { this.token = token; }
        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }
    }
}
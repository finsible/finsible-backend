package org.finsible.backend.authentication.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.finsible.backend.BaseResponse;
import org.finsible.backend.authentication.entity.UserData;
import org.finsible.backend.authentication.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/googleSignIn")
    public ResponseEntity<BaseResponse<UserData>> googleSignIn(@RequestParam String token, @RequestParam String clientId, @RequestParam String deviceType, HttpServletResponse response) {
        return authService.googleSignIn(token, clientId, deviceType, response);
    }

    @PostMapping("/googleSignInWithCode")
    public ResponseEntity<BaseResponse<UserData>> googleSignInWithCode(@RequestParam String code, @RequestParam String clientId, @RequestParam String deviceType, HttpServletResponse response) {
        return authService.googleSignInWithCode(code, clientId, deviceType, response);
    }
}

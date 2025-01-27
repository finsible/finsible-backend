package org.finsible.backend.authentication.controller;

import org.finsible.backend.BaseResponse;
import org.finsible.backend.authentication.entity.UserData;
import org.finsible.backend.authentication.service.AuthService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }
    @PostMapping("/googleSignIn")
    public BaseResponse<UserData> googleSignIn(@RequestParam String token, @RequestParam String clientId) {
        return authService.googleSignIn(token, clientId);
    }
}

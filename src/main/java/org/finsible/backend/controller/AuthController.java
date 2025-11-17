package org.finsible.backend.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.finsible.backend.AppConstants;
import org.finsible.backend.BaseResponse;
import org.finsible.backend.dto.request.UserAuthenticationRequestDTO;
import org.finsible.backend.dto.request.groups.AuthWithCode;
import org.finsible.backend.dto.request.groups.AuthWithToken;
import org.finsible.backend.dto.response.UserResponseDTO;
import org.finsible.backend.service.AuthService;
import org.finsible.backend.utility.CookieHandler;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.GeneralSecurityException;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;
    private final CookieHandler cookieHandler;

    public AuthController(AuthService authService, CookieHandler cookieHandler) {
        this.cookieHandler = cookieHandler;
        this.authService = authService;
    }

    @PostMapping("/sign-in/google")
    public ResponseEntity<BaseResponse<UserResponseDTO>> googleSignIn(@Validated(AuthWithToken.class) @RequestBody UserAuthenticationRequestDTO authRequest, @RequestParam String deviceType, HttpServletResponse httpServletResponse)
            throws GeneralSecurityException, IOException {
        UserResponseDTO userResponseDTO = authService.googleSignIn(authRequest, deviceType);
        return responseWithAuthCookies(deviceType, httpServletResponse, userResponseDTO);
    }

    @PostMapping("/sign-in/google-code")
    public ResponseEntity<BaseResponse<UserResponseDTO>> googleSignInWithCode(@Validated(AuthWithCode.class) @RequestBody UserAuthenticationRequestDTO authRequest, @RequestParam String deviceType,
                                                                              HttpServletResponse httpServletResponse) throws GeneralSecurityException, IOException {
        UserResponseDTO userResponseDTO = authService.googleSignInWithCode(authRequest, deviceType);
        return responseWithAuthCookies(deviceType, httpServletResponse, userResponseDTO);
    }

    @NotNull
    private ResponseEntity<BaseResponse<UserResponseDTO>> responseWithAuthCookies(@RequestParam String deviceType, HttpServletResponse httpServletResponse, UserResponseDTO userResponseDTO) {
        if(deviceType.equals(AppConstants.DEVICE_WEB)) {
            cookieHandler.setAuthenticationCookies(httpServletResponse, userResponseDTO.getJwt());
        }
        if(!deviceType.equals(AppConstants.DEVICE_MOBILE)) {
            userResponseDTO.setJwt(null); // Clear JWT from response body for clients other than mobile
        }

        return ResponseEntity.ok(new BaseResponse<>(AppConstants.LOGIN_SUCCESS_MESSAGE, true, userResponseDTO));
    }

    @GetMapping("/me")
    public ResponseEntity<BaseResponse<UserResponseDTO>> getUser(@RequestAttribute String userId, @RequestParam String deviceType) {
        UserResponseDTO userResponseDTO = authService.getUser(userId);
        return ResponseEntity.ok(new BaseResponse<>(AppConstants.DATA_FETCH_SUCCESS, true, userResponseDTO));
    }

    @PostMapping("/sign-out")
    public ResponseEntity<BaseResponse<String>> logout(@RequestAttribute String userId, @RequestParam String deviceType, HttpServletResponse httpServletResponse) {
        // Clear cookies on logout
        if(deviceType.equals(AppConstants.DEVICE_WEB)) {
            cookieHandler.clearAuthenticationCookies(httpServletResponse);
        }
        authService.signOut(userId);
        return ResponseEntity.ok(new BaseResponse<>(AppConstants.LOGOUT_SUCCESS_MESSAGE, true));
    }
}

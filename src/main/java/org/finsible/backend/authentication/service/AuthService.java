package org.finsible.backend.authentication.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.api.client.googleapis.auth.oauth2.*;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.finsible.backend.AppConstants;
import org.finsible.backend.BaseResponse;
import org.finsible.backend.ErrorDetails;
import org.finsible.backend.authentication.entity.Account;
import org.finsible.backend.authentication.entity.AccountGroup;
import org.finsible.backend.authentication.entity.User;
import org.finsible.backend.authentication.entity.UserData;
import org.finsible.backend.authentication.repository.AccountGroupRepository;
import org.finsible.backend.authentication.repository.AccountRepository;
import org.finsible.backend.authentication.repository.UserRepository;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Collections;

@Service
public class AuthService {
    private UserRepository userRepository;
    private AccountRepository accountRepository;
    private AccountGroupRepository accountGroupRepository;
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    private static final String CLIENT_SECRET = System.getenv("REACT_WEB_APP_GOOGLE_CLIENT_SECRET");

    @Autowired
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    @Autowired
    public void setAccountRepository(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }
    @Autowired
    public void setAccountGroupRepository(AccountGroupRepository accountGroupRepository) {
        this.accountGroupRepository = accountGroupRepository;
    }

    public String authenticateAndGenerateToken(String userId) {
        return JwtService.generateToken(userId);
    }

    public ResponseEntity<BaseResponse<UserData>> googleSignIn(String token, String clientId, String deviceType, HttpServletResponse httpServletResponse) {
        BaseResponse<UserData> response;
        try {
            JsonFactory jsonFactory = GsonFactory.getDefaultInstance();
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier
                    .Builder(GoogleNetHttpTransport.newTrustedTransport(), jsonFactory)
                    // Specify the CLIENT_ID of the app that accesses the backend:
                    .setAudience(Collections.singletonList(clientId))
                    // Or, if multiple clients access the backend:
                    //.setAudience(Arrays.asList(CLIENT_ID_1, CLIENT_ID_2, CLIENT_ID_3))
                    .build();

            GoogleIdToken idToken = verifier.verify(token);
            if (idToken != null) {
                GoogleIdToken.Payload payload = idToken.getPayload();

                String userId = payload.getSubject();
                String email = payload.getEmail(); //email and name is null for some user
                String name = (String) payload.get("name");
                String picture = (String) payload.get("picture");
                String lastLoggedIn = LocalDateTime.now().toString();
                User user = userRepository.findById(userId).orElse(null);
                boolean isNewUser = user == null;

                // if user is null then create a new user and define the isNewUser and accountCreated as true
                if (isNewUser) {
                    user = new User(email, name, picture, userId, lastLoggedIn, lastLoggedIn);
                    userRepository.save(user);
                    // create cash account under cash account group
                    ObjectMapper objectMapper = new ObjectMapper();
                    ObjectNode jsonNode = objectMapper.createObjectNode();

                    jsonNode.put("Amount", 0);
                    AccountGroup cash = accountGroupRepository.findAccountGroupById(1L);
                    Account cashAccount = new Account(null, user, cash, "Cash", "Cash", jsonNode);
                    accountRepository.save(cashAccount);
                } else {
                    // if user is not null then update the lastLoggedIn time and set isNewUser and accountCreated as false and update in the database
                    user.setLastLoggedIn(lastLoggedIn);
                    user.setName(name);
                    user.setPicture(picture);
                    userRepository.save(user);
                }
                String jwt = authenticateAndGenerateToken(user.getId());
                UserData userData = new UserData(isNewUser, user.getId(), user.getEmail(), user.getName(), user.getPicture(), user.getAccountCreated(), user.getLastLoggedIn(), null);
                if(deviceType.equals(AppConstants.DEVICE_MOBILE)) {
                    userData.setJwt(jwt);
                }
                if(deviceType.equals(AppConstants.DEVICE_WEB)) {
                    Cookie jwtCookie = getJwtCookie(jwt, 15552000); // 180 days in seconds
                    Cookie authStatusCookie = getAuthCookie("true");
                    httpServletResponse.addCookie(jwtCookie);
                    httpServletResponse.addCookie(authStatusCookie);
                }
                response = new BaseResponse<>(AppConstants.LOGIN_SUCCESS_MESSAGE,true, userData);
                logger.info("User logged in successfully with email: {}", email);
                return ResponseEntity.ok(response);
            }
            response = new BaseResponse<>("Invalid ID token.", false);
            logger.error("Invalid ID token.");
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            return errorHandler(e, deviceType, httpServletResponse, new BaseResponse<>(AppConstants.RESPONSE_ERROR_MESSAGE, false));
        }
    }

    public ResponseEntity<BaseResponse<UserData>> googleSignInWithCode(String code, String clientId, String deviceType, HttpServletResponse httpServletResponse) {
        try{
            GoogleTokenResponse tokenResponse = new GoogleAuthorizationCodeTokenRequest(
                    new NetHttpTransport(),
                    GsonFactory.getDefaultInstance(),
                    clientId,
                    CLIENT_SECRET,
                    code,
                    AppConstants.REDIRECT_URI // Redirect URI
            ).execute();

            String idToken = tokenResponse.getIdToken();
            // since we already have the function which fetched user data from the id token, we use it
            return googleSignIn(idToken, clientId, deviceType, httpServletResponse);
        }
        catch(Exception e){
            return errorHandler(e, deviceType, httpServletResponse, new BaseResponse<>(AppConstants.RESPONSE_ERROR_MESSAGE, false));
        }
    }

    private ResponseEntity<BaseResponse<UserData>> errorHandler(Exception e, String deviceType, HttpServletResponse httpServletResponse, BaseResponse<UserData> response) {
        logger.error("Error while logging in: {}", e.getMessage());
        if(deviceType.equals(AppConstants.DEVICE_WEB)) {
            // Clear cookies on error
            httpServletResponse.addCookie(getJwtCookie("", 0)); // Expire JWT cookie
            httpServletResponse.addCookie(getAuthCookie("false"));
        }
        ErrorDetails errorDetails=new ErrorDetails(AppConstants.INTERNAL_SERVER_ERROR, e.getMessage(), e.getCause() != null ? e.getCause().toString() : "");
        logger.error("Error details: {}", errorDetails);
        response.setErrorDetails(errorDetails);
        return ResponseEntity.internalServerError().body(response);
    }

    public ResponseEntity<BaseResponse<UserData>> getUser(String userId, String deviceType, HttpServletResponse httpServletResponse) {
        BaseResponse<UserData> response;
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                response = new BaseResponse<>(AppConstants.USER_NOT_FOUND, false);
                return ResponseEntity.status(AppConstants.UNAUTHORIZED_REQUEST).body(response);
            }
            UserData userData = new UserData(false, user.getId(), user.getEmail(), user.getName(), user.getPicture(), user.getAccountCreated(), user.getLastLoggedIn(), null);
            response = new BaseResponse<>("Data fetched successfully", true, userData);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return errorHandler(e, deviceType, httpServletResponse, new BaseResponse<>(AppConstants.RESPONSE_ERROR_MESSAGE, false));
        }
    }

    public ResponseEntity<BaseResponse<UserData>> logout(String userId, String deviceType, HttpServletResponse httpServletResponse) {
        BaseResponse<UserData> response;
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                response = new BaseResponse<>(AppConstants.USER_NOT_FOUND, false);
                return ResponseEntity.status(AppConstants.UNAUTHORIZED_REQUEST).body(response);
            }
            // Clear cookies on logout
            if(deviceType.equals(AppConstants.DEVICE_WEB)) {
                httpServletResponse.addCookie(getJwtCookie("", 0)); // Expire JWT cookie
                httpServletResponse.addCookie(getAuthCookie("false"));
            }
            response = new BaseResponse<>("Logged out successfully", true);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return errorHandler(e, deviceType, httpServletResponse, new BaseResponse<>(AppConstants.RESPONSE_ERROR_MESSAGE, false));
        }
    }

    @NotNull // method returns a non-null Cookie object always
    private static Cookie getJwtCookie(String jwtToken, int expiry) {
        Cookie jwtCookie = new Cookie("jwt_token", jwtToken);

        // HTTP-only prevents JavaScript access - critical for XSS protection
        jwtCookie.setHttpOnly(true);

        // Secure flag ensures cookie only sent over HTTPS
        //jwtCookie.setSecure(true);

        // SameSite=Strict prevents CSRF attacks by ensuring the cookie
        // is only sent with requests from the same site
        //jwtCookie.setAttribute("SameSite", "Strict");

        // Set appropriate expiration - match your JWT expiration
        jwtCookie.setMaxAge(expiry);

        // Set path to root so it's available for all endpoints
        jwtCookie.setPath("/");

        return jwtCookie;
    }

    @NotNull
    private static Cookie getAuthCookie(String isAuthenticated) {
        // Optional: Also set a separate flag cookie that JavaScript can read
        // This allows your frontend to know authentication status without
        // accessing the actual token
        Cookie authStatusCookie = new Cookie("is_authenticated", isAuthenticated);
        authStatusCookie.setHttpOnly(false); // JavaScript can read this one
        // authStatusCookie.setSecure(true);
        //authStatusCookie.setAttribute("SameSite", "Strict");
        authStatusCookie.setMaxAge(15552000); // 180 days in seconds
        authStatusCookie.setPath("/");

        return authStatusCookie;
    }
}

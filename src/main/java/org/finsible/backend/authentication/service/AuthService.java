package org.finsible.backend.authentication.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import org.finsible.backend.BaseResponse;
import org.finsible.backend.authentication.entity.User;
import org.finsible.backend.authentication.entity.UserData;
import org.finsible.backend.authentication.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;

@Service
public class AuthService {

    private UserRepository userRepository;
    BaseResponse<UserData> response;
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public String authenticateAndGenerateToken(String userId) {
        return JwtService.generateToken(userId);
    }

    public BaseResponse<UserData> googleSignIn(String token, String clientId) {
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
                String email = payload.getEmail();
                String name = (String) payload.get("name");
                String picture = (String) payload.get("picture");
                String lastLoggedIn = LocalDateTime.now().toString();
                boolean categoriesEdited = false; //handle later

                User user = userRepository.findById(userId).orElse(null);
                boolean isNewUser = user == null;
                int status;
                // if user is null then create a new user and define the isNewUser and accountCreated as true
                if (user == null) {
                    status=201;  // for created
                    user = new User(email, name, picture, userId, lastLoggedIn, categoriesEdited, lastLoggedIn);
                    userRepository.save(user);
                } else {
                    // if user is not null then update the lastLoggedIn time and set isNewUser and accountCreated as false and update in the database
                    user.setLastLoggedIn(lastLoggedIn);
                    user.setName(name);
                    user.setPicture(picture);
                    status=200;
                    // update user in database
                    userRepository.save(user);
                }
                String jwt = authenticateAndGenerateToken(user.getId());
                UserData userData = new UserData(isNewUser, user.getId(), user.getEmail(), user.getName(), user.getPicture(), user.getAccountCreated(), user.getLastLoggedIn(), jwt);
                response = new BaseResponse<>("You are successfully logged in.",true, status ,userData);
                logger.info("User logged in successfully with email: {}", email);
                return response;
            }
            response = new BaseResponse<>("Invalid ID token.", false, 400);
            logger.error("Invalid ID token.");
            return response;

        } catch (Exception e) {
            response = new BaseResponse<>(e.getMessage(), false, 400);
            logger.error("Error while logging in: {}", e.getMessage());
            return response;
        }
    }
}

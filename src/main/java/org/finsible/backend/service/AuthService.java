package org.finsible.backend.service;

import com.google.api.client.googleapis.auth.oauth2.*;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import org.finsible.backend.AppConstants;
import org.finsible.backend.CustomExceptionHandler.InvalidTokenException;
import org.finsible.backend.CustomExceptionHandler.UserNotFoundException;
import org.finsible.backend.dto.request.UserAuthenticationRequestDTO;
import org.finsible.backend.entity.*;
import org.finsible.backend.dto.response.UserResponseDTO;
import org.finsible.backend.mapper.UserMapper;
import org.finsible.backend.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.math.BigDecimal;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final AccountGroupRepository accountGroupRepository;
    private final AdminRepository adminRepository;
    private final LanguageService languageService;
    private final CurrencyService currencyService;
    private final UserMapper userMapper;
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    private final String CLIENT_SECRET;

    public AuthService(UserRepository userRepository, AccountRepository accountRepository, AccountGroupRepository accountGroupRepository,
                       AdminRepository adminRepository, UserMapper userMapper, LanguageService languageService,
                       CurrencyService currencyService, @Value("${app.google.client.secret}") String clientSecret) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.accountGroupRepository = accountGroupRepository;
        this.adminRepository = adminRepository;
        this.userMapper = userMapper;
        this.languageService = languageService;
        this.currencyService = currencyService;
        this.CLIENT_SECRET = clientSecret;
    }

    public String authenticateAndGenerateToken(String userId, List<String> roles) {
        return JwtService.generateToken(userId, roles);
    }

    public UserResponseDTO googleSignIn(UserAuthenticationRequestDTO authRequest, String deviceType) throws GeneralSecurityException, IOException {
        JsonFactory jsonFactory = GsonFactory.getDefaultInstance();
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier
                .Builder(GoogleNetHttpTransport.newTrustedTransport(), jsonFactory)
                // Specify the CLIENT_ID of the app that accesses the backend:
                .setAudience(Collections.singletonList(authRequest.getClientId()))
                // Or, if multiple clients access the backend:
                //.setAudience(Arrays.asList(CLIENT_ID_1, CLIENT_ID_2, CLIENT_ID_3))
                .build();

        GoogleIdToken idToken = verifier.verify(authRequest.getToken());

        if(idToken == null){
            logger.error("Invalid ID token.");
            throw new InvalidTokenException("Invalid ID token.");
        }
        GoogleIdToken.Payload payload = idToken.getPayload();

        String userId = payload.getSubject();
        String email = payload.getEmail(); //email and name is null for some user
        String name = (String) payload.get("name");
        String picture = (String) payload.get("picture");
        User user = userRepository.findById(userId).orElse(null);
        boolean isNewUser = user == null;

        // fetch the language and currency
        SupportedLanguage defaultLanguage = languageService.getLanguageOrDefault(authRequest.getDefaultLanguageCode());

        // fetch the currency
        SupportedCurrency defaultCurrency = currencyService.getCurrencyOrDefault(authRequest.getDefaultCurrencyCode());

        // if user is null then create a new user and define the isNewUser and accountCreated as true
        if (isNewUser) {
            user = User.builder().email(email).name(name).picture(picture).id(userId)
                    .accountCreated(Instant.now()).lastLoggedIn(Instant.now())
                    .defaultLanguage(defaultLanguage).defaultCurrency(defaultCurrency).build();

            userRepository.save(user);
            // create cash account under cash account group
            AccountGroup cash = accountGroupRepository.findAccountGroupById(AppConstants.CASH_ACCOUNT_GROUP_ID);
            Account cashAccount = Account.builder()
                    .name("Cash")
                    .description("Cash account")
                    .balance(new BigDecimal("0.00"))
                    .accountGroup(cash)
                    .user(user)
                    .currency(user.getDefaultCurrency())
                    .isSystemDefault(true)
                    .isActive(true)
                    .build();
            accountRepository.save(cashAccount);
        } else {
            user.setName(name);
            user.setPicture(picture);
            user.setDefaultCurrency(defaultCurrency);
            user.setDefaultLanguage(defaultLanguage);
            userRepository.save(user);
        }
        // check in admin table if the user is an admin
        boolean isAdmin = adminRepository.findByEmail(email) != null;
        List<String> roles = new ArrayList<>(Collections.singleton("USER"));
        if(isAdmin){
            roles.add("ADMIN");
        }
        String jwt = authenticateAndGenerateToken(user.getId(), roles);
        UserResponseDTO userResponseDTO = userMapper.toUserResponseDTO(user, isNewUser);
        userResponseDTO.setJwt(jwt);
        logger.info("User logged in successfully with email: {}", email);
        return userResponseDTO;
    }

    public UserResponseDTO googleSignInWithCode(UserAuthenticationRequestDTO authRequest, String deviceType) throws IOException, GeneralSecurityException {
        GoogleTokenResponse tokenResponse = new GoogleAuthorizationCodeTokenRequest(
                new NetHttpTransport(),
                GsonFactory.getDefaultInstance(),
                authRequest.getClientId(),
                CLIENT_SECRET,
                authRequest.getCode(),
                AppConstants.REDIRECT_URI // Redirect URI
        ).execute();

        String idToken = tokenResponse.getIdToken();
        authRequest.setToken(idToken);
        // since we already have the function which fetched user data from the id token, we use it
        return googleSignIn(authRequest, deviceType);

    }

    public UserResponseDTO getUser(String userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            throw new UserNotFoundException(AppConstants.USER_NOT_FOUND);
        }
        return userMapper.toUserResponseDTO(user, false);
    }

    public void signOut(String userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            throw new UserNotFoundException(AppConstants.USER_NOT_FOUND);
        }
    }
}

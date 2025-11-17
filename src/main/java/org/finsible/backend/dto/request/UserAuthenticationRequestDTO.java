package org.finsible.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.finsible.backend.dto.request.groups.AuthWithCode;
import org.finsible.backend.dto.request.groups.AuthWithToken;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserAuthenticationRequestDTO {
    @NotBlank(message = "Authentication Token is required", groups = AuthWithToken.class)
    private String token;

    @NotBlank(message = "Authentication code is required", groups = AuthWithCode.class)
    private String code;

    @NotBlank(message = "Client ID is required", groups = {AuthWithCode.class, AuthWithToken.class})
    private String clientId;

    @NotBlank(message = "Default currency is required", groups = {AuthWithCode.class, AuthWithToken.class})
    private String defaultCurrencyCode;

    @NotBlank(message = "Default language is required", groups = {AuthWithCode.class, AuthWithToken.class})
    private String defaultLanguageCode;
}

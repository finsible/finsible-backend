package org.finsible.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserResponseDTO {
    @JsonProperty("isNewUser")  //  lombok's getter will mapped as getNewUser() instead of getIsNewUser() and due to that in response Json we were seeing newUser
    // we can use @Accessors(fluent=true) instead of @JsonProperty("isNewUser") to get the same result : https://www.baeldung.com/lombok-accessors
    private boolean isNewUser;
    private String userId;
    private String email;
    private String name;
    private String picture;
    private Instant accountCreated;
    private Instant lastLoggedIn;
    private String jwt;
}

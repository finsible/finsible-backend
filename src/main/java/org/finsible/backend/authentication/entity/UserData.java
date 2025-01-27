package org.finsible.backend.authentication.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserData {
    @JsonProperty("isNewUser")  //  lombok's getter will mapped as getNewUser() instead of getIsNewUser() and due to that in response Json we were seeing newUser
    // we can use @Accessors(fluent=true) instead of @JsonProperty("isNewUser") to get the same result : https://www.baeldung.com/lombok-accessors
    private boolean isNewUser;
    private String userId;
    private String email;
    private String name;
    private String picture;
    private String accountCreated;
    private String lastLoggedIn;
    private String jwt;

    public UserData(boolean isNewUser, String userId, String email, String name, String picture, String accountCreated, String lastLoggedIn, String jwt) {
        this.isNewUser = isNewUser;
        this.userId = userId;
        this.email = email;
        this.name = name;
        this.picture = picture;
        this.accountCreated = accountCreated;
        this.lastLoggedIn = lastLoggedIn;
        this.jwt = jwt;
    }
}

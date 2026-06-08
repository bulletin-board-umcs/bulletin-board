package pl.umcs.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public record User(String username, String password, boolean admin) implements Serializable {
    public User() {
        this(null, null, false);
    }

    @JsonCreator
    public User(
            @JsonProperty("username") String username,
            @JsonProperty("password") String password,
            @JsonProperty("admin") boolean admin
    ) {
        this.username = username;
        this.password = password;
        this.admin = admin;
    }

    public static User create(String username, String password, boolean admin) {
        return new User(username, password, admin);
    }
}

package pl.umcs.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

public class User implements Serializable {
    private final String username;
    private final String password;

    private final boolean admin;

    public User() {
        this.username = null;
        this.password = null;
        this.admin = false;
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

    public static Comment create(String authorUsername, String content) {
        return new Comment(
                UUID.randomUUID(),
                authorUsername,
                content,
                LocalDateTime.now()
        );
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public boolean isAdmin() {
        return admin;
    }
}

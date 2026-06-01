package pl.umcs.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

public class Comment implements Serializable {
    private final UUID id;
    private final String authorUsername;
    private final String content;
    private final LocalDateTime createdAt;

    public Comment() {
        this.id = null;
        this.authorUsername = null;
        this.content = null;
        this.createdAt = null;
    }

    @JsonCreator
    public Comment(
            @JsonProperty("id") UUID id,
            @JsonProperty("authorUsername") String authorUsername,
            @JsonProperty("content") String content,
            @JsonProperty("createdAt") LocalDateTime createdAt
    ) {
        this.id = id;
        this.authorUsername = authorUsername;
        this.content = content;
        this.createdAt = createdAt;
    }

    public static Comment create(String authorUsername, String content) {
        return new Comment(UUID.randomUUID(), authorUsername, content, LocalDateTime.now());
    }

    public UUID getId() {
        return id;
    }

    public String getAuthorUsername() {
        return authorUsername;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}

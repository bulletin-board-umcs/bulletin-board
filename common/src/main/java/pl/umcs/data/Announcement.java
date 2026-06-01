package pl.umcs.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Announcement implements Serializable {

    private final UUID id;
    private final String title;
    private final String content;
    private final String authorUsername;
    private final AnnouncementCategory category;
    private final LocalDateTime createdAt;
    private final LocalDateTime expiresAt;
    private final List<Comment> comments;

    public Announcement() {
        this.id = null;
        this.title = null;
        this.content = null;
        this.authorUsername = null;
        this.category = null;
        this.createdAt = null;
        this.expiresAt = null;
        this.comments = new ArrayList<>();
    }

    @JsonCreator
    public Announcement(
            @JsonProperty("id") UUID id,
            @JsonProperty("title") String title,
            @JsonProperty("content") String content,
            @JsonProperty("authorUsername") String authorUsername,
            @JsonProperty("category") AnnouncementCategory category,
            @JsonProperty("createdAt") LocalDateTime createdAt,
            @JsonProperty("expiresAt") LocalDateTime expiresAt,
            @JsonProperty("comments") List<Comment> comments
    ) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.authorUsername = authorUsername;
        this.category = category;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.comments = comments != null ? comments : new ArrayList<>();
    }

    public static Announcement create(String title,
                                      String content,
                                      String authorUsername,
                                      AnnouncementCategory category
    ) {
        LocalDateTime now = LocalDateTime.now();
        return new Announcement(
                UUID.randomUUID(),
                title,
                content,
                authorUsername,
                category,
                now, now.plusHours(24),
                new ArrayList<>());
    }

    public void addComment(Comment comment) {
        comments.add(comment);
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public UUID getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getAuthorUsername() {
        return authorUsername;
    }

    public AnnouncementCategory getCategory() {
        return category;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public List<Comment> getComments() {
        return Collections.unmodifiableList(comments);
    }
}

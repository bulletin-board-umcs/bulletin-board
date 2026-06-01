package pl.umcs.manager;

import pl.umcs.data.User;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SessionManager {

    private final Map<String, User> sessions = new ConcurrentHashMap<>();

    public String createSession(User user) {
        String token = UUID.randomUUID().toString();
        sessions.put(token, user);
        return token;
    }

    public User requireUser(String token) {
        User user = sessions.get(token);
        if (user == null) {
            throw new IllegalArgumentException(
                    "Session has expired. Please log in again."
            );
        }
        return user;
    }

    public void removeSession(String token) {
        sessions.remove(token);
    }

}

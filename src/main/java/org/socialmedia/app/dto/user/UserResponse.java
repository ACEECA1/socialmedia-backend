package org.socialmedia.app.dto.user;

import lombok.Builder;
import lombok.Data;
import org.socialmedia.app.model.user.User;
import org.socialmedia.app.model.user.UserStatus;

import java.util.List;

@Data
@Builder
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private UserStatus status;
    private List<String> roles;

    public static UserResponse fromUser(User user, List<String> roles) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .status(user.getStatus())
                .roles(roles)
                .build();
    }
}

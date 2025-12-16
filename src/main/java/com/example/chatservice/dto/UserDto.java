package com.example.chatservice.dto;

import com.example.chatservice.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserDto {
    private String id;
    private String username;
    private String email;
    private String role;
    private boolean online;
    private boolean nicknameCompleted;

    public UserDto(String id, String username, String email, String role, boolean nicknameCompleted) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.role = role;
        this.nicknameCompleted = nicknameCompleted;
    }

    public static UserDto from(User user) {
        return new UserDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                user.isNicknameCompleted()
        );
    }
}

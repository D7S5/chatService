package com.example.chatservice.dto;

import com.example.chatservice.entity.Participant;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ParticipantDto {
    private String userId;
    private String username;
    private RoomRole role;

    public static ParticipantDto from(Participant participant) {
        return new ParticipantDto(
                participant.getUserId(),
                null,
                participant.getRole()
        );

    }
}

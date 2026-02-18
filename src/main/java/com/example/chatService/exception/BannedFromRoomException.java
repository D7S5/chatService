package com.example.chatService.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class BannedFromRoomException extends RuntimeException {

    public BannedFromRoomException(String roomId) {
        super("You are banned from room: " + roomId);
    }
}

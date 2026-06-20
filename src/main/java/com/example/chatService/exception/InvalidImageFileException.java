package com.example.chatService.exception;

public class InvalidImageFileException extends RuntimeException {

    public InvalidImageFileException(String message) {
        super(message);
    }
}

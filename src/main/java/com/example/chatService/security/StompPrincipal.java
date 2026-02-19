package com.example.chatService.security;

import lombok.AllArgsConstructor;

import java.security.Principal;

@AllArgsConstructor
public class StompPrincipal implements Principal {
    private final String name;
    @Override public String getName() { return name; }
}

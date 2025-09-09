package com.example.boardpjt.model.dto;

import lombok.Getter;
import lombok.Setter;

public class PostDTO {
    @Getter
    @Setter
    public static class Request {
        private String title;
        private String content;
        private String username;
    }

    public record Response(Long id, String title, String content, String username, String createdAt) {
    }
}

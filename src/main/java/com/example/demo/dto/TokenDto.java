package com.example.demo.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TokenDto {
    private String token;
    private Long tokenExpiresIn;
}

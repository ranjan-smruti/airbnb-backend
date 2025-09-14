package com.codingshuttle.projects.airBnbApp.DTO;

import lombok.Data;

@Data
public class SignUpRequestDTO {
    private String email;
    private String password;
    private String name;
}

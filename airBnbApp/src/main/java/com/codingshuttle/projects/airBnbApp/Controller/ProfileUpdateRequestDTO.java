package com.codingshuttle.projects.airBnbApp.Controller;

import com.codingshuttle.projects.airBnbApp.Entity.enums.Gender;
import com.codingshuttle.projects.airBnbApp.Entity.enums.Roles;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ProfileUpdateRequestDTO {
    private String name;
    private LocalDate dateOfBirth;
    private Gender gender;
    //private Roles roles;
}

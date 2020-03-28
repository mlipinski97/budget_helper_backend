package pl.lipinski.engineerdegree.dao.dto;

import lombok.Data;

@Data
public class UserRegistrationDto {
    private String username;
    private String password;
    private String passwordConfirmation;
}


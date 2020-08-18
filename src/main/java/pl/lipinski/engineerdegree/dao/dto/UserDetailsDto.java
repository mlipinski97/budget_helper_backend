package pl.lipinski.engineerdegree.dao.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserDetailsDto {
    private String username;
    private String roles;
}

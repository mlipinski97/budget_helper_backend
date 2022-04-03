package pl.lipinski.engineerdegree.service;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import pl.lipinski.engineerdegree.dao.dto.UserDetailsDto;
import pl.lipinski.engineerdegree.dao.dto.UserRegistrationDto;
import pl.lipinski.engineerdegree.dao.entity.User;

import java.util.Optional;

public interface UserService {

    void deleteByUsername(String username);

    ResponseEntity<?> saveUser(UserRegistrationDto userRegistrationDto, BindingResult bindingResult);

    ResponseEntity<?> saveAdmin(UserRegistrationDto userRegistrationDto, BindingResult bindingResult);

    UserDetailsDto account();

    Iterable<User> findAll();

    Optional<User> findByUsername(String username);

    ResponseEntity<?> getAllByBudgetListId(Long budgetListId);
}

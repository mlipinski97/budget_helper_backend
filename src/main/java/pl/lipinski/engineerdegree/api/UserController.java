package pl.lipinski.engineerdegree.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import pl.lipinski.engineerdegree.dao.dto.UserDetailsDto;
import pl.lipinski.engineerdegree.dao.dto.UserRegistrationDto;
import pl.lipinski.engineerdegree.dao.entity.User;
import pl.lipinski.engineerdegree.service.UserService;

import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {


    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/getall")
    public Iterable<User> getAll() {
        return userService.findAll();
    }

    @GetMapping("/getbyusername")
    public Optional<User> getByUsername(@RequestParam String username) {
        return userService.findByUsername(username);
    }

    //TODO with budgelist service
    @GetMapping("/getallbybudgetlistid")
    public ResponseEntity<?> getAllByBudgetListId(@RequestParam Long budgetListId) {
        return userService.getAllByBudgetListId(budgetListId);
    }

    @DeleteMapping("/delete")
    public void deleteByUsername(@RequestParam String username) {
        userService.deleteByUsername(username);
    }

    @PostMapping("/register")
    public ResponseEntity<?> saveUser(@ModelAttribute("userform") UserRegistrationDto userRegistrationDto,
                                      BindingResult bindingResult) {
        return userService.saveUser(userRegistrationDto, bindingResult);
    }

    @PostMapping("/registeradmin")
    public ResponseEntity<?> saveAdmin(@ModelAttribute("userform") UserRegistrationDto userRegistrationDto,
                                       BindingResult bindingResult) {
        return userService.saveAdmin(userRegistrationDto, bindingResult);
    }

    @GetMapping("/account")
    public UserDetailsDto account() {
        return userService.account();
    }
}

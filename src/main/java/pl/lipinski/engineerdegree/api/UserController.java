package pl.lipinski.engineerdegree.api;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import pl.lipinski.engineerdegree.dao.dto.UserDetailsDto;
import pl.lipinski.engineerdegree.dao.dto.UserRegistrationDto;
import pl.lipinski.engineerdegree.dao.entity.User;
import pl.lipinski.engineerdegree.manager.UserManager;

import java.util.NoSuchElementException;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private UserManager userManager;
    private ModelMapper modelMapper;

    @Autowired
    public UserController(UserManager userManager) {
        this.userManager = userManager;
        this.modelMapper = new ModelMapper();
    }

    @GetMapping("/getall")
    public Iterable<User> getAll(){
        return userManager.findAll();
    }

    @GetMapping("/getbyusername")
    public Optional<User> getByUsername(String username){
        return userManager.findByUsername(username);
    }

    @PostMapping("/register")
    public ResponseEntity saveUser(@ModelAttribute("userform")UserRegistrationDto userRegistrationDto,
                                   BindingResult bindingResult){
        User user = modelMapper.map(userRegistrationDto, User.class);
        userManager.saveUser(user);
        return ResponseEntity.ok(modelMapper.map(user, UserDetailsDto.class));
    }
    @PostMapping("/registeradmin")
    public ResponseEntity saveAdmin(@ModelAttribute("userform")UserRegistrationDto userRegistrationDto,
                                   BindingResult bindingResult){
        User admin = modelMapper.map(userRegistrationDto, User.class);
        userManager.saveUser(admin);
        return ResponseEntity.ok(modelMapper.map(admin, UserDetailsDto.class));
    }

    @GetMapping("/account")
    public UserDetailsDto account() {
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<User> user = userManager.findByUsername(name);
        user.orElseThrow(NoSuchElementException::new);
        return modelMapper.map(user.get(), UserDetailsDto.class);
    }
    
}

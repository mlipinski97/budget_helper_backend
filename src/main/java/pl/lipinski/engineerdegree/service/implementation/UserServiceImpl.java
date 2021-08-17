package pl.lipinski.engineerdegree.service.implementation;

import org.modelmapper.ModelMapper;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import pl.lipinski.engineerdegree.dao.dto.UserDetailsDto;
import pl.lipinski.engineerdegree.dao.dto.UserRegistrationDto;
import pl.lipinski.engineerdegree.dao.entity.BudgetList;
import pl.lipinski.engineerdegree.dao.entity.User;
import pl.lipinski.engineerdegree.dao.entity.intersection.UserBudgetListIntersection;
import pl.lipinski.engineerdegree.dao.repository.BudgetListRepo;
import pl.lipinski.engineerdegree.dao.repository.UserRepo;
import pl.lipinski.engineerdegree.service.UserBudgetListIntersectionService;
import pl.lipinski.engineerdegree.service.UserService;
import pl.lipinski.engineerdegree.util.error.ControllerError;
import pl.lipinski.engineerdegree.util.validator.UserRegistrationValidator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Optional;

import static pl.lipinski.engineerdegree.util.error.ErrorCodes.BUDGET_LIST_NOT_FOUND_ERROR_CODE;
import static pl.lipinski.engineerdegree.util.error.ErrorMessages.BUDGET_LIST_NOT_FOUND_ERROR_MESSAGE;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
    private final UserRegistrationValidator userRegistrationValidator;
    private final BudgetListRepo budgetListRepo;
    private final UserBudgetListIntersectionService userBudgetListIntersectionService;

    public UserServiceImpl(UserRepo userRepo,
                           PasswordEncoder passwordEncoder,
                           @Lazy UserRegistrationValidator userRegistrationValidator,
                           BudgetListRepo budgetListRepo,
                           UserBudgetListIntersectionService userBudgetListIntersectionService) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.userRegistrationValidator = userRegistrationValidator;
        this.budgetListRepo = budgetListRepo;
        this.userBudgetListIntersectionService = userBudgetListIntersectionService;
        this.modelMapper = new ModelMapper();
    }

    public Optional<User> findByUsername(String username) {
        return userRepo.findByUsername(username);
    }

    public void deleteByUsername(String username) {
        userRepo.deleteByUsername(username);
    }

    public Iterable<User> findAll() {
        return userRepo.findAll();
    }

    public ResponseEntity<?> saveUser(UserRegistrationDto userRegistrationDto,
                                      BindingResult bindingResult) {
        userRegistrationValidator.validate(userRegistrationDto, bindingResult);
        if (bindingResult.hasErrors()) {
            ControllerError controllerError = new ControllerError(HttpStatus.BAD_REQUEST,
                    userRegistrationValidator.getErrorCode(),
                    userRegistrationValidator.getErrorMessages(bindingResult));
            return new ResponseEntity(controllerError, HttpStatus.BAD_REQUEST);
        }
        User user = modelMapper.map(userRegistrationDto, User.class);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRoles("ROLE_USER");
        user.setEnabled(true);
        userRepo.save(user);
        return ResponseEntity.ok(modelMapper.map(user, UserDetailsDto.class));
    }

    public ResponseEntity<?> saveAdmin(UserRegistrationDto userRegistrationDto,
                                       BindingResult bindingResult) {
        userRegistrationValidator.validate(userRegistrationDto, bindingResult);
        if (bindingResult.hasErrors()) {
            ControllerError controllerError = new ControllerError(HttpStatus.BAD_REQUEST,
                    userRegistrationValidator.getErrorCode(),
                    userRegistrationValidator.getErrorMessages(bindingResult));
            return new ResponseEntity(controllerError, HttpStatus.BAD_REQUEST);
        }
        User admin = modelMapper.map(userRegistrationDto, User.class);
        admin.setPassword(passwordEncoder.encode(admin.getPassword()));
        admin.setRoles("ROLE_ADMIN");
        admin.setEnabled(true);
        userRepo.save(admin);
        return ResponseEntity.ok(modelMapper.map(admin, UserDetailsDto.class));
    }

    public UserDetailsDto account() {
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<User> user = findByUsername(name);
        user.orElseThrow(NoSuchElementException::new);
        return modelMapper.map(user.get(), UserDetailsDto.class);
    }

    public ResponseEntity<?> getAllByBudgetListId(Long budgetListId) {
        Optional<BudgetList> searchedBudgetList = budgetListRepo.findById(budgetListId);
        if (!searchedBudgetList.isPresent()) {
            ControllerError controllerError = new ControllerError(HttpStatus.BAD_REQUEST,
                    BUDGET_LIST_NOT_FOUND_ERROR_CODE.getValue(),
                    Collections.singletonList(BUDGET_LIST_NOT_FOUND_ERROR_MESSAGE.getMessage()));
            return new ResponseEntity(controllerError, HttpStatus.BAD_REQUEST);
        }
        ArrayList<UserDetailsDto> users = new ArrayList<>();
        for (UserBudgetListIntersection ubli : userBudgetListIntersectionService
                .findAllByIntersectionBudgetList(searchedBudgetList.get())) {
            users.add(modelMapper.map(ubli.getIntersectionUser(), UserDetailsDto.class));
        }
        return ResponseEntity.ok(users);
    }

    //filler method used instead of data.sql file for educational purpose
    @EventListener(ApplicationReadyEvent.class)
    public void dbFiller() {
        userRepo.save(new User("admin", passwordEncoder.encode("admin"), true, "ROLE_ADMIN"));
        userRepo.save(new User("user", passwordEncoder.encode("user"), true, "ROLE_USER"));
    }
}

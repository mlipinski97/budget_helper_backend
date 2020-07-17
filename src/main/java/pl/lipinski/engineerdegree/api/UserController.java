package pl.lipinski.engineerdegree.api;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import pl.lipinski.engineerdegree.dao.dto.UserDetailsDto;
import pl.lipinski.engineerdegree.dao.dto.UserRegistrationDto;
import pl.lipinski.engineerdegree.dao.entity.BudgetList;
import pl.lipinski.engineerdegree.dao.entity.User;
import pl.lipinski.engineerdegree.dao.entity.UserBudgetListIntersection;
import pl.lipinski.engineerdegree.manager.BudgetListManager;
import pl.lipinski.engineerdegree.manager.UserBudgetListIntersectionManager;
import pl.lipinski.engineerdegree.manager.UserManager;
import pl.lipinski.engineerdegree.util.error.ControllerError;
import pl.lipinski.engineerdegree.util.validator.UserRegistrationValidator;

import java.util.*;

import static pl.lipinski.engineerdegree.util.error.ERRORCODES.BUDGET_LIST_NOT_FOUND_ERROR_CODE;
import static pl.lipinski.engineerdegree.util.error.ERRORMESSAGES.BUDGET_LIST_NOT_FOUND_ERROR_MESSAGE;

@RestController
@RequestMapping("/api/users")
public class UserController {


    private final UserManager userManager;
    private final ModelMapper modelMapper;
    private final UserRegistrationValidator userRegistrationValidator;
    private UserBudgetListIntersectionManager userBudgetListIntersectionManager;
    private BudgetListManager budgetListManager;

    @Autowired
    public UserController(UserManager userManager,
                          UserRegistrationValidator userRegistrationValidator,
                          UserBudgetListIntersectionManager userBudgetListIntersectionManager,
                          BudgetListManager budgetListManager) {
        this.userManager = userManager;
        this.modelMapper = new ModelMapper();
        this.userRegistrationValidator = userRegistrationValidator;
        this.userBudgetListIntersectionManager = userBudgetListIntersectionManager;
        this.budgetListManager = budgetListManager;
    }

    @GetMapping("/getall")
    public Iterable<User> getAll(){
        return userManager.findAll();
    }

    @GetMapping("/getbyusername")
    public Optional<User> getByUsername(@RequestParam String username){
        return userManager.findByUsername(username);
    }

    @GetMapping("/getallbybudgetlistid")
    public ResponseEntity<Iterable<UserDetailsDto>> getAllByBudgetListId(@RequestParam Long budgetListId){
        Optional<BudgetList> searchedBudgetList = budgetListManager.findById(budgetListId);
        if(!searchedBudgetList.isPresent()){
            ControllerError controllerError = new ControllerError(HttpStatus.BAD_REQUEST,
                    BUDGET_LIST_NOT_FOUND_ERROR_CODE.getValue(),
                    Collections.singletonList(BUDGET_LIST_NOT_FOUND_ERROR_MESSAGE.getMessage()));
            return new ResponseEntity(controllerError, HttpStatus.BAD_REQUEST);
        }
        ArrayList<UserDetailsDto> users = new ArrayList<>();
        for (UserBudgetListIntersection ubli : userBudgetListIntersectionManager
                .findAllByIntersectionBudgetList(searchedBudgetList.get())) {
            users.add(modelMapper.map(ubli.getIntersectionUser(), UserDetailsDto.class));
        }
        return ResponseEntity.ok(users);
    }

    @DeleteMapping("/delete")
    public void deleteByUsername(@RequestParam String username){
        userManager.deleteByUsername(username);
    }

    @PostMapping("/register")
    public ResponseEntity<UserDetailsDto> saveUser(@ModelAttribute("userform")UserRegistrationDto userRegistrationDto,
                                   BindingResult bindingResult){
        userRegistrationValidator.validate(userRegistrationDto, bindingResult);
        if(bindingResult.hasErrors()){
            ControllerError controllerError = new ControllerError(HttpStatus.BAD_REQUEST,
                    userRegistrationValidator.getErrorCode(),
                    userRegistrationValidator.getErrorMessages(bindingResult));
            return new ResponseEntity(controllerError, HttpStatus.BAD_REQUEST);
        }
        User user = modelMapper.map(userRegistrationDto, User.class);
        userManager.saveUser(user);
        return ResponseEntity.ok(modelMapper.map(user, UserDetailsDto.class));
    }

    @PostMapping("/registeradmin")
    public ResponseEntity<UserDetailsDto> saveAdmin(@ModelAttribute("userform")UserRegistrationDto userRegistrationDto,
                                   BindingResult bindingResult){
        userRegistrationValidator.validate(userRegistrationDto, bindingResult);
        if(bindingResult.hasErrors()){
            ControllerError controllerError = new ControllerError(HttpStatus.BAD_REQUEST,
                    userRegistrationValidator.getErrorCode(),
                    userRegistrationValidator.getErrorMessages(bindingResult));
            return new ResponseEntity(controllerError, HttpStatus.BAD_REQUEST);
        }
        User admin = modelMapper.map(userRegistrationDto, User.class);
        userManager.saveAdmin(admin);
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

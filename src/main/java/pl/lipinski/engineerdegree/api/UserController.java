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
import pl.lipinski.engineerdegree.dao.entity.intersection.FriendshipIntersection;
import pl.lipinski.engineerdegree.dao.entity.intersection.UserBudgetListIntersection;
import pl.lipinski.engineerdegree.manager.BudgetListManager;
import pl.lipinski.engineerdegree.manager.FriendshipIntersectionManager;
import pl.lipinski.engineerdegree.manager.UserBudgetListIntersectionManager;
import pl.lipinski.engineerdegree.manager.UserManager;
import pl.lipinski.engineerdegree.util.error.ControllerError;
import pl.lipinski.engineerdegree.util.validator.UserRegistrationValidator;

import java.util.*;

import static pl.lipinski.engineerdegree.util.error.ERRORCODES.*;
import static pl.lipinski.engineerdegree.util.error.ERRORMESSAGES.*;

@RestController
@RequestMapping("/api/users")
public class UserController {


    private final UserManager userManager;
    private final ModelMapper modelMapper;
    private final UserRegistrationValidator userRegistrationValidator;
    private UserBudgetListIntersectionManager userBudgetListIntersectionManager;
    private BudgetListManager budgetListManager;
    private FriendshipIntersectionManager friendshipIntersectionManager;

    @Autowired
    public UserController(UserManager userManager,
                          UserRegistrationValidator userRegistrationValidator,
                          UserBudgetListIntersectionManager userBudgetListIntersectionManager,
                          BudgetListManager budgetListManager,
                          FriendshipIntersectionManager friendshipIntersectionManager) {
        this.userManager = userManager;
        this.modelMapper = new ModelMapper();
        this.userRegistrationValidator = userRegistrationValidator;
        this.userBudgetListIntersectionManager = userBudgetListIntersectionManager;
        this.budgetListManager = budgetListManager;
        this.friendshipIntersectionManager = friendshipIntersectionManager;
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

    @GetMapping("/friendship/getallfriends")
    public ResponseEntity<Iterable<FriendshipIntersection>> getAllFriends(){
        String requesterName = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<User> requester = userManager.findByUsername(requesterName);
        if(!requester.isPresent()){
            ControllerError controllerError = new ControllerError(HttpStatus.BAD_REQUEST,
                    REQUESTER_NOT_FOUND_ERROR_CODE.getValue(),
                    Collections.singletonList(REQUESTER_NOT_FOUND_ERROR_MESSAGE.getMessage()));
            return new ResponseEntity(controllerError, HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.ok(friendshipIntersectionManager.findAllByRequesterOrFriend(requester.get()));
    }


    @GetMapping("/friendship/findFriendship")
    public ResponseEntity<Optional<FriendshipIntersection>> findFriendship(@RequestParam String friendUsername){
        Optional<User> friend = userManager.findByUsername(friendUsername);
        if(!friend.isPresent()){
            ControllerError controllerError = new ControllerError(HttpStatus.BAD_REQUEST,
                    USER_NOT_FOUND_ERROR_CODE.getValue(),
                    Collections.singletonList(USER_NOT_FOUND_ERROR_MESSAGE.getMessage()));
            return new ResponseEntity(controllerError, HttpStatus.BAD_REQUEST);
        }
        String requesterName = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<User> requester = userManager.findByUsername(requesterName);
        if(!requester.isPresent()){
            ControllerError controllerError = new ControllerError(HttpStatus.BAD_REQUEST,
                    REQUESTER_NOT_FOUND_ERROR_CODE.getValue(),
                    Collections.singletonList(REQUESTER_NOT_FOUND_ERROR_MESSAGE.getMessage()));
            return new ResponseEntity(controllerError, HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.ok(friendshipIntersectionManager.
                findByUsers(requester.get(), friend.get()));
    }

    @PostMapping("/friendship/add")
    public ResponseEntity<FriendshipIntersection> saveFriendshipIntersection(@RequestParam String friendUsername){
        Optional<User> friend = userManager.findByUsername(friendUsername);
        if(!friend.isPresent()){
            ControllerError controllerError = new ControllerError(HttpStatus.BAD_REQUEST,
                    USER_NOT_FOUND_ERROR_CODE.getValue(),
                    Collections.singletonList(USER_NOT_FOUND_ERROR_MESSAGE.getMessage()));
            return new ResponseEntity(controllerError, HttpStatus.BAD_REQUEST);
        }
        String requesterName = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<User> requester = userManager.findByUsername(requesterName);
        if(!requester.isPresent()){
            ControllerError controllerError = new ControllerError(HttpStatus.BAD_REQUEST,
                    REQUESTER_NOT_FOUND_ERROR_CODE.getValue(),
                    Collections.singletonList(REQUESTER_NOT_FOUND_ERROR_MESSAGE.getMessage()));
            return new ResponseEntity(controllerError, HttpStatus.BAD_REQUEST);
        }
        Optional<FriendshipIntersection> intersection = friendshipIntersectionManager
                .findByUsers(requester.get(), friend.get());
        if(intersection.isPresent()){
            ControllerError controllerError = new ControllerError(HttpStatus.BAD_REQUEST,
                    FRIENDSHIP_INTERSECTION_ALREADY_EXISTS_ERROR_CODE.getValue(),
                    Collections.singletonList(FRIENDSHIP_INTERSECTION_ALREADY_EXISTS_ERROR_MESSAGE.getMessage()));
            return new ResponseEntity(controllerError, HttpStatus.BAD_REQUEST);
        }
        if(requesterName.equals(friendUsername)){
            ControllerError controllerError = new ControllerError(HttpStatus.BAD_REQUEST,
                    FRIENDSHIP_WITH_SELF_NOT_ALLOWED_ERROR_CODE.getValue(),
                    Collections.singletonList(FRIENDSHIP_WITH_SELF_NOT_ALLOWED_ERROR_MESSAGE.getMessage()));
            return new ResponseEntity(controllerError, HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.ok(friendshipIntersectionManager.save(requester.get(), friend.get()));
    }

    @DeleteMapping("/friendship/delete")
    public ResponseEntity removeFriendship(@RequestParam String friendUsername){
        Optional<User> friend = userManager.findByUsername(friendUsername);
        if(!friend.isPresent()){
            ControllerError controllerError = new ControllerError(HttpStatus.BAD_REQUEST,
                    USER_NOT_FOUND_ERROR_CODE.getValue(),
                    Collections.singletonList(USER_NOT_FOUND_ERROR_MESSAGE.getMessage()));
            return new ResponseEntity(controllerError, HttpStatus.BAD_REQUEST);
        }
        String requesterName = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<User> requester = userManager.findByUsername(requesterName);
        if(!requester.isPresent()){
            ControllerError controllerError = new ControllerError(HttpStatus.BAD_REQUEST,
                    REQUESTER_NOT_FOUND_ERROR_CODE.getValue(),
                    Collections.singletonList(REQUESTER_NOT_FOUND_ERROR_MESSAGE.getMessage()));
            return new ResponseEntity(controllerError, HttpStatus.BAD_REQUEST);
        }
        Optional<FriendshipIntersection> intersection = friendshipIntersectionManager
                .findByUsers(requester.get(), friend.get());
        if(!intersection.isPresent()){
            ControllerError controllerError = new ControllerError(HttpStatus.BAD_REQUEST,
                    FRIENDSHIP_INTERSECTION_DOES_NOT_EXISTS_ERROR_CODE.getValue(),
                    Collections.singletonList(FRIENDSHIP_INTERSECTION_DOES_NOT_EXISTS_ERROR_MESSAGE.getMessage()));
            return new ResponseEntity(controllerError, HttpStatus.BAD_REQUEST);
        }
        friendshipIntersectionManager.deleteById(intersection.get().getId());
        return ResponseEntity.ok(0);
    }

    @DeleteMapping("/friendship/deletemany")
    public ResponseEntity removeFriendship(@RequestBody List<String> friendUsernames){
       ArrayList<FriendshipIntersection> intersections = new ArrayList<>();
       String requesterName = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<User> requester = userManager.findByUsername(requesterName);
        if(!requester.isPresent()){
            ControllerError controllerError = new ControllerError(HttpStatus.BAD_REQUEST,
                    REQUESTER_NOT_FOUND_ERROR_CODE.getValue(),
                    Collections.singletonList(REQUESTER_NOT_FOUND_ERROR_MESSAGE.getMessage()));
            return new ResponseEntity(controllerError, HttpStatus.BAD_REQUEST);
        }
       for(String friendUsername : friendUsernames){
           Optional<User> friend = userManager.findByUsername(friendUsername);
           if(!friend.isPresent()){
               ControllerError controllerError = new ControllerError(HttpStatus.BAD_REQUEST,
                       USER_NOT_FOUND_ERROR_CODE.getValue(),
                       Collections.singletonList(USER_NOT_FOUND_ERROR_MESSAGE.getMessage()));
               return new ResponseEntity(controllerError, HttpStatus.BAD_REQUEST);
           }
           Optional<FriendshipIntersection> intersection = friendshipIntersectionManager
                   .findByUsers(requester.get(), friend.get());
           if(!intersection.isPresent()){
               ControllerError controllerError = new ControllerError(HttpStatus.BAD_REQUEST,
                       FRIENDSHIP_INTERSECTION_DOES_NOT_EXISTS_ERROR_CODE.getValue(),
                       Collections.singletonList(FRIENDSHIP_INTERSECTION_DOES_NOT_EXISTS_ERROR_MESSAGE.getMessage()));
               return new ResponseEntity(controllerError, HttpStatus.BAD_REQUEST);
           }
           friendshipIntersectionManager.deleteById(intersection.get().getId());
       }
       return ResponseEntity.ok(0);
    }

    @PatchMapping("/friendship/accept")
    public ResponseEntity<FriendshipIntersection> acceptFriendship(@RequestParam String requesterUsername){
        Optional<User> requester = userManager.findByUsername(requesterUsername);
        if(!requester.isPresent()){
            ControllerError controllerError = new ControllerError(HttpStatus.BAD_REQUEST,
                    USER_NOT_FOUND_ERROR_CODE.getValue(),
                    Collections.singletonList(USER_NOT_FOUND_ERROR_MESSAGE.getMessage()));
            return new ResponseEntity(controllerError, HttpStatus.BAD_REQUEST);
        }
        String friendUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<User> friend = userManager.findByUsername(friendUsername);
        if(!friend.isPresent()){
            ControllerError controllerError = new ControllerError(HttpStatus.BAD_REQUEST,
                    REQUESTER_NOT_FOUND_ERROR_CODE.getValue(),
                    Collections.singletonList(REQUESTER_NOT_FOUND_ERROR_MESSAGE.getMessage()));
            return new ResponseEntity(controllerError, HttpStatus.BAD_REQUEST);
        }
        Optional<FriendshipIntersection> intersection = friendshipIntersectionManager
                .findByUsers(requester.get(), friend.get());
        if(!intersection.isPresent()){
            ControllerError controllerError = new ControllerError(HttpStatus.BAD_REQUEST,
                    FRIENDSHIP_INTERSECTION_DOES_NOT_EXISTS_ERROR_CODE.getValue(),
                    Collections.singletonList(FRIENDSHIP_INTERSECTION_DOES_NOT_EXISTS_ERROR_MESSAGE.getMessage()));
            return new ResponseEntity(controllerError, HttpStatus.BAD_REQUEST);
        }
        intersection.get().setAccepted(true);
        return ResponseEntity.ok(friendshipIntersectionManager.acceptFriendship(intersection.get()));
    }
}

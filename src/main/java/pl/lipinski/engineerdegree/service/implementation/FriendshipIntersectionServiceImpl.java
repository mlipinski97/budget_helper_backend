package pl.lipinski.engineerdegree.service.implementation;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import pl.lipinski.engineerdegree.dao.entity.User;
import pl.lipinski.engineerdegree.dao.entity.intersection.FriendshipIntersection;
import pl.lipinski.engineerdegree.dao.repository.FriendshipIntersectionRepo;
import pl.lipinski.engineerdegree.service.FriendshipIntersectionService;
import pl.lipinski.engineerdegree.service.UserService;
import pl.lipinski.engineerdegree.util.error.ControllerError;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static pl.lipinski.engineerdegree.util.error.ErrorCodes.*;
import static pl.lipinski.engineerdegree.util.error.ErrorMessages.*;

@Service
public class FriendshipIntersectionServiceImpl implements FriendshipIntersectionService {

    private final FriendshipIntersectionRepo friendshipIntersectionRepo;
    private final UserService userService;

    public FriendshipIntersectionServiceImpl(FriendshipIntersectionRepo friendshipIntersectionRepo, UserService userService) {
        this.friendshipIntersectionRepo = friendshipIntersectionRepo;
        this.userService = userService;
    }

    public ResponseEntity<?> findAllByRequesterOrFriend() {
        String requesterName = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<User> requester = userService.findByUsername(requesterName);
        if (!requester.isPresent()) {
            ControllerError controllerError = new ControllerError(HttpStatus.BAD_REQUEST,
                    REQUESTER_NOT_FOUND_ERROR_CODE.getValue(),
                    Collections.singletonList(REQUESTER_NOT_FOUND_ERROR_MESSAGE.getMessage()));
            return new ResponseEntity(controllerError, HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.ok(friendshipIntersectionRepo.findAllByRequesterOrFriend(requester.get()));
    }

    public ResponseEntity<?> findFriendship(String friendUsername) {
        Optional<User> friend = userService.findByUsername(friendUsername);
        if (!friend.isPresent()) {
            ControllerError controllerError = new ControllerError(HttpStatus.BAD_REQUEST,
                    USER_NOT_FOUND_ERROR_CODE.getValue(),
                    Collections.singletonList(USER_NOT_FOUND_ERROR_MESSAGE.getMessage()));
            return new ResponseEntity(controllerError, HttpStatus.BAD_REQUEST);
        }
        String requesterName = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<User> requester = userService.findByUsername(requesterName);
        if (!requester.isPresent()) {
            ControllerError controllerError = new ControllerError(HttpStatus.BAD_REQUEST,
                    REQUESTER_NOT_FOUND_ERROR_CODE.getValue(),
                    Collections.singletonList(REQUESTER_NOT_FOUND_ERROR_MESSAGE.getMessage()));
            return new ResponseEntity(controllerError, HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.ok(friendshipIntersectionRepo.findByUsers(requester.get(), friend.get()));
    }

    public ResponseEntity<?> saveFriendshipIntersection(String friendUsername){
        Optional<User> friend = userService.findByUsername(friendUsername);
        if (!friend.isPresent()) {
            ControllerError controllerError = new ControllerError(HttpStatus.BAD_REQUEST,
                    USER_NOT_FOUND_ERROR_CODE.getValue(),
                    Collections.singletonList(USER_NOT_FOUND_ERROR_MESSAGE.getMessage()));
            return new ResponseEntity(controllerError, HttpStatus.BAD_REQUEST);
        }
        String requesterName = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<User> requester = userService.findByUsername(requesterName);
        if (!requester.isPresent()) {
            ControllerError controllerError = new ControllerError(HttpStatus.BAD_REQUEST,
                    REQUESTER_NOT_FOUND_ERROR_CODE.getValue(),
                    Collections.singletonList(REQUESTER_NOT_FOUND_ERROR_MESSAGE.getMessage()));
            return new ResponseEntity(controllerError, HttpStatus.BAD_REQUEST);
        }
        Optional<FriendshipIntersection> intersection = friendshipIntersectionRepo.findByUsers(requester.get(), friend.get());
        if (intersection.isPresent()) {
            ControllerError controllerError = new ControllerError(HttpStatus.BAD_REQUEST,
                    FRIENDSHIP_INTERSECTION_ALREADY_EXISTS_ERROR_CODE.getValue(),
                    Collections.singletonList(FRIENDSHIP_INTERSECTION_ALREADY_EXISTS_ERROR_MESSAGE.getMessage()));
            return new ResponseEntity(controllerError, HttpStatus.BAD_REQUEST);
        }
        if (requesterName.equals(friendUsername)) {
            ControllerError controllerError = new ControllerError(HttpStatus.BAD_REQUEST,
                    FRIENDSHIP_WITH_SELF_NOT_ALLOWED_ERROR_CODE.getValue(),
                    Collections.singletonList(FRIENDSHIP_WITH_SELF_NOT_ALLOWED_ERROR_MESSAGE.getMessage()));
            return new ResponseEntity(controllerError, HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.ok(save(requester.get(), friend.get()));
    }

    public ResponseEntity<?> deleteFriendship(String friendUsername){
        Optional<User> friend = userService.findByUsername(friendUsername);
        if (!friend.isPresent()) {
            ControllerError controllerError = new ControllerError(HttpStatus.BAD_REQUEST,
                    USER_NOT_FOUND_ERROR_CODE.getValue(),
                    Collections.singletonList(USER_NOT_FOUND_ERROR_MESSAGE.getMessage()));
            return new ResponseEntity(controllerError, HttpStatus.BAD_REQUEST);
        }
        String requesterName = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<User> requester = userService.findByUsername(requesterName);
        if (!requester.isPresent()) {
            ControllerError controllerError = new ControllerError(HttpStatus.BAD_REQUEST,
                    REQUESTER_NOT_FOUND_ERROR_CODE.getValue(),
                    Collections.singletonList(REQUESTER_NOT_FOUND_ERROR_MESSAGE.getMessage()));
            return new ResponseEntity(controllerError, HttpStatus.BAD_REQUEST);
        }
        Optional<FriendshipIntersection> intersection = friendshipIntersectionRepo.findByUsers(requester.get(), friend.get());
        if (!intersection.isPresent()) {
            ControllerError controllerError = new ControllerError(HttpStatus.BAD_REQUEST,
                    FRIENDSHIP_INTERSECTION_DOES_NOT_EXISTS_ERROR_CODE.getValue(),
                    Collections.singletonList(FRIENDSHIP_INTERSECTION_DOES_NOT_EXISTS_ERROR_MESSAGE.getMessage()));
            return new ResponseEntity(controllerError, HttpStatus.BAD_REQUEST);
        }
        deleteById(intersection.get().getId());
        return ResponseEntity.ok(0);
    }

    public ResponseEntity<?> removeFriendships(List<String> friendsUsernames){
        ArrayList<FriendshipIntersection> intersections = new ArrayList<>();
        String requesterName = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<User> requester = userService.findByUsername(requesterName);
        if (!requester.isPresent()) {
            ControllerError controllerError = new ControllerError(HttpStatus.BAD_REQUEST,
                    REQUESTER_NOT_FOUND_ERROR_CODE.getValue(),
                    Collections.singletonList(REQUESTER_NOT_FOUND_ERROR_MESSAGE.getMessage()));
            return new ResponseEntity(controllerError, HttpStatus.BAD_REQUEST);
        }
        for (String friendUsername : friendsUsernames) {
            Optional<User> friend = userService.findByUsername(friendUsername);
            if (!friend.isPresent()) {
                ControllerError controllerError = new ControllerError(HttpStatus.BAD_REQUEST,
                        USER_NOT_FOUND_ERROR_CODE.getValue(),
                        Collections.singletonList(USER_NOT_FOUND_ERROR_MESSAGE.getMessage()));
                return new ResponseEntity(controllerError, HttpStatus.BAD_REQUEST);
            }
            Optional<FriendshipIntersection> intersection = friendshipIntersectionRepo
                    .findByUsers(requester.get(), friend.get());
            if (!intersection.isPresent()) {
                ControllerError controllerError = new ControllerError(HttpStatus.BAD_REQUEST,
                        FRIENDSHIP_INTERSECTION_DOES_NOT_EXISTS_ERROR_CODE.getValue(),
                        Collections.singletonList(FRIENDSHIP_INTERSECTION_DOES_NOT_EXISTS_ERROR_MESSAGE.getMessage()));
                return new ResponseEntity(controllerError, HttpStatus.BAD_REQUEST);
            }
            deleteById(intersection.get().getId());
        }
        return ResponseEntity.ok(0);
    }

    public ResponseEntity<?> acceptFriendship(String requesterUsername){
        Optional<User> requester = userService.findByUsername(requesterUsername);
        if (!requester.isPresent()) {
            ControllerError controllerError = new ControllerError(HttpStatus.BAD_REQUEST,
                    USER_NOT_FOUND_ERROR_CODE.getValue(),
                    Collections.singletonList(USER_NOT_FOUND_ERROR_MESSAGE.getMessage()));
            return new ResponseEntity(controllerError, HttpStatus.BAD_REQUEST);
        }
        String friendUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<User> friend = userService.findByUsername(friendUsername);
        if (!friend.isPresent()) {
            ControllerError controllerError = new ControllerError(HttpStatus.BAD_REQUEST,
                    REQUESTER_NOT_FOUND_ERROR_CODE.getValue(),
                    Collections.singletonList(REQUESTER_NOT_FOUND_ERROR_MESSAGE.getMessage()));
            return new ResponseEntity(controllerError, HttpStatus.BAD_REQUEST);
        }
        Optional<FriendshipIntersection> intersection = friendshipIntersectionRepo
                .findByUsers(requester.get(), friend.get());
        if (!intersection.isPresent()) {
            ControllerError controllerError = new ControllerError(HttpStatus.BAD_REQUEST,
                    FRIENDSHIP_INTERSECTION_DOES_NOT_EXISTS_ERROR_CODE.getValue(),
                    Collections.singletonList(FRIENDSHIP_INTERSECTION_DOES_NOT_EXISTS_ERROR_MESSAGE.getMessage()));
            return new ResponseEntity(controllerError, HttpStatus.BAD_REQUEST);
        }
        intersection.get().setAccepted(true);
        return ResponseEntity.ok(friendshipIntersectionRepo.save(intersection.get()));
    }

    public void deleteById(Long id) {
        friendshipIntersectionRepo.deleteById(id);
    }

    public FriendshipIntersection save(User requester, User friend) {
        FriendshipIntersection friendshipIntersection = new FriendshipIntersection();
        friendshipIntersection.setAccepted(false);
        friendshipIntersection.setRequester(requester);
        friendshipIntersection.setFriend(friend);
        return friendshipIntersectionRepo.save(friendshipIntersection);
    }
}

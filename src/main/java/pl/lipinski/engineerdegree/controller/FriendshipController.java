package pl.lipinski.engineerdegree.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.lipinski.engineerdegree.service.FriendshipIntersectionService;

import java.util.List;

@RestController
@RequestMapping("/api/users/friendship")
public class FriendshipController {
    private final FriendshipIntersectionService friendshipIntersectionService;

    public FriendshipController(FriendshipIntersectionService friendshipIntersectionService) {
        this.friendshipIntersectionService = friendshipIntersectionService;
    }


    @GetMapping("/getallfriends")
    public ResponseEntity<?> getAllFriends() {
        return friendshipIntersectionService.findAllByRequesterOrFriend();
    }


    @GetMapping("/findFriendship")
    public ResponseEntity<?> findFriendship(@RequestParam String friendUsername) {
        return friendshipIntersectionService.findFriendship(friendUsername);
    }

    @PostMapping("/add")
    public ResponseEntity<?> saveFriendshipIntersection(@RequestParam String friendUsername) {
        return friendshipIntersectionService.saveFriendshipIntersection(friendUsername);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> removeFriendship(@RequestParam String friendUsername) {
        return friendshipIntersectionService.deleteFriendship(friendUsername);
    }

    @DeleteMapping("/deletemany")
    public ResponseEntity<?> removeFriendships(@RequestBody List<String> friendsUsernames) {
        return friendshipIntersectionService.removeFriendships(friendsUsernames);
    }

    @PatchMapping("/accept")
    public ResponseEntity<?> acceptFriendship(@RequestParam String requesterUsername) {
        return friendshipIntersectionService.acceptFriendship(requesterUsername);
    }
}

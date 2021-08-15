package pl.lipinski.engineerdegree.service;

import org.springframework.http.ResponseEntity;

import java.util.List;

public interface FriendshipIntersectionService {

    ResponseEntity<?> findAllByRequesterOrFriend();

    ResponseEntity<?> findFriendship(String friendUsername);

    ResponseEntity<?> acceptFriendship(String requesterUsername);

    ResponseEntity<?> removeFriendships(List<String> friendsUsernames);

    ResponseEntity<?> deleteFriendship(String friendUsername);

    ResponseEntity<?> saveFriendshipIntersection(String friendUsername);
}

package pl.lipinski.engineerdegree.dao.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.lipinski.engineerdegree.dao.entity.User;
import pl.lipinski.engineerdegree.dao.entity.intersection.FriendshipIntersection;

import java.util.Optional;

public interface FriendshipIntersectionRepo extends JpaRepository<FriendshipIntersection, Long> {
    public Iterable<FriendshipIntersection> findAllByRequesterOrFriend(User requester, User friend);
    public Optional<FriendshipIntersection> findByRequesterAndFriend(User requester, User friend);
}

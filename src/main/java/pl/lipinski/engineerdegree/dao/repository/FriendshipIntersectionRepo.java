package pl.lipinski.engineerdegree.dao.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.lipinski.engineerdegree.dao.entity.User;
import pl.lipinski.engineerdegree.dao.entity.intersection.FriendshipIntersection;

import javax.persistence.TypedQuery;
import java.util.Optional;

public interface FriendshipIntersectionRepo extends JpaRepository<FriendshipIntersection, Long> {

    @Query("SELECT f FROM FriendshipIntersection f WHERE f.requester = :#{#user} OR f.friend = :#{#user}")
    public Iterable<FriendshipIntersection> findAllByRequesterOrFriend(@Param("user") User user);

    @Query("SELECT f FROM FriendshipIntersection f WHERE f.requester = :#{#requester} AND f.friend = :#{#friend} " +
            "OR f.requester = :#{#friend} AND f.friend = :#{#requester}")
    public Optional<FriendshipIntersection> findByUsers(@Param("requester")User requester, @Param("friend") User friend);
}

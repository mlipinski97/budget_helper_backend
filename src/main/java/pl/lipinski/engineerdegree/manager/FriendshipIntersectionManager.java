package pl.lipinski.engineerdegree.manager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.lipinski.engineerdegree.dao.entity.User;
import pl.lipinski.engineerdegree.dao.entity.intersection.FriendshipIntersection;
import pl.lipinski.engineerdegree.dao.entity.intersection.UserBudgetListIntersection;
import pl.lipinski.engineerdegree.dao.repository.FriendshipIntersectionRepo;

import java.util.Optional;

@Service
public class FriendshipIntersectionManager {

    FriendshipIntersectionRepo friendshipIntersectionRepo;

    @Autowired
    public FriendshipIntersectionManager(FriendshipIntersectionRepo friendshipIntersectionRepo) {
        this.friendshipIntersectionRepo = friendshipIntersectionRepo;
    }

    public Optional<FriendshipIntersection> findById(Long id){
        return friendshipIntersectionRepo.findById(id);
    }

    public Iterable<FriendshipIntersection> findAllByRequesterOrFriend(User requester){
        return friendshipIntersectionRepo.findAllByRequesterOrFriend(requester);
    }

    public Optional<FriendshipIntersection> findByRequesterOrFriend(User requester, User friend){
        return friendshipIntersectionRepo.findByRequesterAndFriend(requester, friend);
    }

    public void deleteById(Long id){
        friendshipIntersectionRepo.deleteById(id);
    }

    public Optional<FriendshipIntersection> findByRequesterAndFriendOrFriendAndRequester(User requester, User friend){
        return friendshipIntersectionRepo.findByRequesterAndFriendOrFriendAndRequester(requester, friend);
    }

    public FriendshipIntersection save(User requester, User friend){
        FriendshipIntersection friendshipIntersection = new FriendshipIntersection();
        friendshipIntersection.setAccepted(false);
        friendshipIntersection.setRequester(requester);
        friendshipIntersection.setFriend(friend);
        return friendshipIntersectionRepo.save(friendshipIntersection);
    }
}

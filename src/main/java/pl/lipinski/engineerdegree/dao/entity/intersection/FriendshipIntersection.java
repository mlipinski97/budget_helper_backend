package pl.lipinski.engineerdegree.dao.entity.intersection;

import pl.lipinski.engineerdegree.dao.entity.User;

import javax.persistence.*;

@Entity
@Table(name = "friendship_intersection")
public class FriendshipIntersection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(referencedColumnName = "username")
    private User requester;

    @ManyToOne
    @JoinColumn(referencedColumnName = "username")
    private User friend;

    private Boolean isAccepted;

    public FriendshipIntersection() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getRequester() {
        return requester;
    }

    public void setRequester(User requester) {
        this.requester = requester;
    }

    public User getFriend() {
        return friend;
    }

    public void setFriend(User friend) {
        this.friend = friend;
    }

    public Boolean getAccepted() {
        return isAccepted;
    }

    public void setAccepted(Boolean accepted) {
        isAccepted = accepted;
    }
}

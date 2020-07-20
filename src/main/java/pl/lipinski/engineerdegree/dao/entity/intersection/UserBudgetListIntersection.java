package pl.lipinski.engineerdegree.dao.entity.intersection;

import pl.lipinski.engineerdegree.dao.entity.BudgetList;
import pl.lipinski.engineerdegree.dao.entity.User;

import javax.persistence.*;

@Entity
@Table(name = "user_budget_intersection")
public class UserBudgetListIntersection {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User intersectionUser;

    @ManyToOne
    private BudgetList intersectionBudgetList;

    public UserBudgetListIntersection() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getIntersectionUser() {
        return intersectionUser;
    }

    public void setIntersectionUser(User intersectionUser) {
        this.intersectionUser = intersectionUser;
    }

    public BudgetList getIntersectionBudgetList() {
        return intersectionBudgetList;
    }

    public void setIntersectionBudgetList(BudgetList intersectionBudgetList) {
        this.intersectionBudgetList = intersectionBudgetList;
    }
}

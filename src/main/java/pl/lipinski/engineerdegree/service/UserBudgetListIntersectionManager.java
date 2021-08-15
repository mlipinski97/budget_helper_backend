package pl.lipinski.engineerdegree.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.lipinski.engineerdegree.dao.entity.BudgetList;
import pl.lipinski.engineerdegree.dao.entity.User;
import pl.lipinski.engineerdegree.dao.entity.intersection.UserBudgetListIntersection;
import pl.lipinski.engineerdegree.dao.repository.UserBudgetListIntersectionRepo;

import java.util.Optional;

@Service
public class UserBudgetListIntersectionManager {

    UserBudgetListIntersectionRepo userBudgetListIntersectionRepo;

    @Autowired
    public UserBudgetListIntersectionManager(UserBudgetListIntersectionRepo userBudgetListIntersectionRepo) {
        this.userBudgetListIntersectionRepo = userBudgetListIntersectionRepo;
    }

    public Optional<UserBudgetListIntersection> findById(Long id) {
        return userBudgetListIntersectionRepo.findById(id);
    }

    public Iterable<UserBudgetListIntersection> findAllByIntersectionBudgetList(BudgetList budgetList) {
        return userBudgetListIntersectionRepo.findAllByIntersectionBudgetList(budgetList);
    }

    public void deleteById(Long id) {
        userBudgetListIntersectionRepo.deleteById(id);
    }

    public Iterable<UserBudgetListIntersection> findAllByIntersectionUser(User user) {
        return userBudgetListIntersectionRepo.findAllByIntersectionUser(user);
    }

    public Optional<UserBudgetListIntersection> findByIntersectionUserAndIntersectionBudgetList(User user, BudgetList budgetList) {
        return userBudgetListIntersectionRepo.findByIntersectionUserAndIntersectionBudgetList(user, budgetList);
    }

    public UserBudgetListIntersection save(User user, BudgetList budgetList) {
        UserBudgetListIntersection userBudgetListIntersection = new UserBudgetListIntersection();
        userBudgetListIntersection.setIntersectionBudgetList(budgetList);
        userBudgetListIntersection.setIntersectionUser(user);
        return userBudgetListIntersectionRepo.save(userBudgetListIntersection);
    }
}

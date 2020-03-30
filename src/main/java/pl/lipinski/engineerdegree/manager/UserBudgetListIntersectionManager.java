package pl.lipinski.engineerdegree.manager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.lipinski.engineerdegree.dao.entity.BudgetList;
import pl.lipinski.engineerdegree.dao.entity.User;
import pl.lipinski.engineerdegree.dao.entity.UserBudgetListIntersection;
import pl.lipinski.engineerdegree.dao.repository.UserBudgetListIntersectionRepo;

import java.util.Optional;

@Service
public class UserBudgetListIntersectionManager {

    UserBudgetListIntersectionRepo userBudgetListIntersectionRepo;

    @Autowired
    public UserBudgetListIntersectionManager(UserBudgetListIntersectionRepo userBudgetListIntersectionRepo) {
        this.userBudgetListIntersectionRepo = userBudgetListIntersectionRepo;
    }

    public Optional<UserBudgetListIntersection> findById(Long id){
        return userBudgetListIntersectionRepo.findById(id);
    }

    public void deleteById(Long id){
        userBudgetListIntersectionRepo.deleteById(id);
    }

    public Iterable<UserBudgetListIntersection> findAllByIntersectionUserAndAndIntersectionBudgetList(User user, BudgetList budgetList){
        return userBudgetListIntersectionRepo.findAllByIntersectionUserAndAndIntersectionBudgetList(user, budgetList);
    }

    public void save(User user, BudgetList budgetList){
        UserBudgetListIntersection userBudgetListIntersection = new UserBudgetListIntersection();
        userBudgetListIntersection.setIntersectionBudgetList(budgetList);
        userBudgetListIntersection.setIntersectionUser(user);
        userBudgetListIntersectionRepo.save(userBudgetListIntersection);
    }
}

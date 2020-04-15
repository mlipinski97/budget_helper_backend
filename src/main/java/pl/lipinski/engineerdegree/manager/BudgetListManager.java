package pl.lipinski.engineerdegree.manager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import pl.lipinski.engineerdegree.dao.entity.BudgetList;
import pl.lipinski.engineerdegree.dao.entity.User;
import pl.lipinski.engineerdegree.dao.entity.UserBudgetListIntersection;
import pl.lipinski.engineerdegree.dao.repository.BudgetListRepo;

import javax.persistence.EntityNotFoundException;
import java.util.*;

@Service
public class BudgetListManager {

    private final BudgetListRepo budgetListRepo;
    private final UserManager userManager;
    private final UserBudgetListIntersectionManager intersectionManager;

    @Autowired
    public BudgetListManager(BudgetListRepo budgetListRepo,
                             UserManager userManager,
                             UserBudgetListIntersectionManager intersectionManager) {
        this.budgetListRepo = budgetListRepo;
        this.userManager = userManager;
        this.intersectionManager = intersectionManager;
    }

    public Iterable<BudgetList> findAll(){
        return budgetListRepo.findAll();
    }

    public Optional<BudgetList> findById(Long id){
        return budgetListRepo.findById(id);
    }

    public Iterable<BudgetList> findByName(String name){
        return budgetListRepo.findByName(name);
    }

    public Iterable<BudgetList> findAllByUser(String username){
        Optional<User> user = userManager.findByUsername(username);
        user.orElseThrow(EntityNotFoundException::new);
        Iterable<UserBudgetListIntersection> intersections = intersectionManager.findAllByIntersectionUser(user.get());
        List<BudgetList> budgetLists = new ArrayList<>();
        for (UserBudgetListIntersection intersection : intersections ) {
            budgetLists.add(intersection.getIntersectionBudgetList());
        }
        return budgetLists;
    }

    public void deleteById(Long id){
        budgetListRepo.deleteById(id);
    }

    public void addBudgetList(BudgetList budgetList){
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<User> user = userManager.findByUsername(name);
        user.orElseThrow(EntityNotFoundException::new);
        budgetList.setRemainingValue(budgetList.getBudgetValue());
        budgetListRepo.save(budgetList);
        intersectionManager.save(user.get(), budgetList);
    }
    public void editBudgetList(BudgetList budgetList){
        budgetListRepo.save(budgetList);
    }


}

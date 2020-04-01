package pl.lipinski.engineerdegree.manager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import pl.lipinski.engineerdegree.dao.entity.BudgetList;
import pl.lipinski.engineerdegree.dao.entity.User;
import pl.lipinski.engineerdegree.dao.repository.BudgetListRepo;

import javax.persistence.EntityNotFoundException;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

@Service
public class BudgetListManager {

    private BudgetListRepo budgetListRepo;
    private UserManager userManager;
    private UserBudgetListIntersectionManager intersectionManager;

    @Autowired
    public BudgetListManager(BudgetListRepo budgetListRepo, UserManager userManager, UserBudgetListIntersectionManager intersectionManager) {
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

    public void deleteById(Long id){
        budgetListRepo.deleteById(id);
    }

    public void addBudgetList(BudgetList budgetList){
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<User> user = userManager.findByUsername(name);
        user.orElseThrow(EntityNotFoundException::new);
        budgetListRepo.save(budgetList);
        intersectionManager.save(user.get(), budgetList);
    }
    public void editBudgetList(BudgetList budgetList){
        budgetListRepo.save(budgetList);

    }
}

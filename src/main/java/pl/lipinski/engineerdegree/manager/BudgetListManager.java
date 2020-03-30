package pl.lipinski.engineerdegree.manager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import pl.lipinski.engineerdegree.dao.entity.BudgetList;
import pl.lipinski.engineerdegree.dao.entity.Expense;
import pl.lipinski.engineerdegree.dao.entity.User;
import pl.lipinski.engineerdegree.dao.repository.BudgetListRepo;

import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class BudgetListManager {

    BudgetListRepo budgetListRepo;
    private UserManager userManager;

    @Autowired
    public BudgetListManager(BudgetListRepo budgetListRepo, UserManager userManager) {
        this.budgetListRepo = budgetListRepo;
        this.userManager = userManager;
    }

    public Iterable<BudgetList> findAll(){
        return budgetListRepo.findAll();
    }

    public Optional<BudgetList> findById(Long id){
        return budgetListRepo.findById(id);
    }

    public Optional<BudgetList> findByName(String name){
        return budgetListRepo.findByName(name);
    }

    public void deleteById(Long id){
        budgetListRepo.deleteById(id);
    }

    public void addBudgetList(BudgetList budgetList){
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<User> user = userManager.findByUsername(name);
        user.orElseThrow(NoSuchElementException::new);
        budgetList.addUser(user.get());
        budgetListRepo.save(budgetList);
    }

}

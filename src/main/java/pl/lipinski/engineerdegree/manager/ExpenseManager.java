package pl.lipinski.engineerdegree.manager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import pl.lipinski.engineerdegree.dao.entity.Expense;
import pl.lipinski.engineerdegree.dao.entity.User;
import pl.lipinski.engineerdegree.dao.repository.ExpenseRepo;

import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class ExpenseManager {

    private ExpenseRepo expenseRepo;
    private UserManager userManager;

    @Autowired
    public ExpenseManager(ExpenseRepo expenseRepo, UserManager userManager) {
        this.expenseRepo = expenseRepo;
        this.userManager = userManager;
    }

    public Iterable<Expense> findAll(){
        return expenseRepo.findAll();
    }

    public Optional<Expense> findById(Long id){
        return expenseRepo.findById(id);
    }

    public void deleteByID(Long id){
        expenseRepo.deleteById(id);
    }

    public ResponseEntity addExpense(Expense expense){
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<User> user = userManager.findByUsername(name);
        user.orElseThrow(NoSuchElementException::new);
        expense.setExpenseOwner(user.get());
        expenseRepo.save(expense);
        return ResponseEntity.ok(expense);
    }


}

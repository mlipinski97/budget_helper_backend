package pl.lipinski.engineerdegree.manager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import pl.lipinski.engineerdegree.dao.entity.BudgetList;
import pl.lipinski.engineerdegree.dao.entity.Expense;
import pl.lipinski.engineerdegree.dao.entity.User;
import pl.lipinski.engineerdegree.dao.repository.ExpenseRepo;

import javax.persistence.EntityNotFoundException;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class ExpenseManager {

    private final ExpenseRepo expenseRepo;
    private final UserManager userManager;
    private final BudgetListManager budgetListManager;

    @Autowired
    public ExpenseManager(ExpenseRepo expenseRepo, UserManager userManager, BudgetListManager budgetListManager) {
        this.expenseRepo = expenseRepo;
        this.userManager = userManager;
        this.budgetListManager = budgetListManager;
    }

    public Iterable<Expense> findAll(){
        return expenseRepo.findAll();
    }

    public Optional<Expense> findById(Long id){
        return expenseRepo.findById(id);
    }

    public Iterable<Expense> findAllByBudgetList(BudgetList budgetList){
        return expenseRepo.findAllByBudgetList(budgetList);
    }

    public void deletebyId(Long id){
        Optional<Expense> expense = findById(id);
        expense.orElseThrow(EntityNotFoundException::new);
        BudgetList budgetList = expense.get().getBudgetList();
        expenseRepo.deleteById(id);
        updateBudgetListRemainingValue(budgetList);
    }

    public Expense addExpense(Expense expense){
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<User> user = userManager.findByUsername(name);
        user.orElseThrow(NoSuchElementException::new);
        expense.setExpenseOwner(user.get());
        if(expense.getDone() == null){
            expense.setDone(false);
        }
        Expense expenseToReturn = expenseRepo.save(expense);
        updateBudgetListRemainingValue(expenseToReturn.getBudgetList());
        return expenseToReturn;
    }

    public void updateBudgetListRemainingValue(BudgetList budgetList){
        Iterable<Expense> expenses = findAllByBudgetList(budgetList);
        double expensesSummary = 0;
        for (Expense e : expenses) {
            expensesSummary += e.getAmount();
        }
        Double difference = budgetList.getBudgetValue() - expensesSummary;
        budgetList.setRemainingValue(difference);
        budgetListManager.editBudgetList(budgetList);
    }
}

package pl.lipinski.engineerdegree.dao.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import pl.lipinski.engineerdegree.dao.entity.BudgetList;
import pl.lipinski.engineerdegree.dao.entity.Category;
import pl.lipinski.engineerdegree.dao.entity.User;

import java.util.Date;

@Data
public class ExpenseDto {

    //form data
    private String name;
    private Double amount;
    @DateTimeFormat(pattern = "dd-MM-yyyy")
    private Date dateOfExpense;

    private User expenseOwner;

    private BudgetList budgetList;

    private Category category;

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public BudgetList getBudgetList() {
        return budgetList;
    }

    public void setBudgetList(BudgetList budgetList) {
        this.budgetList = budgetList;
    }

    private Boolean isDone;

    public Date getDateOfExpense() {
        return dateOfExpense;
    }

    public void setDateOfExpense(Date dateOfExpense) {
        this.dateOfExpense = dateOfExpense;
    }
}

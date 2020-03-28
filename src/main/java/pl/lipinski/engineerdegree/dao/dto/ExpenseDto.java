package pl.lipinski.engineerdegree.dao.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import pl.lipinski.engineerdegree.dao.entity.User;

import java.time.LocalDate;

@Data
public class ExpenseDto {

    private String name;
    private Double amount;
    @DateTimeFormat(pattern = "dd-MM-yyyy")
    private LocalDate dateOfExpense;
    private User expenseOwner;

    public LocalDate getDateOfExpense() {
        return dateOfExpense;
    }

    public void setDateOfExpense(LocalDate dateOfExpense) {
        this.dateOfExpense = dateOfExpense;
    }
}

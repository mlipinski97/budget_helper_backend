package pl.lipinski.engineerdegree.dao.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
public class BudgetListDto {
    private String name;
    private Double budgetValue;
    private Double remainingValue;
    @DateTimeFormat(pattern = "dd-MM-yyyy")
    private LocalDate dueDate;
}

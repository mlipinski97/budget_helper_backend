package pl.lipinski.engineerdegree.dao.dto;

import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@EqualsAndHashCode
@AllArgsConstructor
public class BudgetListDto {
    private String name;
    private Double budgetValue;
    private Double remainingValue;
    @DateTimeFormat(pattern = "dd-MM-yyyy")
    private LocalDate dueDate;
    @DateTimeFormat(pattern = "dd-MM-yyyy")
    private LocalDate startingDate;
    private String currencyCode;
}

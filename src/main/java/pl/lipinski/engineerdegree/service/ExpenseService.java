package pl.lipinski.engineerdegree.service;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import pl.lipinski.engineerdegree.dao.dto.ExpenseDto;
import pl.lipinski.engineerdegree.dao.entity.Expense;

import java.util.List;

public interface ExpenseService {

    Iterable<Expense> findAll();

    ResponseEntity<?> findById(Long id);

    Iterable<Expense> getAllByBudgetListId(Long id);

    ResponseEntity<?> deleteById(Long id);

    ResponseEntity<?> deleteManyById(List<Long> idList);

    ResponseEntity<?> addExpense(Long budgetListId, String categoryName, ExpenseDto expenseDto, BindingResult bindingResult);

    ResponseEntity<?> changeToDone(Long id);

    ResponseEntity<?> changeToUndone(Long id);

    ResponseEntity<?> changeDoneState(Long id);

    ResponseEntity<?> edit(Long id, ExpenseDto expenseDto, BindingResult bindingResult);

    ResponseEntity<?> getAllByDateAndExpenseOwner(String startDate, String endDate, String username);
}

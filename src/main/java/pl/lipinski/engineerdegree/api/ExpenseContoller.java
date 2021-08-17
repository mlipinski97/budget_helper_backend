package pl.lipinski.engineerdegree.api;

import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import pl.lipinski.engineerdegree.dao.dto.ExpenseDto;
import pl.lipinski.engineerdegree.dao.entity.Expense;
import pl.lipinski.engineerdegree.service.ExpenseService;

import java.util.List;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseContoller {

    private final ExpenseService expenseService;

    public ExpenseContoller(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @Transactional
    @GetMapping("/getall")
    public Iterable<Expense> getAll() {
        return expenseService.findAll();
    }

    @Transactional
    @GetMapping("/getbyid")
    public ResponseEntity<?> findById(@RequestParam Long id) {
        return expenseService.findById(id);
    }

    @Transactional
    @GetMapping("/getallbybudgetlist")
    public Iterable<Expense> getAllByBudgetListId(@RequestParam Long id) {
        return expenseService.getAllByBudgetListId(id);
    }

    @DeleteMapping("/deletebyid")
    public ResponseEntity<?> deleteById(@RequestParam Long id) {
        return expenseService.deleteById(id);
    }

    @Transactional
    @DeleteMapping("/deletemany")
    public ResponseEntity<?> deleteManyById(@RequestBody List<Long> idList) {
        return expenseService.deleteManyById(idList);
    }

    @Transactional
    @PostMapping("/add")
    public ResponseEntity<?> addExpense(@RequestParam Long budgetListId,
                                        @RequestParam String categoryName,
                                        @ModelAttribute("expenseform") ExpenseDto expenseDto,
                                        BindingResult bindingResult) {
        return expenseService.addExpense(budgetListId, categoryName, expenseDto, bindingResult);
    }

    @Transactional
    @PatchMapping("/complete")
    public ResponseEntity<?> changeToDone(@RequestParam Long id) {
        return expenseService.changeToDone(id);
    }

    @Transactional
    @PatchMapping("/undocomplete")
    public ResponseEntity<?> changeToUndone(@RequestParam Long id) {
        return expenseService.changeToUndone(id);
    }

    @Transactional
    @PatchMapping("/changedonestate")
    public ResponseEntity<?> changeDoneState(@RequestParam Long id) {
        return expenseService.changeDoneState(id);
    }

    //Endpoint not used. Front end app in final version dont allow users to edit expenses.
    //need to change edit to accommodate adding category field in db entity
    @Transactional
    @PatchMapping("/edit")
    public ResponseEntity<?> edit(@RequestParam Long id,
                                  @ModelAttribute("expenseform") ExpenseDto expenseDto,
                                  BindingResult bindingResult) {
        return expenseService.edit(id, expenseDto, bindingResult);
    }

    @Transactional
    @GetMapping("/getmonthstatistics")
    public ResponseEntity<?> getAllByDateAndExpenseOwner(@RequestParam String startDate,
                                                         @RequestParam String endDate,
                                                         @RequestParam String username) {
        return expenseService.getAllByDateAndExpenseOwner(startDate, endDate, username);
    }


}

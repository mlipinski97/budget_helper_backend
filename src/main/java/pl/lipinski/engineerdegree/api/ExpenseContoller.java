package pl.lipinski.engineerdegree.api;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import pl.lipinski.engineerdegree.dao.dto.ExpenseDto;
import pl.lipinski.engineerdegree.dao.entity.BudgetList;
import pl.lipinski.engineerdegree.dao.entity.Expense;
import pl.lipinski.engineerdegree.manager.BudgetListManager;
import pl.lipinski.engineerdegree.manager.ExpenseManager;
import pl.lipinski.engineerdegree.util.error.ControllerError;
import pl.lipinski.engineerdegree.util.validator.ExpenseValidator;

import javax.persistence.EntityNotFoundException;
import java.util.Arrays;
import java.util.Optional;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseContoller {

    private final Integer BUDGET_LIST_NOT_FOUND_ERROR_CODE = 509;
    private final Integer EXPENSE_NOT_FOUND_ERROR_CODE = 510;
    private final String EXPENSE_NOT_FOUND_ERROR_MESSAGE = "Expense not found!";
    private final String BUDGET_LIST_NOT_FOUND_ERROR_MESSAGE = "Budget list not found!";

    private ExpenseManager expenseManager;
    private ModelMapper modelMapper;
    private ExpenseValidator expenseValidator;
    private BudgetListManager budgetListManager;

    @Autowired
    public ExpenseContoller(ExpenseManager expenseManager, ExpenseValidator expenseValidator, BudgetListManager budgetListManage) {
        this.expenseManager = expenseManager;
        this.modelMapper = new ModelMapper();
        this.expenseValidator = expenseValidator;
        this.budgetListManager = budgetListManage;
    }

    @GetMapping("/getall")
    public Iterable<Expense> getAll(){
        return expenseManager.findAll();
    }

    @DeleteMapping("/deletebyid")
    public void deleteById(Long id){
        expenseManager.deletebyId(id);
    }

    @PostMapping("/add")
    public ResponseEntity save(@RequestParam Long budgetListId,
                               @ModelAttribute("expenseform")ExpenseDto expenseDto,
                               BindingResult bindingResult) {
        Optional<BudgetList> budgetList = budgetListManager.findById(budgetListId);
        if(!budgetList.isPresent()){
            ControllerError controllerError = new ControllerError(HttpStatus.BAD_REQUEST,
                    BUDGET_LIST_NOT_FOUND_ERROR_CODE,
                    Arrays.asList(BUDGET_LIST_NOT_FOUND_ERROR_MESSAGE));
            return new ResponseEntity(controllerError, HttpStatus.BAD_REQUEST);
        }
        expenseDto.setBudgetList(budgetList.get());
        expenseValidator.validate(expenseDto, bindingResult);
        if(bindingResult.hasErrors()){
            ControllerError controllerError = new ControllerError(HttpStatus.BAD_REQUEST,
                    expenseValidator.getErrorCode(),
                    expenseValidator.getErrorMessages(bindingResult));
            return new ResponseEntity(controllerError, HttpStatus.BAD_REQUEST);
        }
        Expense expense = modelMapper.map(expenseDto, Expense.class);
        expenseManager.addExpense(expense);
        return ResponseEntity.ok(modelMapper.map(expense, Expense.class));
    }

    @PatchMapping("/complete")
    public ResponseEntity changeToDone(@RequestParam Long id){
        Optional<Expense> expense = expenseManager.findById(id);
        if(!expense.isPresent()){
            ControllerError controllerError = new ControllerError(HttpStatus.BAD_REQUEST,
                    EXPENSE_NOT_FOUND_ERROR_CODE,
                    Arrays.asList(EXPENSE_NOT_FOUND_ERROR_MESSAGE));
            return new ResponseEntity(controllerError, HttpStatus.BAD_REQUEST);
        }
        expense.get().setDone(true);
        Expense updatedExpense = expenseManager.addExpense(expense.get());
        return ResponseEntity.ok(updatedExpense);
    }

    @PatchMapping("/undocomplete")
    public ResponseEntity changeToUndone(@RequestParam Long id){
        Optional<Expense> expense = expenseManager.findById(id);
        if(!expense.isPresent()){
            ControllerError controllerError = new ControllerError(HttpStatus.BAD_REQUEST,
                    EXPENSE_NOT_FOUND_ERROR_CODE,
                    Arrays.asList(EXPENSE_NOT_FOUND_ERROR_MESSAGE));
            return new ResponseEntity(controllerError, HttpStatus.BAD_REQUEST);
        }
        expense.get().setDone(false);
        Expense updatedExpense = expenseManager.addExpense(expense.get());
        return ResponseEntity.ok(updatedExpense);
    }

    @PatchMapping("/edit")
    public ResponseEntity edit(@RequestParam Long id,
                               @ModelAttribute("expenseform")ExpenseDto expenseDto,
                               BindingResult bindingResult) {
        Optional<Expense> expenseToUpdate = expenseManager.findById(id);
        if(!expenseToUpdate.isPresent()){
            ControllerError controllerError = new ControllerError(HttpStatus.BAD_REQUEST,
                    EXPENSE_NOT_FOUND_ERROR_CODE,
                    Arrays.asList(EXPENSE_NOT_FOUND_ERROR_MESSAGE));
            return new ResponseEntity(controllerError, HttpStatus.BAD_REQUEST);
        }
        expenseValidator.validate(expenseDto, bindingResult);
        if(bindingResult.hasErrors()){
            ControllerError controllerError = new ControllerError(HttpStatus.BAD_REQUEST,
                    expenseValidator.getErrorCode(),
                    expenseValidator.getErrorMessages(bindingResult));
            return new ResponseEntity(controllerError, HttpStatus.BAD_REQUEST);
        }
        Expense expense = modelMapper.map(expenseDto, Expense.class);
        expenseToUpdate.get().setAmount(expense.getAmount());
        expenseToUpdate.get().setDateOfExpense(expense.getDateOfExpense());
        expenseToUpdate.get().setName(expense.getName());

        expenseManager.addExpense(expenseToUpdate.get());
        return ResponseEntity.ok(expenseToUpdate.get());
    }

}

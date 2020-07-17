package pl.lipinski.engineerdegree.api;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import pl.lipinski.engineerdegree.dao.dto.ExpenseDto;
import pl.lipinski.engineerdegree.dao.entity.BudgetList;
import pl.lipinski.engineerdegree.dao.entity.Expense;
import pl.lipinski.engineerdegree.dao.entity.User;
import pl.lipinski.engineerdegree.manager.BudgetListManager;
import pl.lipinski.engineerdegree.manager.ExpenseManager;
import pl.lipinski.engineerdegree.manager.UserBudgetListIntersectionManager;
import pl.lipinski.engineerdegree.manager.UserManager;
import pl.lipinski.engineerdegree.util.error.ControllerError;
import pl.lipinski.engineerdegree.util.validator.ExpenseValidator;

import javax.persistence.EntityNotFoundException;
import java.util.Arrays;
import java.util.Optional;

import static pl.lipinski.engineerdegree.util.error.ERRORCODES.*;
import static pl.lipinski.engineerdegree.util.error.ERRORMESSAGES.*;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseContoller {

    private ExpenseManager expenseManager;
    private ModelMapper modelMapper;
    private ExpenseValidator expenseValidator;
    private BudgetListManager budgetListManager;
    private UserManager userManager;
    private UserBudgetListIntersectionManager intersectionManager;

    @Autowired
    public ExpenseContoller(ExpenseManager expenseManager,
                            ExpenseValidator expenseValidator,
                            BudgetListManager budgetListManage,
                            UserManager userManager,
                            UserBudgetListIntersectionManager intersectionManager) {
        this.expenseManager = expenseManager;
        this.modelMapper = new ModelMapper();
        this.expenseValidator = expenseValidator;
        this.budgetListManager = budgetListManage;
        this.userManager = userManager;
        this.intersectionManager = intersectionManager;
    }

    @GetMapping("/getall")
    public Iterable<Expense> getAll(){
        return expenseManager.findAll();
    }

    @DeleteMapping("/deletebyid")
    public ResponseEntity deleteById(@RequestParam Long id){
        expenseManager.findById(id).orElseThrow(EntityNotFoundException::new);
        if(!validatePermissions(expenseManager.findById(id).get())){
            ControllerError controllerError = new ControllerError(HttpStatus.BAD_REQUEST,
                    USER_DONT_HAVE_PERMISSIONS_ERROR_CODE.getValue(),
                    Arrays.asList(USER_DONT_HAVE_PERMISSIONS_ERROR_MESSAGE.getMessage()));
            return new ResponseEntity(controllerError, HttpStatus.BAD_REQUEST);
        }
        expenseManager.deletebyId(id);
        return new ResponseEntity(HttpStatus.OK);
    }

    @GetMapping("/getbyid")
    public ResponseEntity findById(@RequestParam Long id){
        Expense expense = expenseManager.findById(id).orElseThrow(EntityNotFoundException::new);
        if(!validatePermissions(expense)){
            ControllerError controllerError = new ControllerError(HttpStatus.BAD_REQUEST,
                    USER_DONT_HAVE_PERMISSIONS_ERROR_CODE.getValue(),
                    Arrays.asList(USER_DONT_HAVE_PERMISSIONS_ERROR_MESSAGE.getMessage()));
            return new ResponseEntity(controllerError, HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.ok(expense);
    }

    @GetMapping("/getallbybudgetlist")
    public Iterable<Expense> getAllByBudgetListId(@RequestParam Long id){
        Optional<BudgetList> budgetList = budgetListManager.findById(id);
        budgetList.orElseThrow(EntityNotFoundException::new);
        return expenseManager.findAllByBudgetList(budgetList.get());
    }

    @PostMapping("/add")
    public ResponseEntity save(@RequestParam Long budgetListId,
                               @ModelAttribute("expenseform")ExpenseDto expenseDto,
                               BindingResult bindingResult) {
        Optional<BudgetList> budgetList = budgetListManager.findById(budgetListId);
        if(!budgetList.isPresent()){
            ControllerError controllerError = new ControllerError(HttpStatus.BAD_REQUEST,
                    BUDGET_LIST_NOT_FOUND_ERROR_CODE.getValue(),
                    Arrays.asList(BUDGET_LIST_NOT_FOUND_ERROR_MESSAGE.getMessage()));
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
        if(!validatePermissions(expense)){
            ControllerError controllerError = new ControllerError(HttpStatus.BAD_REQUEST,
                    USER_DONT_HAVE_PERMISSIONS_ERROR_CODE.getValue(),
                    Arrays.asList(USER_DONT_HAVE_PERMISSIONS_ERROR_MESSAGE.getMessage()));
            return new ResponseEntity(controllerError, HttpStatus.BAD_REQUEST);
        }
        expenseManager.addExpense(expense);
        return ResponseEntity.ok(modelMapper.map(expense, Expense.class));
    }

    @PatchMapping("/complete")
    public ResponseEntity changeToDone(@RequestParam Long id){
        Optional<Expense> expense = expenseManager.findById(id);
        if(!expense.isPresent()){
            ControllerError controllerError = new ControllerError(HttpStatus.BAD_REQUEST,
                    EXPENSE_NOT_FOUND_ERROR_CODE.getValue(),
                    Arrays.asList(EXPENSE_NOT_FOUND_ERROR_MESSAGE.getMessage()));
            return new ResponseEntity(controllerError, HttpStatus.BAD_REQUEST);
        }
        if(!validatePermissions(expense.get())){
            ControllerError controllerError = new ControllerError(HttpStatus.BAD_REQUEST,
                    USER_DONT_HAVE_PERMISSIONS_ERROR_CODE.getValue(),
                    Arrays.asList(USER_DONT_HAVE_PERMISSIONS_ERROR_MESSAGE.getMessage()));
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
                    EXPENSE_NOT_FOUND_ERROR_CODE.getValue(),
                    Arrays.asList(EXPENSE_NOT_FOUND_ERROR_MESSAGE.getMessage()));
            return new ResponseEntity(controllerError, HttpStatus.BAD_REQUEST);
        }
        if(!validatePermissions(expense.get())){
            ControllerError controllerError = new ControllerError(HttpStatus.BAD_REQUEST,
                    USER_DONT_HAVE_PERMISSIONS_ERROR_CODE.getValue(),
                    Arrays.asList(USER_DONT_HAVE_PERMISSIONS_ERROR_MESSAGE.getMessage()));
            return new ResponseEntity(controllerError, HttpStatus.BAD_REQUEST);
        }
        expense.get().setDone(false);
        Expense updatedExpense = expenseManager.addExpense(expense.get());
        return ResponseEntity.ok(updatedExpense);
    }

    @PatchMapping("/changedonestate")
    public ResponseEntity changeDoneState(@RequestParam Long id){
        Optional<Expense> expense = expenseManager.findById(id);
        if(!expense.isPresent()){
            ControllerError controllerError = new ControllerError(HttpStatus.BAD_REQUEST,
                    EXPENSE_NOT_FOUND_ERROR_CODE.getValue(),
                    Arrays.asList(EXPENSE_NOT_FOUND_ERROR_MESSAGE.getMessage()));
            return new ResponseEntity(controllerError, HttpStatus.BAD_REQUEST);
        }
        if(!validatePermissions(expense.get())){
            ControllerError controllerError = new ControllerError(HttpStatus.BAD_REQUEST,
                    USER_DONT_HAVE_PERMISSIONS_ERROR_CODE.getValue(),
                    Arrays.asList(USER_DONT_HAVE_PERMISSIONS_ERROR_MESSAGE.getMessage()));
            return new ResponseEntity(controllerError, HttpStatus.BAD_REQUEST);
        }
        if(expense.get().getDone()){
            expense.get().setDone(false);
        } else{
            expense.get().setDone(true);
        }
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
                    EXPENSE_NOT_FOUND_ERROR_CODE.getValue(),
                    Arrays.asList(EXPENSE_NOT_FOUND_ERROR_MESSAGE.getMessage()));
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
        if(!validatePermissions(expense)){
            ControllerError controllerError = new ControllerError(HttpStatus.BAD_REQUEST,
                    USER_DONT_HAVE_PERMISSIONS_ERROR_CODE.getValue(),
                    Arrays.asList(USER_DONT_HAVE_PERMISSIONS_ERROR_MESSAGE.getMessage()));
            return new ResponseEntity(controllerError, HttpStatus.BAD_REQUEST);
        }
        expenseToUpdate.get().setAmount(expense.getAmount());
        expenseToUpdate.get().setDateOfExpense(expense.getDateOfExpense());
        expenseToUpdate.get().setName(expense.getName());

        expenseManager.addExpense(expenseToUpdate.get());
        return ResponseEntity.ok(expenseToUpdate.get());
    }

    private boolean validatePermissions(Expense expense){
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<User> user = userManager.findByUsername(name);
        if(!user.isPresent()){
            return false;
        }
        return intersectionManager.findByIntersectionUserAndIntersectionBudgetList(user.get(), expense.getBudgetList()).isPresent()
                || user.get().getRoles().equals("ROLE_ADMIN");
    }

}

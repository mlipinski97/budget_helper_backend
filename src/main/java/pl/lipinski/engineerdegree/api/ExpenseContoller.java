package pl.lipinski.engineerdegree.api;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import pl.lipinski.engineerdegree.dao.dto.ExpenseDto;
import pl.lipinski.engineerdegree.dao.entity.BudgetList;
import pl.lipinski.engineerdegree.dao.entity.Category;
import pl.lipinski.engineerdegree.dao.entity.Expense;
import pl.lipinski.engineerdegree.dao.entity.User;
import pl.lipinski.engineerdegree.service.*;
import pl.lipinski.engineerdegree.util.error.ControllerError;
import pl.lipinski.engineerdegree.util.validator.ExpenseValidator;

import javax.persistence.EntityNotFoundException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static pl.lipinski.engineerdegree.util.error.ErrorCodes.*;
import static pl.lipinski.engineerdegree.util.error.ErrorMessages.*;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseContoller {

    private final ExpenseManager expenseManager;
    private final ModelMapper modelMapper;
    private final ExpenseValidator expenseValidator;
    private final BudgetListService budgetListService;
    private final UserService userService;
    private final CategoryManager categoryManager;
    private final UserBudgetListIntersectionManager intersectionManager;

    @Autowired
    public ExpenseContoller(ExpenseManager expenseManager,
                            ExpenseValidator expenseValidator,
                            BudgetListService budgetListManage,
                            UserService userService,
                            UserBudgetListIntersectionManager intersectionManager,
                            CategoryManager categoryManager) {
        this.expenseManager = expenseManager;
        this.modelMapper = new ModelMapper();
        this.expenseValidator = expenseValidator;
        this.budgetListService = budgetListManage;
        this.userService = userService;
        this.intersectionManager = intersectionManager;
        this.categoryManager = categoryManager;
    }

    @Transactional
    @GetMapping("/getall")
    public Iterable<Expense> getAll() {
        return expenseManager.findAll();
    }

    @DeleteMapping("/deletebyid")
    public ResponseEntity<?> deleteById(@RequestParam Long id) {
        expenseManager.findById(id).orElseThrow(EntityNotFoundException::new);
        if (!validatePermissionsForExpense(expenseManager.findById(id).get())) {
            ControllerError controllerError = new ControllerError(HttpStatus.BAD_REQUEST,
                    USER_DONT_HAVE_PERMISSIONS_ERROR_CODE.getValue(),
                    Collections.singletonList(USER_DONT_HAVE_PERMISSIONS_ERROR_MESSAGE.getMessage()));
            return new ResponseEntity<>(controllerError, HttpStatus.BAD_REQUEST);
        }
        expenseManager.deletebyId(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }
    @Transactional
    @DeleteMapping("/deletemany")
    public ResponseEntity<?> deleteById(@RequestBody List<Long> idList) {
        for (Long id : idList) {
            expenseManager.findById(id).orElseThrow(EntityNotFoundException::new);
            if (!validatePermissionsForExpense(expenseManager.findById(id).get())) {
                ControllerError controllerError = new ControllerError(HttpStatus.BAD_REQUEST,
                        USER_DONT_HAVE_PERMISSIONS_ERROR_CODE.getValue(),
                        Collections.singletonList(USER_DONT_HAVE_PERMISSIONS_ERROR_MESSAGE.getMessage()));
                return new ResponseEntity<>(controllerError, HttpStatus.BAD_REQUEST);
            }
        }
        for (Long id : idList) {
            expenseManager.deletebyId(id);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }
    @Transactional
    @GetMapping("/getbyid")
    public ResponseEntity<?> findById(@RequestParam Long id) {
        Expense expense = expenseManager.findById(id).orElseThrow(EntityNotFoundException::new);
        if (!validatePermissionsForExpense(expense)) {
            ControllerError controllerError = new ControllerError(HttpStatus.BAD_REQUEST,
                    USER_DONT_HAVE_PERMISSIONS_ERROR_CODE.getValue(),
                    Collections.singletonList(USER_DONT_HAVE_PERMISSIONS_ERROR_MESSAGE.getMessage()));
            return new ResponseEntity<>(controllerError, HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.ok(expense);
    }

    @Transactional
    @GetMapping("/getallbybudgetlist")
    public Iterable<Expense> getAllByBudgetListId(@RequestParam Long id) {
        Optional<BudgetList> budgetList = budgetListService.findById(id);
        budgetList.orElseThrow(EntityNotFoundException::new);
        return expenseManager.findAllByBudgetList(budgetList.get());
    }

    @Transactional
    @PostMapping("/add")
    public ResponseEntity<?> save(@RequestParam Long budgetListId,
                               @RequestParam String categoryName,
                               @ModelAttribute("expenseform") ExpenseDto expenseDto,
                               BindingResult bindingResult) {
        Optional<BudgetList> budgetList = budgetListService.findById(budgetListId);
        if (!budgetList.isPresent()) {
            ControllerError controllerError = new ControllerError(HttpStatus.BAD_REQUEST,
                    BUDGET_LIST_NOT_FOUND_ERROR_CODE.getValue(),
                    Collections.singletonList(BUDGET_LIST_NOT_FOUND_ERROR_MESSAGE.getMessage()));
            return new ResponseEntity<>(controllerError, HttpStatus.BAD_REQUEST);
        }
        expenseDto.setBudgetList(budgetList.get());
        expenseValidator.validate(expenseDto, bindingResult);
        if (bindingResult.hasErrors()) {
            ControllerError controllerError = new ControllerError(HttpStatus.BAD_REQUEST,
                    expenseValidator.getErrorCode(),
                    expenseValidator.getErrorMessages(bindingResult));
            return new ResponseEntity<>(controllerError, HttpStatus.BAD_REQUEST);
        }
        Optional<Category> category = categoryManager.findByName(categoryName);

        if (!category.isPresent()) {
            ControllerError controllerError = new ControllerError(HttpStatus.BAD_REQUEST,
                    CATEGORY_NOT_FOUND_ERROR_CODE.getValue(),
                    Collections.singletonList(CATEGORY_NOT_FOUND_ERROR_MESSAGE.getMessage()));
            return new ResponseEntity<>(controllerError, HttpStatus.BAD_REQUEST);
        }
        expenseDto.setCategory(category.get());
        Expense expense = modelMapper.map(expenseDto, Expense.class);
        if (!validatePermissionsForExpense(expense)) {
            ControllerError controllerError = new ControllerError(HttpStatus.BAD_REQUEST,
                    USER_DONT_HAVE_PERMISSIONS_ERROR_CODE.getValue(),
                    Collections.singletonList(USER_DONT_HAVE_PERMISSIONS_ERROR_MESSAGE.getMessage()));
            return new ResponseEntity<>(controllerError, HttpStatus.BAD_REQUEST);
        }
        expenseManager.addExpense(expense);
        return ResponseEntity.ok(modelMapper.map(expense, Expense.class));
    }

    @Transactional
    @PatchMapping("/complete")
    public ResponseEntity<?> changeToDone(@RequestParam Long id) {
        Optional<Expense> expense = expenseManager.findById(id);
        if (!expense.isPresent()) {
            ControllerError controllerError = new ControllerError(HttpStatus.BAD_REQUEST,
                    EXPENSE_NOT_FOUND_ERROR_CODE.getValue(),
                    Collections.singletonList(EXPENSE_NOT_FOUND_ERROR_MESSAGE.getMessage()));
            return new ResponseEntity<>(controllerError, HttpStatus.BAD_REQUEST);
        }
        if (!validatePermissionsForExpense(expense.get())) {
            ControllerError controllerError = new ControllerError(HttpStatus.BAD_REQUEST,
                    USER_DONT_HAVE_PERMISSIONS_ERROR_CODE.getValue(),
                    Collections.singletonList(USER_DONT_HAVE_PERMISSIONS_ERROR_MESSAGE.getMessage()));
            return new ResponseEntity<>(controllerError, HttpStatus.BAD_REQUEST);
        }
        expense.get().setDone(true);
        Expense updatedExpense = expenseManager.addExpense(expense.get());
        return ResponseEntity.ok(updatedExpense);
    }

    @Transactional
    @PatchMapping("/undocomplete")
    public ResponseEntity<?> changeToUndone(@RequestParam Long id) {
        Optional<Expense> expense = expenseManager.findById(id);
        if (!expense.isPresent()) {
            ControllerError controllerError = new ControllerError(HttpStatus.BAD_REQUEST,
                    EXPENSE_NOT_FOUND_ERROR_CODE.getValue(),
                    Collections.singletonList(EXPENSE_NOT_FOUND_ERROR_MESSAGE.getMessage()));
            return new ResponseEntity<>(controllerError, HttpStatus.BAD_REQUEST);
        }
        if (!validatePermissionsForExpense(expense.get())) {
            ControllerError controllerError = new ControllerError(HttpStatus.BAD_REQUEST,
                    USER_DONT_HAVE_PERMISSIONS_ERROR_CODE.getValue(),
                    Collections.singletonList(USER_DONT_HAVE_PERMISSIONS_ERROR_MESSAGE.getMessage()));
            return new ResponseEntity<>(controllerError, HttpStatus.BAD_REQUEST);
        }
        expense.get().setDone(false);
        Expense updatedExpense = expenseManager.addExpense(expense.get());
        return ResponseEntity.ok(updatedExpense);
    }

    @Transactional
    @PatchMapping("/changedonestate")
    public ResponseEntity<?> changeDoneState(@RequestParam Long id) {
        Optional<Expense> expense = expenseManager.findById(id);
        if (!expense.isPresent()) {
            ControllerError controllerError = new ControllerError(HttpStatus.BAD_REQUEST,
                    EXPENSE_NOT_FOUND_ERROR_CODE.getValue(),
                    Collections.singletonList(EXPENSE_NOT_FOUND_ERROR_MESSAGE.getMessage()));
            return new ResponseEntity<>(controllerError, HttpStatus.BAD_REQUEST);
        }
        if (!validatePermissionsForExpense(expense.get())) {
            ControllerError controllerError = new ControllerError(HttpStatus.BAD_REQUEST,
                    USER_DONT_HAVE_PERMISSIONS_ERROR_CODE.getValue(),
                    Collections.singletonList(USER_DONT_HAVE_PERMISSIONS_ERROR_MESSAGE.getMessage()));
            return new ResponseEntity<>(controllerError, HttpStatus.BAD_REQUEST);
        }
        if (expense.get().getDone()) {
            expense.get().setDone(false);
        } else {
            expense.get().setDone(true);
        }
        Expense updatedExpense = expenseManager.addExpense(expense.get());
        return ResponseEntity.ok(updatedExpense);
    }

    //TODO change edit to accommodate to adding category field
    @Transactional
    @PatchMapping("/edit")
    public ResponseEntity<?> edit(@RequestParam Long id,
                               @ModelAttribute("expenseform") ExpenseDto expenseDto,
                               BindingResult bindingResult) {
        Optional<Expense> expenseToUpdate = expenseManager.findById(id);
        if (!expenseToUpdate.isPresent()) {
            ControllerError controllerError = new ControllerError(HttpStatus.BAD_REQUEST,
                    EXPENSE_NOT_FOUND_ERROR_CODE.getValue(),
                    Collections.singletonList(EXPENSE_NOT_FOUND_ERROR_MESSAGE.getMessage()));
            return new ResponseEntity<>(controllerError, HttpStatus.BAD_REQUEST);
        }
        expenseValidator.validate(expenseDto, bindingResult);
        if (bindingResult.hasErrors()) {
            ControllerError controllerError = new ControllerError(HttpStatus.BAD_REQUEST,
                    expenseValidator.getErrorCode(),
                    expenseValidator.getErrorMessages(bindingResult));
            return new ResponseEntity<>(controllerError, HttpStatus.BAD_REQUEST);
        }
        Expense expense = modelMapper.map(expenseDto, Expense.class);
        if (!validatePermissionsForExpense(expense)) {
            ControllerError controllerError = new ControllerError(HttpStatus.BAD_REQUEST,
                    USER_DONT_HAVE_PERMISSIONS_ERROR_CODE.getValue(),
                    Collections.singletonList(USER_DONT_HAVE_PERMISSIONS_ERROR_MESSAGE.getMessage()));
            return new ResponseEntity<>(controllerError, HttpStatus.BAD_REQUEST);
        }
        expenseToUpdate.get().setAmount(expense.getAmount());
        expenseToUpdate.get().setDateOfExpense(expense.getDateOfExpense());
        expenseToUpdate.get().setName(expense.getName());

        expenseManager.addExpense(expenseToUpdate.get());
        return ResponseEntity.ok(expenseToUpdate.get());
    }
    @Transactional
    @GetMapping("/getmonthstatistics")
    public ResponseEntity<?> getAllByDateAndExpenseOwner(@RequestParam String startDate,
                                                         @RequestParam String endDate,
                                                         @RequestParam String username){
        if (!validatePermissionsForLoggedUser(username)) {
            ControllerError controllerError = new ControllerError(HttpStatus.BAD_REQUEST,
                    USER_DONT_HAVE_PERMISSIONS_ERROR_CODE.getValue(),
                    Collections.singletonList(USER_DONT_HAVE_PERMISSIONS_ERROR_MESSAGE.getMessage()));
            return new ResponseEntity<>(controllerError, HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.ok(expenseManager.findAllByDateAndExpenseOwner(startDate, endDate, username));
    }

    private boolean validatePermissionsForLoggedUser(String username){
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<User> user = userService.findByUsername(name);
        if (!user.isPresent()) {
            return false;
        }
        return SecurityContextHolder.getContext().getAuthentication().getName().equals(username)
                || user.get().getRoles().equals("ROLE_ADMIN");
    }

    private boolean validatePermissionsForExpense(Expense expense) {
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<User> user = userService.findByUsername(name);
        if (!user.isPresent()) {
            return false;
        }
        return intersectionManager.findByIntersectionUserAndIntersectionBudgetList(user.get(), expense.getBudgetList()).isPresent()
                || user.get().getRoles().equals("ROLE_ADMIN");
    }

}

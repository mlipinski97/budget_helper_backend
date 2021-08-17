package pl.lipinski.engineerdegree.service.implementation;

import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestParam;
import pl.lipinski.engineerdegree.dao.dto.ExpenseDto;
import pl.lipinski.engineerdegree.dao.entity.BudgetList;
import pl.lipinski.engineerdegree.dao.entity.Category;
import pl.lipinski.engineerdegree.dao.entity.Expense;
import pl.lipinski.engineerdegree.dao.entity.User;
import pl.lipinski.engineerdegree.dao.repository.BudgetListRepo;
import pl.lipinski.engineerdegree.dao.repository.ExpenseRepo;
import pl.lipinski.engineerdegree.service.CategoryService;
import pl.lipinski.engineerdegree.service.ExpenseService;
import pl.lipinski.engineerdegree.service.UserBudgetListIntersectionService;
import pl.lipinski.engineerdegree.service.UserService;
import pl.lipinski.engineerdegree.util.error.ControllerError;
import pl.lipinski.engineerdegree.util.validator.ExpenseValidator;

import javax.persistence.EntityNotFoundException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static pl.lipinski.engineerdegree.util.error.ErrorCodes.*;
import static pl.lipinski.engineerdegree.util.error.ErrorMessages.*;

@Service
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseRepo expenseRepo;
    private final UserService userService;
    private final BudgetListRepo budgetListRepo;
    private final ExpenseValidator expenseValidator;
    private final CategoryService categoryService;
    private final ModelMapper modelMapper;
    private final UserBudgetListIntersectionService userBudgetListIntersectionService;

    public ExpenseServiceImpl(ExpenseRepo expenseRepo,
                              UserService userService,
                              BudgetListRepo budgetListRepo,
                              ExpenseValidator expenseValidator,
                              CategoryService categoryService, UserBudgetListIntersectionService userBudgetListIntersectionService) {
        this.expenseRepo = expenseRepo;
        this.userService = userService;
        this.budgetListRepo = budgetListRepo;
        this.expenseValidator = expenseValidator;
        this.categoryService = categoryService;
        this.userBudgetListIntersectionService = userBudgetListIntersectionService;
        this.modelMapper = new ModelMapper();
    }

    public Iterable<Expense> findAll() {
        return expenseRepo.findAll();
    }

    public ResponseEntity<?> findById(Long id) {
        Expense expense = expenseRepo.findById(id).orElseThrow(EntityNotFoundException::new);
        if (!validatePermissionsForExpense(expense)) {
            ControllerError controllerError = new ControllerError(HttpStatus.BAD_REQUEST,
                    USER_DONT_HAVE_PERMISSIONS_ERROR_CODE.getValue(),
                    Collections.singletonList(USER_DONT_HAVE_PERMISSIONS_ERROR_MESSAGE.getMessage()));
            return new ResponseEntity<>(controllerError, HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.ok(expense);
    }

    public Iterable<Expense> getAllByBudgetListId(@RequestParam Long id) {
        Optional<BudgetList> budgetList = budgetListRepo.findById(id);
        budgetList.orElseThrow(EntityNotFoundException::new);
        return expenseRepo.findAllByBudgetList(budgetList.get());
    }

    public ResponseEntity<?> deleteById(Long id) {
        Expense expense = expenseRepo.findById(id).orElseThrow(EntityNotFoundException::new);
        if (!validatePermissionsForExpense(expenseRepo.findById(id).get())) {
            ControllerError controllerError = new ControllerError(HttpStatus.BAD_REQUEST,
                    USER_DONT_HAVE_PERMISSIONS_ERROR_CODE.getValue(),
                    Collections.singletonList(USER_DONT_HAVE_PERMISSIONS_ERROR_MESSAGE.getMessage()));
            return new ResponseEntity<>(controllerError, HttpStatus.BAD_REQUEST);
        }
        BudgetList budgetList = expense.getBudgetList();
        expenseRepo.deleteById(id);
        updateBudgetListRemainingValue(budgetList);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    public ResponseEntity<?> deleteManyById(List<Long> idList) {
        for (Long id : idList) {
            expenseRepo.findById(id).orElseThrow(EntityNotFoundException::new);
            if (!validatePermissionsForExpense(expenseRepo.findById(id).get())) {
                ControllerError controllerError = new ControllerError(HttpStatus.BAD_REQUEST,
                        USER_DONT_HAVE_PERMISSIONS_ERROR_CODE.getValue(),
                        Collections.singletonList(USER_DONT_HAVE_PERMISSIONS_ERROR_MESSAGE.getMessage()));
                return new ResponseEntity<>(controllerError, HttpStatus.BAD_REQUEST);
            }
        }
        for (Long id : idList) {
            Expense expense = expenseRepo.findById(id).orElseThrow(EntityNotFoundException::new);
            BudgetList budgetList = expense.getBudgetList();
            expenseRepo.deleteById(id);
            updateBudgetListRemainingValue(budgetList);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    public ResponseEntity<?> addExpense(Long budgetListId,
                                  String categoryName,
                                  ExpenseDto expenseDto,
                                  BindingResult bindingResult) {
        Optional<BudgetList> budgetList = budgetListRepo.findById(budgetListId);
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
        Optional<Category> category = categoryService.getByName(categoryName);

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
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<User> user = userService.findByUsername(name);
        user.orElseThrow(NoSuchElementException::new);
        expense.setExpenseOwner(user.get());
        if (expense.getDone() == null) {
            expense.setDone(false);
        }
        Expense expenseToReturn = expenseRepo.save(expense);
        updateBudgetListRemainingValue(expenseToReturn.getBudgetList());
        return ResponseEntity.ok(modelMapper.map(expense, Expense.class));
    }

    public ResponseEntity<?> changeToDone(Long id) {
        Optional<Expense> expense = expenseRepo.findById(id);
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
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<User> user = userService.findByUsername(name);
        user.orElseThrow(NoSuchElementException::new);
        expense.get().setExpenseOwner(user.get());
        Expense updatedExpense = expenseRepo.save(expense.get());
        updateBudgetListRemainingValue(updatedExpense.getBudgetList());
        return ResponseEntity.ok(updatedExpense);
    }

    public ResponseEntity<?> changeToUndone(Long id) {
        Optional<Expense> expense = expenseRepo.findById(id);
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
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<User> user = userService.findByUsername(name);
        user.orElseThrow(NoSuchElementException::new);
        expense.get().setExpenseOwner(user.get());
        Expense updatedExpense = expenseRepo.save(expense.get());
        updateBudgetListRemainingValue(updatedExpense.getBudgetList());
        return ResponseEntity.ok(updatedExpense);
    }

    public ResponseEntity<?> changeDoneState(Long id) {
        Optional<Expense> expense = expenseRepo.findById(id);
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
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<User> user = userService.findByUsername(name);
        user.orElseThrow(NoSuchElementException::new);
        expense.get().setExpenseOwner(user.get());
        Expense updatedExpense = expenseRepo.save(expense.get());
        updateBudgetListRemainingValue(updatedExpense.getBudgetList());
        return ResponseEntity.ok(updatedExpense);
    }

    public ResponseEntity<?> edit(Long id,
                                  ExpenseDto expenseDto,
                                  BindingResult bindingResult) {
        Optional<Expense> expenseToUpdate = expenseRepo.findById(id);
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

        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<User> user = userService.findByUsername(name);
        user.orElseThrow(NoSuchElementException::new);
        expenseToUpdate.get().setExpenseOwner(user.get());
        Expense expenseToReturn = expenseRepo.save(expense);
        updateBudgetListRemainingValue(expenseToReturn.getBudgetList());
        return ResponseEntity.ok(expenseToReturn);
    }

    public ResponseEntity<?> getAllByDateAndExpenseOwner(String startDate,
                                                         String endDate,
                                                         String username) {
        if (!validatePermissionsForLoggedUser(username)) {
            ControllerError controllerError = new ControllerError(HttpStatus.BAD_REQUEST,
                    USER_DONT_HAVE_PERMISSIONS_ERROR_CODE.getValue(),
                    Collections.singletonList(USER_DONT_HAVE_PERMISSIONS_ERROR_MESSAGE.getMessage()));
            return new ResponseEntity<>(controllerError, HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.ok(findAllByDateAndExpenseOwner(startDate, endDate, username));
    }

    public Iterable<Expense> findAllByDateAndExpenseOwner(String startDate, String endDate, String username) {
        Date startDateObject = null;
        Date endDateObject = null;
        try {
            startDateObject = new SimpleDateFormat("dd-MM-yyyy").parse(startDate);
            endDateObject = new SimpleDateFormat("dd-MM-yyyy").parse(endDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return expenseRepo.findAllByDateAndExpenseOwner(startDateObject, endDateObject, username);
    }


    public void updateBudgetListRemainingValue(BudgetList budgetList) {
        Iterable<Expense> expenses = expenseRepo.findAllByBudgetList(budgetList);
        double expensesSummary = 0;
        for (Expense e : expenses) {
            expensesSummary += e.getAmount();
        }
        double difference = budgetList.getBudgetValue() - expensesSummary;
        difference = Math.floor(difference * 100) / 100;
        budgetList.setRemainingValue(difference);
        budgetListRepo.save(budgetList);
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
        return userBudgetListIntersectionService
                .findByIntersectionUserAndIntersectionBudgetList(user.get(), expense.getBudgetList()).isPresent()
                || user.get().getRoles().equals("ROLE_ADMIN");
    }
}

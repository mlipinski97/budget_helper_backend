package pl.lipinski.engineerdegree.api;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import pl.lipinski.engineerdegree.dao.dto.ExpenseDto;
import pl.lipinski.engineerdegree.dao.dto.UserDetailsDto;
import pl.lipinski.engineerdegree.dao.entity.Expense;
import pl.lipinski.engineerdegree.dao.entity.User;
import pl.lipinski.engineerdegree.manager.ExpenseManager;
import pl.lipinski.engineerdegree.util.error.ControllerError;
import pl.lipinski.engineerdegree.util.validator.ExpenseValidator;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseContoller {

    private ExpenseManager expenseManager;
    private ModelMapper modelMapper;
    private ExpenseValidator expenseValidator;

    @Autowired
    public ExpenseContoller(ExpenseManager expenseManager, ExpenseValidator expenseValidator) {
        this.expenseManager = expenseManager;
        this.modelMapper = new ModelMapper();
        this.expenseValidator = expenseValidator;
    }

    @GetMapping("/getall")
    public Iterable<Expense> getAll(){
        return expenseManager.findAll();
    }

    @DeleteMapping("/deletebyid")
    public void deleteById(Long id){
        expenseManager.deleteByID(id);
    }

    @PostMapping("/add")
    public ResponseEntity save(@ModelAttribute("expenseform")ExpenseDto expenseDto,
                               BindingResult bindingResult) {
        System.out.println(expenseDto.getDateOfExpense());
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
}

package pl.lipinski.engineerdegree.api;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import pl.lipinski.engineerdegree.dao.dto.BudgetListDto;
import pl.lipinski.engineerdegree.dao.dto.ExpenseDto;
import pl.lipinski.engineerdegree.dao.entity.BudgetList;
import pl.lipinski.engineerdegree.dao.entity.Expense;
import pl.lipinski.engineerdegree.manager.BudgetListManager;
import pl.lipinski.engineerdegree.manager.UserManager;
import pl.lipinski.engineerdegree.util.error.ControllerError;
import pl.lipinski.engineerdegree.util.validator.BudgetListValidator;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityNotFoundException;
import java.util.Optional;

@RestController
@RequestMapping("/api/budgetlist")
public class BudgetListController {

    private BudgetListManager budgetListManager;
    private UserManager userManager;
    private BudgetListValidator budgetListValidator;
    private ModelMapper modelMapper;


    @Autowired
    public BudgetListController(BudgetListManager budgetListManager,
                                UserManager userManager,
                                BudgetListValidator budgetListValidator) {
        this.budgetListManager = budgetListManager;
        this.userManager = userManager;
        this.budgetListValidator = budgetListValidator;
        this.modelMapper = new ModelMapper();
    }

    @GetMapping("/getall")
    public Iterable<BudgetList> getAll(){
        return budgetListManager.findAll();
    }

    @GetMapping("/getbyname")
    public Iterable<BudgetList> getByName(String name){
        return budgetListManager.findByName(name);
    }

    @GetMapping("/getbyid")
    public Optional<BudgetList> getById(Long id){
        return budgetListManager.findById(id);
    }

    @DeleteMapping("/delete")
    public void deleteById(Long id){
        budgetListManager.deleteById(id);
    }

    @PostMapping("/add")
    public ResponseEntity addBudgetList(@ModelAttribute("budgetlistform")BudgetListDto budgetListDto,
                                        BindingResult bindingResult){
        budgetListValidator.validate(budgetListDto, bindingResult);
        if(bindingResult.hasErrors()){
            ControllerError controllerError = new ControllerError(HttpStatus.BAD_REQUEST,
                    budgetListValidator.getErrorCode(),
                    budgetListValidator.getErrorMessages(bindingResult));
            return new ResponseEntity(controllerError, HttpStatus.BAD_REQUEST);
        }
        BudgetList budgetList = modelMapper.map(budgetListDto, BudgetList.class);
        budgetListManager.addBudgetList(budgetList);
        return ResponseEntity.ok(modelMapper.map(budgetList, BudgetList.class));

    }

    @PatchMapping("/edit")
    public ResponseEntity edit(@RequestParam Long id,
                               @ModelAttribute("budgetlistform") BudgetListDto budgetListDto,
                               BindingResult bindingResult) {
        Optional<BudgetList> budgetListToUpdate = budgetListManager.findById(id);
        budgetListToUpdate.orElseThrow(EntityNotFoundException::new);
        budgetListValidator.validate(budgetListDto, bindingResult);
        if(bindingResult.hasErrors()){
            ControllerError controllerError = new ControllerError(HttpStatus.BAD_REQUEST,
                    budgetListValidator.getErrorCode(),
                    budgetListValidator.getErrorMessages(bindingResult));
            return new ResponseEntity(controllerError, HttpStatus.BAD_REQUEST);
        }
        BudgetList budgetList = modelMapper.map(budgetListDto, BudgetList.class);
        budgetListToUpdate.get().setBudgetValue(budgetList.getBudgetValue());
        budgetListToUpdate.get().setName(budgetList.getName());

        budgetListManager.addBudgetList(budgetListToUpdate.get());
        return ResponseEntity.ok(budgetListToUpdate.get());
    }


}

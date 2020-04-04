package pl.lipinski.engineerdegree.api;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import pl.lipinski.engineerdegree.dao.dto.BudgetListDto;
import pl.lipinski.engineerdegree.dao.entity.BudgetList;
import pl.lipinski.engineerdegree.dao.entity.User;
import pl.lipinski.engineerdegree.dao.entity.UserBudgetListIntersection;
import pl.lipinski.engineerdegree.manager.BudgetListManager;
import pl.lipinski.engineerdegree.manager.UserBudgetListIntersectionManager;
import pl.lipinski.engineerdegree.manager.UserManager;
import pl.lipinski.engineerdegree.util.error.ControllerError;

import pl.lipinski.engineerdegree.util.validator.BudgetListValidator;

import java.util.Arrays;
import java.util.Optional;

import static pl.lipinski.engineerdegree.util.error.ERRORCODES.*;
import static pl.lipinski.engineerdegree.util.error.ERRORMESSAGES.*;

@RestController
@RequestMapping("/api/budgetlist")
public class BudgetListController {

    private BudgetListManager budgetListManager;
    private UserManager userManager;
    private BudgetListValidator budgetListValidator;
    private ModelMapper modelMapper;
    private UserBudgetListIntersectionManager userBudgetListIntersectionManager;


    @Autowired
    public BudgetListController(BudgetListManager budgetListManager,
                                UserManager userManager,
                                BudgetListValidator budgetListValidator,
                                UserBudgetListIntersectionManager userBudgetListIntersectionManager) {
        this.budgetListManager = budgetListManager;
        this.userManager = userManager;
        this.budgetListValidator = budgetListValidator;
        this.modelMapper = new ModelMapper();
        this.userBudgetListIntersectionManager = userBudgetListIntersectionManager;
    }

    @GetMapping("/getall")
    public Iterable<BudgetList> getAll(){
        return budgetListManager.findAll();
    }

    @GetMapping("/getbyname")
    public Iterable<BudgetList> getByName(@RequestParam String name){
        return budgetListManager.findByName(name);
    }

    @GetMapping("/getbyid")
    public Optional<BudgetList> getById(@RequestParam Long id){
        return budgetListManager.findById(id);
    }

    @DeleteMapping("/delete")
    public void deleteById(@RequestParam Long id){
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
        if(!budgetListToUpdate.isPresent()){
            ControllerError controllerError = new ControllerError(HttpStatus.BAD_REQUEST,
                    BUDGET_LIST_NOT_FOUND_ERROR_CODE.getValue(),
                    Arrays.asList(BUDGET_LIST_NOT_FOUND_ERROR_MESSAGE.getMessage()));
            return new ResponseEntity(controllerError, HttpStatus.BAD_REQUEST);
        }
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
        budgetListManager.editBudgetList(budgetListToUpdate.get());
        return ResponseEntity.ok(budgetListToUpdate.get());
    }

    @PatchMapping("/share")
    public ResponseEntity share(@RequestParam String username,@RequestParam Long budgetListId){
        Optional<User> user = userManager.findByUsername(username);
        if(!user.isPresent()){
            ControllerError controllerError = new ControllerError(HttpStatus.BAD_REQUEST,
                    USER_NOT_FOUND_ERROR_CODE.getValue(),
                    Arrays.asList(USER_NOT_FOUND_ERROR_MESSAGE.getMessage()));
            return new ResponseEntity(controllerError, HttpStatus.BAD_REQUEST);
        }
        Optional<BudgetList> budgetList = budgetListManager.findById(budgetListId);

        if(!budgetList.isPresent()){
            ControllerError controllerError = new ControllerError(HttpStatus.BAD_REQUEST,
                    BUDGET_LIST_NOT_FOUND_ERROR_CODE.getValue(),
                    Arrays.asList(BUDGET_LIST_NOT_FOUND_ERROR_MESSAGE.getMessage()));
            return new ResponseEntity(controllerError, HttpStatus.BAD_REQUEST);
        }

        Optional<UserBudgetListIntersection> intersection = userBudgetListIntersectionManager.
                findByIntersectionUserAndAndIntersectionBudgetList(user.get(), budgetList.get());
        if(intersection.isPresent()){
            ControllerError controllerError = new ControllerError(HttpStatus.BAD_REQUEST,
                    INTERSECTION_ALREADY_EXISTS_ERROR_CODE.getValue(),
                    Arrays.asList(INTERSECTION_ALREADY_EXISTS_ERROR_MESSAGE.getMessage()));
            return new ResponseEntity(controllerError, HttpStatus.BAD_REQUEST);
        }
        UserBudgetListIntersection intersectionToReturn = userBudgetListIntersectionManager.save(user.get(), budgetList.get());
        return ResponseEntity.ok(intersectionToReturn);
    }

    @DeleteMapping("/revoke")
    public ResponseEntity revoke(@RequestParam String username, @RequestParam Long budgetListId){
        Optional<User> user = userManager.findByUsername(username);
        if(!user.isPresent()){
            ControllerError controllerError = new ControllerError(HttpStatus.BAD_REQUEST,
                    USER_NOT_FOUND_ERROR_CODE.getValue(),
                    Arrays.asList(USER_NOT_FOUND_ERROR_MESSAGE.getMessage()));
            return new ResponseEntity(controllerError, HttpStatus.BAD_REQUEST);
        }

        Optional<BudgetList> budgetList = budgetListManager.findById(budgetListId);
        if(!budgetList.isPresent()){
            ControllerError controllerError = new ControllerError(HttpStatus.BAD_REQUEST,
                    BUDGET_LIST_NOT_FOUND_ERROR_CODE.getValue(),
                    Arrays.asList(BUDGET_LIST_NOT_FOUND_ERROR_MESSAGE.getMessage()));
            return new ResponseEntity(controllerError, HttpStatus.BAD_REQUEST);
        }

        Optional<UserBudgetListIntersection> intersection = userBudgetListIntersectionManager.
                findByIntersectionUserAndAndIntersectionBudgetList(user.get(), budgetList.get());
        if(!budgetList.isPresent()){
            ControllerError controllerError = new ControllerError(HttpStatus.BAD_REQUEST,
                    INTERSECTION_NOT_FOUND_ERROR_CODE.getValue(),
                    Arrays.asList(INTERSECTION_NOT_FOUND_ERROR_MESSAGE.getMessage()));
            return new ResponseEntity(controllerError, HttpStatus.BAD_REQUEST);
        }
        userBudgetListIntersectionManager.deleteById(intersection.get().getId());
        return ResponseEntity.ok(0);
    }

}

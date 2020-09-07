package pl.lipinski.engineerdegree.api;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import pl.lipinski.engineerdegree.dao.dto.BudgetListDto;
import pl.lipinski.engineerdegree.dao.entity.BudgetList;
import pl.lipinski.engineerdegree.dao.entity.User;
import pl.lipinski.engineerdegree.dao.entity.intersection.UserBudgetListIntersection;
import pl.lipinski.engineerdegree.manager.BudgetListManager;
import pl.lipinski.engineerdegree.manager.UserBudgetListIntersectionManager;
import pl.lipinski.engineerdegree.manager.UserManager;
import pl.lipinski.engineerdegree.util.error.ControllerError;
import pl.lipinski.engineerdegree.util.validator.BudgetListValidator;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

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
    public Iterable<BudgetList> getAll() {
        return budgetListManager.findAll();
    }

    @GetMapping("/getbyname")
    public Iterable<BudgetList> getByName(@RequestParam String name) {
        List<BudgetList> result =
                StreamSupport.stream(budgetListManager.findByName(name).spliterator(), false)
                        .collect(Collectors.toList());
        List<BudgetList> budgetListList = new ArrayList<>();
        for (BudgetList bl : result) {
            if (validatePermissions(bl)) {
                budgetListList.add(bl);
            }
        }
        return budgetListList;
    }

    @GetMapping("/getallbyuser")
    public Iterable<BudgetList> getAllByUser(@RequestParam String username) {
        List<BudgetList> result =
                StreamSupport.stream(budgetListManager.findAllByUser(username).spliterator(), false)
                        .collect(Collectors.toList());
        List<BudgetList> budgetListList = new ArrayList<>();
        for (BudgetList bl : result) {
            if (validatePermissions(bl)) {
                budgetListList.add(bl);
            }
        }
        return budgetListList;
    }

    @GetMapping("/getearliestdateforuser")
    public ResponseEntity<Map<String, String>> getEarliestForUser(@RequestParam String username){
        List<BudgetList> result =
                StreamSupport.stream(budgetListManager.findAllByUser(username).spliterator(), false)
                        .sorted(Comparator.comparing(BudgetList::getStartingDate))
                        .collect(Collectors.toList());

        if(result.size() > 0){
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            return ResponseEntity.ok(Collections.singletonMap("response", result.get(0).getStartingDate().format(formatter)));
        } else {
            LocalDate localDate = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            return ResponseEntity.ok(Collections.singletonMap("response", localDate.format(formatter)));
        }
    }

    @GetMapping("/getbyid")
    public ResponseEntity<Optional<BudgetList>> getById(@RequestParam Long id) {
        budgetListManager.findById(id).orElseThrow(EntityNotFoundException::new);
        if (!validatePermissions(budgetListManager.findById(id).get())) {
            ControllerError controllerError = new ControllerError(HttpStatus.BAD_REQUEST,
                    USER_DONT_HAVE_PERMISSIONS_ERROR_CODE.getValue(),
                    Collections.singletonList(USER_DONT_HAVE_PERMISSIONS_ERROR_MESSAGE.getMessage()));
            return new ResponseEntity(controllerError, HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity(budgetListManager.findById(id), HttpStatus.OK);
    }


    @DeleteMapping("/delete")
    public ResponseEntity deleteById(@RequestParam Long id) {
        budgetListManager.findById(id).orElseThrow(EntityNotFoundException::new);
        if (!validatePermissions(budgetListManager.findById(id).get())) {
            ControllerError controllerError = new ControllerError(HttpStatus.BAD_REQUEST,
                    USER_DONT_HAVE_PERMISSIONS_ERROR_CODE.getValue(),
                    Collections.singletonList(USER_DONT_HAVE_PERMISSIONS_ERROR_MESSAGE.getMessage()));
            return new ResponseEntity(controllerError, HttpStatus.BAD_REQUEST);
        }
        budgetListManager.deleteById(id);
        return new ResponseEntity(HttpStatus.OK);
    }

    @DeleteMapping("/deletemany")
    public ResponseEntity deleteManyById(@RequestBody ArrayList<Long> idList) {
        for (Long id : idList) {
            budgetListManager.findById(id).orElseThrow(EntityNotFoundException::new);
            if (!validatePermissions(budgetListManager.findById(id).get())) {
                ControllerError controllerError = new ControllerError(HttpStatus.BAD_REQUEST,
                        USER_DONT_HAVE_PERMISSIONS_ERROR_CODE.getValue(),
                        Collections.singletonList(USER_DONT_HAVE_PERMISSIONS_ERROR_MESSAGE.getMessage()));
                return new ResponseEntity(controllerError, HttpStatus.BAD_REQUEST);
            }
        }
        for (Long id : idList) {
            budgetListManager.deleteById(id);
        }
        return ResponseEntity.ok(null);
    }

    @PostMapping("/add")
    public ResponseEntity<BudgetList> addBudgetList(@ModelAttribute("budgetlistform") BudgetListDto budgetListDto,
                                                    BindingResult bindingResult) {
        budgetListValidator.validate(budgetListDto, bindingResult);
        if (bindingResult.hasErrors()) {
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
    public ResponseEntity<BudgetList> edit(@RequestParam Long id,
                                           @ModelAttribute("budgetlistform") BudgetListDto budgetListDto,
                                           BindingResult bindingResult) {
        Optional<BudgetList> budgetListToUpdate = budgetListManager.findById(id);
        if (!budgetListToUpdate.isPresent()) {
            ControllerError controllerError = new ControllerError(HttpStatus.BAD_REQUEST,
                    BUDGET_LIST_NOT_FOUND_ERROR_CODE.getValue(),
                    Collections.singletonList(BUDGET_LIST_NOT_FOUND_ERROR_MESSAGE.getMessage()));
            return new ResponseEntity(controllerError, HttpStatus.BAD_REQUEST);
        }
        if (!validatePermissions(budgetListToUpdate.get())) {
            ControllerError controllerError = new ControllerError(HttpStatus.BAD_REQUEST,
                    USER_DONT_HAVE_PERMISSIONS_ERROR_CODE.getValue(),
                    Collections.singletonList(USER_DONT_HAVE_PERMISSIONS_ERROR_MESSAGE.getMessage()));
            return new ResponseEntity(controllerError, HttpStatus.BAD_REQUEST);
        }
        budgetListValidator.validate(budgetListDto, bindingResult);
        if (bindingResult.hasErrors()) {
            ControllerError controllerError = new ControllerError(HttpStatus.BAD_REQUEST,
                    budgetListValidator.getErrorCode(),
                    budgetListValidator.getErrorMessages(bindingResult));
            return new ResponseEntity(controllerError, HttpStatus.BAD_REQUEST);
        }
        BudgetList budgetList = modelMapper.map(budgetListDto, BudgetList.class);
        budgetListToUpdate.get().setBudgetValue(budgetList.getBudgetValue());
        budgetListToUpdate.get().setName(budgetList.getName());
        budgetListToUpdate.get().setDueDate(budgetList.getDueDate());
        budgetListToUpdate.get().setCurrencyCode(budgetList.getCurrencyCode());
        budgetListToUpdate.get().setStartingDate(budgetList.getStartingDate());
        budgetListManager.editBudgetList(budgetListToUpdate.get());
        return ResponseEntity.ok(budgetListToUpdate.get());
    }

    @PatchMapping("/share")
    public ResponseEntity share(@RequestParam String username, @RequestParam Long budgetListId) {
        Optional<User> user = userManager.findByUsername(username);
        if (!user.isPresent()) {
            ControllerError controllerError = new ControllerError(HttpStatus.BAD_REQUEST,
                    USER_NOT_FOUND_ERROR_CODE.getValue(),
                    Collections.singletonList(USER_NOT_FOUND_ERROR_MESSAGE.getMessage()));
            return new ResponseEntity(controllerError, HttpStatus.BAD_REQUEST);
        }
        Optional<BudgetList> budgetList = budgetListManager.findById(budgetListId);

        if (!budgetList.isPresent()) {
            ControllerError controllerError = new ControllerError(HttpStatus.BAD_REQUEST,
                    BUDGET_LIST_NOT_FOUND_ERROR_CODE.getValue(),
                    Collections.singletonList(BUDGET_LIST_NOT_FOUND_ERROR_MESSAGE.getMessage()));
            return new ResponseEntity(controllerError, HttpStatus.BAD_REQUEST);
        }
        if (!validatePermissions(budgetList.get())) {
            ControllerError controllerError = new ControllerError(HttpStatus.BAD_REQUEST,
                    USER_DONT_HAVE_PERMISSIONS_ERROR_CODE.getValue(),
                    Collections.singletonList(USER_DONT_HAVE_PERMISSIONS_ERROR_MESSAGE.getMessage()));
            return new ResponseEntity(controllerError, HttpStatus.BAD_REQUEST);
        }
        Optional<UserBudgetListIntersection> intersection = userBudgetListIntersectionManager
                .findByIntersectionUserAndIntersectionBudgetList(user.get(), budgetList.get());
        if (intersection.isPresent()) {
            ControllerError controllerError = new ControllerError(HttpStatus.BAD_REQUEST,
                    BUDGET_LIST_INTERSECTION_ALREADY_EXISTS_ERROR_CODE.getValue(),
                    Collections.singletonList(BUDGET_LIST_INTERSECTION_ALREADY_EXISTS_ERROR_MESSAGE.getMessage()));
            return new ResponseEntity(controllerError, HttpStatus.BAD_REQUEST);
        }
        UserBudgetListIntersection intersectionToReturn = userBudgetListIntersectionManager.save(user.get(), budgetList.get());
        return ResponseEntity.ok(intersectionToReturn);
    }

    @PatchMapping("/sharemany")
    public ResponseEntity shareMany(@RequestBody List<String> usernameList, @RequestParam Long budgetListId) {
        Optional<BudgetList> budgetList = budgetListManager.findById(budgetListId);
        if (!budgetList.isPresent()) {
            ControllerError controllerError = new ControllerError(HttpStatus.BAD_REQUEST,
                    BUDGET_LIST_NOT_FOUND_ERROR_CODE.getValue(),
                    Collections.singletonList(BUDGET_LIST_NOT_FOUND_ERROR_MESSAGE.getMessage()));
            return new ResponseEntity(controllerError, HttpStatus.BAD_REQUEST);
        }
        if (!validatePermissions(budgetList.get())) {
            ControllerError controllerError = new ControllerError(HttpStatus.BAD_REQUEST,
                    USER_DONT_HAVE_PERMISSIONS_ERROR_CODE.getValue(),
                    Collections.singletonList(USER_DONT_HAVE_PERMISSIONS_ERROR_MESSAGE.getMessage()));
            return new ResponseEntity(controllerError, HttpStatus.BAD_REQUEST);
        }
        ArrayList<User> users = new ArrayList<>();
        for (String username : usernameList) {
            Optional<User> user = userManager.findByUsername(username);
            if (!user.isPresent()) {
                ControllerError controllerError = new ControllerError(HttpStatus.BAD_REQUEST,
                        USER_NOT_FOUND_ERROR_CODE.getValue(),
                        Collections.singletonList(USER_NOT_FOUND_ERROR_MESSAGE.getMessage()));
                return new ResponseEntity(controllerError, HttpStatus.BAD_REQUEST);
            }
            Optional<UserBudgetListIntersection> intersection = userBudgetListIntersectionManager
                    .findByIntersectionUserAndIntersectionBudgetList(user.get(), budgetList.get());
            if (intersection.isPresent()) {
                ControllerError controllerError = new ControllerError(HttpStatus.BAD_REQUEST,
                        BUDGET_LIST_INTERSECTION_ALREADY_EXISTS_ERROR_CODE.getValue(),
                        Collections.singletonList(BUDGET_LIST_INTERSECTION_ALREADY_EXISTS_ERROR_MESSAGE.getMessage()));
                return new ResponseEntity(controllerError, HttpStatus.BAD_REQUEST);
            }
            users.add(user.get());
        }
        users.forEach(user -> {
            userBudgetListIntersectionManager.save(user, budgetList.get());
        });
        return ResponseEntity.ok(null);
    }

    @DeleteMapping("/revoke")
    public ResponseEntity revoke(@RequestParam String username, @RequestParam Long budgetListId) {
        Optional<User> user = userManager.findByUsername(username);
        if (!user.isPresent()) {
            ControllerError controllerError = new ControllerError(HttpStatus.BAD_REQUEST,
                    USER_NOT_FOUND_ERROR_CODE.getValue(),
                    Collections.singletonList(USER_NOT_FOUND_ERROR_MESSAGE.getMessage()));
            return new ResponseEntity(controllerError, HttpStatus.BAD_REQUEST);
        }

        Optional<BudgetList> budgetList = budgetListManager.findById(budgetListId);
        if (!budgetList.isPresent()) {
            ControllerError controllerError = new ControllerError(HttpStatus.BAD_REQUEST,
                    BUDGET_LIST_NOT_FOUND_ERROR_CODE.getValue(),
                    Collections.singletonList(BUDGET_LIST_NOT_FOUND_ERROR_MESSAGE.getMessage()));
            return new ResponseEntity(controllerError, HttpStatus.BAD_REQUEST);
        }
        if (!validatePermissions(budgetList.get())) {
            ControllerError controllerError = new ControllerError(HttpStatus.BAD_REQUEST,
                    USER_DONT_HAVE_PERMISSIONS_ERROR_CODE.getValue(),
                    Collections.singletonList(USER_DONT_HAVE_PERMISSIONS_ERROR_MESSAGE.getMessage()));
            return new ResponseEntity(controllerError, HttpStatus.BAD_REQUEST);
        }
        Optional<UserBudgetListIntersection> intersection = userBudgetListIntersectionManager.
                findByIntersectionUserAndIntersectionBudgetList(user.get(), budgetList.get());
        if (!intersection.isPresent()) {
            ControllerError controllerError = new ControllerError(HttpStatus.BAD_REQUEST,
                    INTERSECTION_NOT_FOUND_ERROR_CODE.getValue(),
                    Collections.singletonList(INTERSECTION_NOT_FOUND_ERROR_MESSAGE.getMessage()));
            return new ResponseEntity(controllerError, HttpStatus.BAD_REQUEST);
        }

        userBudgetListIntersectionManager.deleteById(intersection.get().getId());
        return ResponseEntity.ok(0);
    }

    @DeleteMapping("/revokemany")
    public ResponseEntity revokeMany(@RequestBody List<String> usernameList, @RequestParam Long budgetListId) {
        Optional<BudgetList> budgetList = budgetListManager.findById(budgetListId);
        if (!budgetList.isPresent()) {
            ControllerError controllerError = new ControllerError(HttpStatus.BAD_REQUEST,
                    BUDGET_LIST_NOT_FOUND_ERROR_CODE.getValue(),
                    Collections.singletonList(BUDGET_LIST_NOT_FOUND_ERROR_MESSAGE.getMessage()));
            return new ResponseEntity(controllerError, HttpStatus.BAD_REQUEST);
        }
        if (!validatePermissions(budgetList.get())) {
            ControllerError controllerError = new ControllerError(HttpStatus.BAD_REQUEST,
                    USER_DONT_HAVE_PERMISSIONS_ERROR_CODE.getValue(),
                    Collections.singletonList(USER_DONT_HAVE_PERMISSIONS_ERROR_MESSAGE.getMessage()));
            return new ResponseEntity(controllerError, HttpStatus.BAD_REQUEST);
        }
        ArrayList<UserBudgetListIntersection> intersections = new ArrayList<>();
        for (String username : usernameList) {
            Optional<User> user = userManager.findByUsername(username);
            if (!user.isPresent()) {
                ControllerError controllerError = new ControllerError(HttpStatus.BAD_REQUEST,
                        USER_NOT_FOUND_ERROR_CODE.getValue(),
                        Collections.singletonList(USER_NOT_FOUND_ERROR_MESSAGE.getMessage()));
                return new ResponseEntity(controllerError, HttpStatus.BAD_REQUEST);
            }
            Optional<UserBudgetListIntersection> intersection = userBudgetListIntersectionManager.
                    findByIntersectionUserAndIntersectionBudgetList(user.get(), budgetList.get());
            if (!intersection.isPresent()) {
                ControllerError controllerError = new ControllerError(HttpStatus.BAD_REQUEST,
                        INTERSECTION_NOT_FOUND_ERROR_CODE.getValue(),
                        Collections.singletonList(INTERSECTION_NOT_FOUND_ERROR_MESSAGE.getMessage()));
                return new ResponseEntity(controllerError, HttpStatus.BAD_REQUEST);
            }
            intersections.add(intersection.get());
        }

        for (UserBudgetListIntersection intersection : intersections) {
            userBudgetListIntersectionManager.deleteById(intersection.getId());
        }

        return ResponseEntity.ok(null);
    }

    private boolean validatePermissions(BudgetList budgetList) {
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<User> user = userManager.findByUsername(name);
        if (!user.isPresent()) {
            return false;
        }
        return userBudgetListIntersectionManager.findByIntersectionUserAndIntersectionBudgetList(user.get(), budgetList)
                .isPresent() || user.get().getRoles().equals("ROLE_ADMIN");
    }

}

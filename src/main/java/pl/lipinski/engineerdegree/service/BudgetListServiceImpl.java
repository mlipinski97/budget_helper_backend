package pl.lipinski.engineerdegree.service;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import pl.lipinski.engineerdegree.dao.dto.BudgetListDto;
import pl.lipinski.engineerdegree.dao.dto.StringResponseDto;
import pl.lipinski.engineerdegree.dao.entity.BudgetList;
import pl.lipinski.engineerdegree.dao.entity.User;
import pl.lipinski.engineerdegree.dao.entity.intersection.UserBudgetListIntersection;
import pl.lipinski.engineerdegree.dao.repository.BudgetListRepo;
import pl.lipinski.engineerdegree.util.error.ControllerError;
import pl.lipinski.engineerdegree.util.validator.BudgetListValidator;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static pl.lipinski.engineerdegree.util.error.ErrorCodes.*;
import static pl.lipinski.engineerdegree.util.error.ErrorMessages.*;

@Service
public class BudgetListServiceImpl implements BudgetListService {

    private final BudgetListRepo budgetListRepo;
    private final UserService userService;
    private final BudgetListValidator budgetListValidator;
    private final ModelMapper modelMapper;
    private final UserBudgetListIntersectionManager intersectionManager;
    private final UserBudgetListIntersectionManager userBudgetListIntersectionManager;

    @Autowired
    public BudgetListServiceImpl(BudgetListRepo budgetListRepo,
                                 UserService userService,
                                 BudgetListValidator budgetListValidator, UserBudgetListIntersectionManager intersectionManager,
                                 UserBudgetListIntersectionManager userBudgetListIntersectionManager) {
        this.budgetListRepo = budgetListRepo;
        this.userService = userService;
        this.budgetListValidator = budgetListValidator;
        this.intersectionManager = intersectionManager;
        this.userBudgetListIntersectionManager = userBudgetListIntersectionManager;
        this.modelMapper = new ModelMapper();
    }


    public Iterable<BudgetList> findAll() {
        return budgetListRepo.findAll();
    }

    public Iterable<BudgetList> findByName(String name) {
        return budgetListRepo.findByName(name);
    }

    public Iterable<BudgetList> getByName(String name) {
        List<BudgetList> result =
                StreamSupport.stream(budgetListRepo.findByName(name).spliterator(), false)
                        .collect(Collectors.toList());
        List<BudgetList> budgetListList = new ArrayList<>();
        for (BudgetList bl : result) {
            if (validatePermissions(bl)) {
                budgetListList.add(bl);
            }
        }
        return budgetListList;
    }

    public Iterable<BudgetList> getAllByUser(String username) {
        List<BudgetList> result =
                StreamSupport.stream(findAllByUser(username).spliterator(), false)
                        .collect(Collectors.toList());
        List<BudgetList> budgetListList = new ArrayList<>();
        for (BudgetList bl : result) {
            if (validatePermissions(bl)) {
                budgetListList.add(bl);
            }
        }
        return budgetListList;
    }

    public ResponseEntity<?> getEarliestForUser(String username) {
        List<BudgetList> result =
                StreamSupport.stream(findAllByUser(username).spliterator(), false)
                        .sorted(Comparator.comparing(BudgetList::getStartingDate))
                        .collect(Collectors.toList());

        if (result.size() > 0) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            return ResponseEntity.ok(new StringResponseDto(result.get(0).getStartingDate().format(formatter)));
        } else {
            LocalDate localDate = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            return ResponseEntity.ok(new StringResponseDto(localDate.format(formatter)));
        }
    }

    public ResponseEntity<?> getById(Long id) {
        budgetListRepo.findById(id).orElseThrow(EntityNotFoundException::new);
        if (!validatePermissions(budgetListRepo.findById(id).get())) {
            ControllerError controllerError = new ControllerError(HttpStatus.BAD_REQUEST,
                    USER_DONT_HAVE_PERMISSIONS_ERROR_CODE.getValue(),
                    Collections.singletonList(USER_DONT_HAVE_PERMISSIONS_ERROR_MESSAGE.getMessage()));
            return new ResponseEntity(controllerError, HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity(budgetListRepo.findById(id), HttpStatus.OK);
    }

    public ResponseEntity<?> deleteById(Long id) {
        budgetListRepo.findById(id).orElseThrow(EntityNotFoundException::new);
        if (!validatePermissions(budgetListRepo.findById(id).get())) {
            ControllerError controllerError = new ControllerError(HttpStatus.BAD_REQUEST,
                    USER_DONT_HAVE_PERMISSIONS_ERROR_CODE.getValue(),
                    Collections.singletonList(USER_DONT_HAVE_PERMISSIONS_ERROR_MESSAGE.getMessage()));
            return new ResponseEntity(controllerError, HttpStatus.BAD_REQUEST);
        }
        budgetListRepo.deleteById(id);
        return new ResponseEntity(HttpStatus.OK);
    }

    public ResponseEntity<?> deleteManyById(ArrayList<Long> idList) {
        for (Long id : idList) {
            budgetListRepo.findById(id).orElseThrow(EntityNotFoundException::new);
            if (!validatePermissions(budgetListRepo.findById(id).get())) {
                ControllerError controllerError = new ControllerError(HttpStatus.BAD_REQUEST,
                        USER_DONT_HAVE_PERMISSIONS_ERROR_CODE.getValue(),
                        Collections.singletonList(USER_DONT_HAVE_PERMISSIONS_ERROR_MESSAGE.getMessage()));
                return new ResponseEntity(controllerError, HttpStatus.BAD_REQUEST);
            }
        }
        for (Long id : idList) {
            budgetListRepo.deleteById(id);
        }
        return ResponseEntity.ok(null);
    }

    public ResponseEntity<?> addBudgetList(BudgetListDto budgetListDto,
                                           BindingResult bindingResult) {
        budgetListValidator.validate(budgetListDto, bindingResult);
        if (bindingResult.hasErrors()) {
            ControllerError controllerError = new ControllerError(HttpStatus.BAD_REQUEST,
                    budgetListValidator.getErrorCode(),
                    budgetListValidator.getErrorMessages(bindingResult));
            return new ResponseEntity(controllerError, HttpStatus.BAD_REQUEST);
        }
        BudgetList budgetList = modelMapper.map(budgetListDto, BudgetList.class);
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<User> user = userService.findByUsername(name);
        user.orElseThrow(EntityNotFoundException::new);
        budgetList.setRemainingValue(budgetList.getBudgetValue());
        budgetListRepo.save(budgetList);
        intersectionManager.save(user.get(), budgetList);
        return ResponseEntity.ok(modelMapper.map(budgetList, BudgetList.class));
    }

    public ResponseEntity<?> edit(Long id,
                                  BudgetListDto budgetListDto,
                                  BindingResult bindingResult) {
        Optional<BudgetList> budgetListToUpdate = budgetListRepo.findById(id);
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
        budgetListRepo.save(budgetListToUpdate.get());
        return ResponseEntity.ok(budgetListToUpdate.get());
    }

    public ResponseEntity<?> share(String username, Long budgetListId) {
        Optional<User> user = userService.findByUsername(username);
        if (!user.isPresent()) {
            ControllerError controllerError = new ControllerError(HttpStatus.BAD_REQUEST,
                    USER_NOT_FOUND_ERROR_CODE.getValue(),
                    Collections.singletonList(USER_NOT_FOUND_ERROR_MESSAGE.getMessage()));
            return new ResponseEntity(controllerError, HttpStatus.BAD_REQUEST);
        }
        Optional<BudgetList> budgetList = budgetListRepo.findById(budgetListId);

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

    public ResponseEntity<?> shareMany(List<String> usernameList, Long budgetListId) {
        Optional<BudgetList> budgetList = budgetListRepo.findById(budgetListId);
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
            Optional<User> user = userService.findByUsername(username);
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
        users.forEach(user -> userBudgetListIntersectionManager.save(user, budgetList.get()));
        return ResponseEntity.ok(null);
    }

    public ResponseEntity<?> revoke(String username, Long budgetListId) {
        Optional<User> user = userService.findByUsername(username);
        if (!user.isPresent()) {
            ControllerError controllerError = new ControllerError(HttpStatus.BAD_REQUEST,
                    USER_NOT_FOUND_ERROR_CODE.getValue(),
                    Collections.singletonList(USER_NOT_FOUND_ERROR_MESSAGE.getMessage()));
            return new ResponseEntity(controllerError, HttpStatus.BAD_REQUEST);
        }

        Optional<BudgetList> budgetList = budgetListRepo.findById(budgetListId);
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

    private Iterable<BudgetList> findAllByUser(String username) {
        Optional<User> user = userService.findByUsername(username);
        user.orElseThrow(EntityNotFoundException::new);
        Iterable<UserBudgetListIntersection> intersections = intersectionManager.findAllByIntersectionUser(user.get());
        List<BudgetList> budgetLists = new ArrayList<>();
        for (UserBudgetListIntersection intersection : intersections) {
            budgetLists.add(intersection.getIntersectionBudgetList());
        }
        return budgetLists;
    }

    private boolean validatePermissions(BudgetList budgetList) {
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<User> user = userService.findByUsername(name);
        if (!user.isPresent()) {
            return false;
        }
        return userBudgetListIntersectionManager.findByIntersectionUserAndIntersectionBudgetList(user.get(), budgetList)
                .isPresent() || user.get().getRoles().equals("ROLE_ADMIN");
    }

    public ResponseEntity<?> revokeMany(List<String> usernameList, Long budgetListId) {
        Optional<BudgetList> budgetList = budgetListRepo.findById(budgetListId);
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
            Optional<User> user = userService.findByUsername(username);
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
}

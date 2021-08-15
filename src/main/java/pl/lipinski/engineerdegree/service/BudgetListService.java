package pl.lipinski.engineerdegree.service;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import pl.lipinski.engineerdegree.dao.dto.BudgetListDto;
import pl.lipinski.engineerdegree.dao.entity.BudgetList;

import java.util.ArrayList;
import java.util.List;

public interface BudgetListService {

    Iterable<BudgetList> findAll();

    Iterable<BudgetList> getByName(String name);

    Iterable<BudgetList> getAllByUser(String username);

    ResponseEntity<?> getEarliestForUser(String username);

    ResponseEntity<?> getById(Long id);

    ResponseEntity<?> deleteById(Long id);

    ResponseEntity<?> deleteManyById(ArrayList<Long> idList);

    ResponseEntity<?> addBudgetList(BudgetListDto budgetListDto, BindingResult bindingResult);

    ResponseEntity<?> edit(Long id, BudgetListDto budgetListDto, BindingResult bindingResult);

    ResponseEntity<?> share(String username, Long budgetListId);

    ResponseEntity<?> shareMany(List<String> usernameList, Long budgetListId);

    ResponseEntity<?> revoke(String username, Long budgetListId);

    ResponseEntity<?> revokeMany(List<String> usernameList, Long budgetListId);
}

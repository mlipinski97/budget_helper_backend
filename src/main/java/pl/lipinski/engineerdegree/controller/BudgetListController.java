package pl.lipinski.engineerdegree.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import pl.lipinski.engineerdegree.dao.dto.BudgetListDto;
import pl.lipinski.engineerdegree.dao.entity.BudgetList;
import pl.lipinski.engineerdegree.service.BudgetListService;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/budgetlist")
public class BudgetListController {

    private final BudgetListService budgetListService;

    public BudgetListController(BudgetListService budgetListService) {
        this.budgetListService = budgetListService;
    }

    @GetMapping("/getall")
    public Iterable<BudgetList> getAll() {
        return budgetListService.findAll();
    }

    @GetMapping("/getbyname")
    public Iterable<BudgetList> getByName(@RequestParam String name) {
        return budgetListService.getByName(name);
    }

    @GetMapping("/getallbyuser")
    public Iterable<BudgetList> getAllByUser(@RequestParam String username) {
        return budgetListService.getAllByUser(username);
    }

    @GetMapping("/getearliestdateforuser")
    public ResponseEntity<?> getEarliestForUser(@RequestParam String username) {
        return budgetListService.getEarliestForUser(username);
    }

    @GetMapping("/getbyid")
    public ResponseEntity<?> getById(@RequestParam Long id) {
        return budgetListService.getById(id);
    }


    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteById(@RequestParam Long id) {
        return budgetListService.deleteById(id);
    }

    @DeleteMapping("/deletemany")
    public ResponseEntity<?> deleteManyById(@RequestBody ArrayList<Long> idList) {
        return budgetListService.deleteManyById(idList);
    }

    @PostMapping("/add")
    public ResponseEntity<?> addBudgetList(@ModelAttribute("budgetlistform") BudgetListDto budgetListDto,
                                           BindingResult bindingResult) {
        return budgetListService.addBudgetList(budgetListDto, bindingResult);
    }

    @PatchMapping("/edit")
    public ResponseEntity<?> edit(@RequestParam Long id,
                                  @ModelAttribute("budgetlistform") BudgetListDto budgetListDto,
                                  BindingResult bindingResult) {
        return budgetListService.edit(id, budgetListDto, bindingResult);
    }

    @PatchMapping("/share")
    public ResponseEntity<?> share(@RequestParam String username, @RequestParam Long budgetListId) {
        return budgetListService.share(username, budgetListId);
    }

    @PatchMapping("/sharemany")
    public ResponseEntity<?> shareMany(@RequestBody List<String> usernameList, @RequestParam Long budgetListId) {
        return budgetListService.shareMany(usernameList, budgetListId);
    }

    @DeleteMapping("/revoke")
    public ResponseEntity<?> revoke(@RequestParam String username, @RequestParam Long budgetListId) {
        return budgetListService.revoke(username, budgetListId);
    }

    @DeleteMapping("/revokemany")
    public ResponseEntity<?> revokeMany(@RequestBody List<String> usernameList, @RequestParam Long budgetListId) {
        return budgetListService.revokeMany(usernameList, budgetListId);
    }

}

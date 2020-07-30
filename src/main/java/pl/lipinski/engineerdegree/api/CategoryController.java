package pl.lipinski.engineerdegree.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import pl.lipinski.engineerdegree.dao.dto.BudgetListDto;
import pl.lipinski.engineerdegree.dao.entity.BudgetList;
import pl.lipinski.engineerdegree.dao.entity.Category;
import pl.lipinski.engineerdegree.manager.CategoryManager;
import pl.lipinski.engineerdegree.util.error.ControllerError;

@RestController
    @RequestMapping("/api/category")
public class CategoryController {

    CategoryManager categoryManager;

    @Autowired
    public CategoryController(CategoryManager categoryManager) {
        this.categoryManager = categoryManager;
    }

    @GetMapping("/getall")
    public Iterable<Category> getAll(){
        return categoryManager.findAll();
    }

    @PostMapping("/add")
    public ResponseEntity<Category> addCategory(@RequestParam String categoryName){
        Category category = new Category();
        category.setCategoryName(categoryName);
        return ResponseEntity.ok(categoryManager.add(category));
    }
}

package pl.lipinski.engineerdegree.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pl.lipinski.engineerdegree.dao.entity.Category;
import pl.lipinski.engineerdegree.service.CategoryService;
import pl.lipinski.engineerdegree.service.UserService;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/category")
public class CategoryController {

    CategoryService categoryService;
    UserService userService;

    @Autowired
    public CategoryController(CategoryService categoryService, UserService userService) {
        this.userService = userService;
        this.categoryService = categoryService;
    }

    @Transactional
    @GetMapping("/getall")
    public Iterable<Category> getAll() {
        return categoryService.findAll();
    }

    @Transactional
    @GetMapping("/getbyname")
    public Optional<Category> getByName(@RequestParam String categoryName) {
        return categoryService.getByName(categoryName);
    }

    @Transactional
    @PostMapping("/add")
    public ResponseEntity<?> addCategory(@RequestParam String categoryName,
                                         @RequestPart(required = false) MultipartFile categoryImage) {
        return categoryService.addCategory(categoryName, categoryImage);
    }

    @Transactional
    @PatchMapping("/edit")
    public ResponseEntity<?> edit(@RequestParam String oldCategoryName,
                                  @RequestParam String newCategoryName,
                                  @RequestPart(required = false) MultipartFile categoryImage) {
        return categoryService.edit(oldCategoryName, newCategoryName, categoryImage);
    }

    @Transactional
    @DeleteMapping("/delete")
    public ResponseEntity<?> delete(@RequestParam String categoryName) {
        return categoryService.delete(categoryName);
    }

    @Transactional
    @DeleteMapping("/deletemany")
    public ResponseEntity<?> delete(@RequestBody List<String> nameList) {
        return categoryService.deleteMany(nameList);
    }


}

package pl.lipinski.engineerdegree.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pl.lipinski.engineerdegree.dao.entity.BudgetList;
import pl.lipinski.engineerdegree.dao.entity.Category;
import pl.lipinski.engineerdegree.dao.entity.User;
import pl.lipinski.engineerdegree.manager.CategoryManager;
import pl.lipinski.engineerdegree.manager.UserManager;
import pl.lipinski.engineerdegree.util.error.ControllerError;

import javax.persistence.EntityNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

import static pl.lipinski.engineerdegree.util.error.ERRORCODES.*;
import static pl.lipinski.engineerdegree.util.error.ERRORMESSAGES.*;

@RestController
@RequestMapping("/api/category")
public class CategoryController {

    CategoryManager categoryManager;
    UserManager userManager;

    @Autowired
    public CategoryController(CategoryManager categoryManager, UserManager userManager) {
        this.userManager = userManager;
        this.categoryManager = categoryManager;
    }

    @GetMapping("/getall")
    public Iterable<Category> getAll() {
        return categoryManager.findAll();
    }

    @GetMapping("/getbyname")
    public Optional<Category> getByName(@RequestParam String categoryName) {
        return categoryManager.findByName(categoryName);
    }

    @PostMapping("/add")
    public ResponseEntity<Category> addCategory(@RequestParam String categoryName,
                                                @RequestPart(required = false) MultipartFile categoryImage) {
        Category category = new Category();
        category.setCategoryName(categoryName);
        if (categoryImage != null) {
            String fileName = StringUtils.cleanPath(categoryImage.getName());
            if (fileName.contains("..")) {
                ControllerError controllerError = new ControllerError(HttpStatus.BAD_REQUEST,
                        IMAGE_EXTENSION_NOT_VALID_ERROR_CODE.getValue(),
                        Collections.singletonList(IMAGE_EXTENSION_NOT_VALID_ERROR_MESSAGE.getMessage()));
                return new ResponseEntity(controllerError, HttpStatus.BAD_REQUEST);
            }

            try {
                category.setCategoryImage(categoryImage.getBytes());
            } catch (IOException e) {
                ControllerError controllerError = new ControllerError(HttpStatus.BAD_REQUEST,
                        UNABLE_TO_GET_BYTES_FROM_IMAGE_ERROR_CODE.getValue(),
                        Collections.singletonList(UNABLE_TO_GET_BYTES_FROM_IMAGE_ERROR_MESSAGE.getMessage()));
                return new ResponseEntity(controllerError, HttpStatus.BAD_REQUEST);
            }
        }
        return ResponseEntity.ok(categoryManager.add(category));
    }

    @PatchMapping("/edit")
    public ResponseEntity edit(@RequestParam String oldCategoryName,
                               @RequestParam String newCategoryName,
                               @RequestPart(required = false) MultipartFile categoryImage) {
        Optional<Category> categoryToUpdate = categoryManager.findByName(oldCategoryName);
        if (!categoryToUpdate.isPresent()) {
            ControllerError controllerError = new ControllerError(HttpStatus.BAD_REQUEST,
                    CATEGORY_NOT_FOUND_ERROR_CODE.getValue(),
                    Collections.singletonList(CATEGORY_NOT_FOUND_ERROR_MESSAGE.getMessage()));
            return new ResponseEntity(controllerError, HttpStatus.BAD_REQUEST);
        }
        Category newCategory = new Category();
        newCategory.setCategoryName(newCategoryName);
        if(categoryImage != null){
            String fileName = StringUtils.cleanPath(categoryImage.getName());
            if (fileName.contains("..")) {
                ControllerError controllerError = new ControllerError(HttpStatus.BAD_REQUEST,
                        IMAGE_EXTENSION_NOT_VALID_ERROR_CODE.getValue(),
                        Collections.singletonList(IMAGE_EXTENSION_NOT_VALID_ERROR_MESSAGE.getMessage()));
                return new ResponseEntity(controllerError, HttpStatus.BAD_REQUEST);
            }
            try {
                newCategory.setCategoryImage(categoryImage.getBytes());
            } catch (IOException e) {
                ControllerError controllerError = new ControllerError(HttpStatus.BAD_REQUEST,
                        UNABLE_TO_GET_BYTES_FROM_IMAGE_ERROR_CODE.getValue(),
                        Collections.singletonList(UNABLE_TO_GET_BYTES_FROM_IMAGE_ERROR_MESSAGE.getMessage()));
                return new ResponseEntity(controllerError, HttpStatus.BAD_REQUEST);
            }
        }
        categoryManager.editCategory(categoryToUpdate.get(), newCategory);
        return ResponseEntity.ok(newCategory);
    }

    @DeleteMapping("/delete")
    public ResponseEntity delete(@RequestParam String categoryName) {
        categoryManager.findByName(categoryName).orElseThrow(EntityNotFoundException::new);
        if (!validatePermissions()) {
            ControllerError controllerError = new ControllerError(HttpStatus.BAD_REQUEST,
                    USER_DONT_HAVE_PERMISSIONS_ERROR_CODE.getValue(),
                    Collections.singletonList(USER_DONT_HAVE_PERMISSIONS_ERROR_MESSAGE.getMessage()));
            return new ResponseEntity(controllerError, HttpStatus.BAD_REQUEST);
        }
        categoryManager.remove(categoryManager.findByName(categoryName).get());
        return new ResponseEntity(HttpStatus.OK);
    }

    private boolean validatePermissions() {
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<User> user = userManager.findByUsername(name);
        if (!user.isPresent()) {
            return false;
        }
        return user.get().getRoles().equals("ROLE_ADMIN");
    }
}

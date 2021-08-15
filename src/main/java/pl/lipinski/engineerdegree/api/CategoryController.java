package pl.lipinski.engineerdegree.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pl.lipinski.engineerdegree.dao.entity.Category;
import pl.lipinski.engineerdegree.dao.entity.User;
import pl.lipinski.engineerdegree.service.CategoryManager;
import pl.lipinski.engineerdegree.service.UserService;
import pl.lipinski.engineerdegree.util.error.ControllerError;

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static pl.lipinski.engineerdegree.util.error.ErrorCodes.*;
import static pl.lipinski.engineerdegree.util.error.ErrorMessages.*;

@RestController
@RequestMapping("/api/category")
public class CategoryController {

    CategoryManager categoryManager;
    UserService userService;

    @Autowired
    public CategoryController(CategoryManager categoryManager, UserService userService) {
        this.userService = userService;
        this.categoryManager = categoryManager;
    }

    @Transactional
    @GetMapping("/getall")
    public Iterable<Category> getAll() {
        return categoryManager.findAll();
    }

    @Transactional
    @GetMapping("/getbyname")
    public Optional<Category> getByName(@RequestParam String categoryName) {
        return categoryManager.findByName(categoryName);
    }

    @Transactional
    @PostMapping("/add")
    public ResponseEntity<Category> addCategory(@RequestParam String categoryName,
                                                @RequestPart(required = false) MultipartFile categoryImage) {
        Optional<Category> possibleDuplicate = categoryManager.findByName(categoryName);
        if(possibleDuplicate.isPresent()){
            ControllerError controllerError = new ControllerError(HttpStatus.BAD_REQUEST,
                    CATEGORY_ALREADY_EXISTS_ERROR_CODE.getValue(),
                    Collections.singletonList(CATEGORY_ALREADY_EXISTS_ERROR_MESSAGE.getMessage()));
            return new ResponseEntity(controllerError, HttpStatus.BAD_REQUEST);
        }
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

    @Transactional
    @PatchMapping("/edit")
    public ResponseEntity edit(@RequestParam String oldCategoryName,
                               @RequestParam String newCategoryName,
                               @RequestPart(required = false) MultipartFile categoryImage) {
        Optional<Category> possibleDuplicate = categoryManager.findByName(newCategoryName);
        if(possibleDuplicate.isPresent() && !oldCategoryName.equals(newCategoryName)){
            ControllerError controllerError = new ControllerError(HttpStatus.BAD_REQUEST,
                    CATEGORY_ALREADY_EXISTS_ERROR_CODE.getValue(),
                    Collections.singletonList(CATEGORY_ALREADY_EXISTS_ERROR_MESSAGE.getMessage()));
            return new ResponseEntity(controllerError, HttpStatus.BAD_REQUEST);
        }
        Optional<Category> categoryToUpdate = categoryManager.findByName(oldCategoryName);
        if (!categoryToUpdate.isPresent()) {
            ControllerError controllerError = new ControllerError(HttpStatus.BAD_REQUEST,
                    CATEGORY_NOT_FOUND_ERROR_CODE.getValue(),
                    Collections.singletonList(CATEGORY_NOT_FOUND_ERROR_MESSAGE.getMessage()));
            return new ResponseEntity(controllerError, HttpStatus.BAD_REQUEST);
        }
        categoryToUpdate.get().setCategoryName(newCategoryName);
        if(categoryImage != null){
            String fileName = StringUtils.cleanPath(categoryImage.getName());
            if (fileName.contains("..")) {
                ControllerError controllerError = new ControllerError(HttpStatus.BAD_REQUEST,
                        IMAGE_EXTENSION_NOT_VALID_ERROR_CODE.getValue(),
                        Collections.singletonList(IMAGE_EXTENSION_NOT_VALID_ERROR_MESSAGE.getMessage()));
                return new ResponseEntity(controllerError, HttpStatus.BAD_REQUEST);
            }
            try {
                categoryToUpdate.get().setCategoryImage(categoryImage.getBytes());
            } catch (IOException e) {
                ControllerError controllerError = new ControllerError(HttpStatus.BAD_REQUEST,
                        UNABLE_TO_GET_BYTES_FROM_IMAGE_ERROR_CODE.getValue(),
                        Collections.singletonList(UNABLE_TO_GET_BYTES_FROM_IMAGE_ERROR_MESSAGE.getMessage()));
                return new ResponseEntity(controllerError, HttpStatus.BAD_REQUEST);
            }
        }
        categoryManager.editCategory(categoryToUpdate.get());
        return ResponseEntity.ok(categoryToUpdate.get());
    }

    @Transactional
    @DeleteMapping("/delete")
    public ResponseEntity delete(@RequestParam String categoryName) {
        Category categoryToUpdate = categoryManager.findByName(categoryName).orElseThrow(EntityNotFoundException::new);
        if (!validateAdminPermissions()) {
            ControllerError controllerError = new ControllerError(HttpStatus.BAD_REQUEST,
                    USER_DONT_HAVE_PERMISSIONS_ERROR_CODE.getValue(),
                    Collections.singletonList(USER_DONT_HAVE_PERMISSIONS_ERROR_MESSAGE.getMessage()));
            return new ResponseEntity(controllerError, HttpStatus.BAD_REQUEST);
        }
        categoryToUpdate.setDeleted(true);
        categoryManager.editCategory(categoryToUpdate);
        return new ResponseEntity(HttpStatus.OK);
    }

    @Transactional
    @DeleteMapping("/deletemany")
    public ResponseEntity delete(@RequestBody List<String> nameList) {
        for (String name : nameList) {
            Category categoryToUpdate = categoryManager.findByName(name).orElseThrow(EntityNotFoundException::new);
            if (!validateAdminPermissions()) {
                ControllerError controllerError = new ControllerError(HttpStatus.BAD_REQUEST,
                        USER_DONT_HAVE_PERMISSIONS_ERROR_CODE.getValue(),
                        Collections.singletonList(USER_DONT_HAVE_PERMISSIONS_ERROR_MESSAGE.getMessage()));
                return new ResponseEntity(controllerError, HttpStatus.BAD_REQUEST);
            }
            categoryToUpdate.setDeleted(true);
            categoryManager.editCategory(categoryToUpdate);
        }
        return new ResponseEntity(HttpStatus.OK);
    }

    private boolean validateAdminPermissions() {
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<User> user = userService.findByUsername(name);
        if (!user.isPresent()) {
            return false;
        }
        return user.get().getRoles().equals("ROLE_ADMIN");
    }
}

package pl.lipinski.engineerdegree.util.validator;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.*;
import pl.lipinski.engineerdegree.dao.entity.User;
import pl.lipinski.engineerdegree.manager.UserManager;

import java.util.ArrayList;
import java.util.List;

@Component
public class UserRegistrationValidator implements Validator {

    private int errorCode;
    private final UserManager userManager;
    protected final ModelMapper modelMapper;

    @Autowired
    public UserRegistrationValidator(UserManager userManager) {
        this.userManager = userManager;
        this.modelMapper = new ModelMapper();
    }

    @Override
    public boolean supports(Class<?> aClass) {
        return User.class.equals(aClass);
    }


    @Override
    public void validate(Object o, Errors errors) {

        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "username", "username is empty!");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "password", "password is empty!");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "passwordConfirmation", "passwordConfirmation is empty");

        if (errors.hasErrors()) {
            errorCode = 500;
            return;
        }

        User user = modelMapper.map(o, User.class);

        if (!user.getPassword().equals(user.getPasswordConfirmation())) {
            errorCode = 501;
            errors.rejectValue("password","passowords does not match!");
            return;
        }
        if (userManager.findByUsername(user.getUsername()).isPresent()) {
            errorCode = 502;
            errors.rejectValue("username","This username is already registered!");
            return;
        }
    }

    public int getErrorCode() {
        return errorCode;
    }

    public List<String> getErrorMessages(BindingResult bindingResult) {
        List<String> errorList = new ArrayList<>();
        List<FieldError> fieldErrorList = bindingResult.getFieldErrors();
        for (FieldError fieldError : fieldErrorList) {
            errorList.add(fieldError.getObjectName() + " with error code: " + fieldError.getCode());
        }
        return errorList;
    }
}

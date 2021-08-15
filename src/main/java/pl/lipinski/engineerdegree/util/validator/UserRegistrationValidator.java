package pl.lipinski.engineerdegree.util.validator;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.*;
import pl.lipinski.engineerdegree.dao.entity.User;
import pl.lipinski.engineerdegree.service.UserService;

import java.util.ArrayList;
import java.util.List;

import static pl.lipinski.engineerdegree.util.error.ErrorCodes.*;
import static pl.lipinski.engineerdegree.util.error.ErrorMessages.PASSWORDS_DOESNT_MATCH_ERROR_MESSAGE;
import static pl.lipinski.engineerdegree.util.error.ErrorMessages.USERNAME_TAKEN_ERROR_MESSAGE;

@Component
public class UserRegistrationValidator implements Validator {

    private int errorCode;
    private final UserService userService;
    protected final ModelMapper modelMapper;

    @Autowired
    public UserRegistrationValidator(UserService userService) {
        this.userService = userService;
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
            errorCode = EMPTY_VALUE_ERROR_CODE.getValue();
            return;
        }

        User user = modelMapper.map(o, User.class);

        if (!user.getPassword().equals(user.getPasswordConfirmation())) {
            errorCode = PASSWORDS_DOESNT_MATCH_ERROR_CODE.getValue();
            errors.rejectValue("password", PASSWORDS_DOESNT_MATCH_ERROR_MESSAGE.getMessage());
            return;
        }
        if (userService.findByUsername(user.getUsername()).isPresent()) {
            errorCode = USERNAME_TAKEN_ERROR_CODE.getValue();
            errors.rejectValue("username", USERNAME_TAKEN_ERROR_MESSAGE.getMessage());
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
            errorList.add(fieldError.getCode());
        }
        return errorList;
    }
}

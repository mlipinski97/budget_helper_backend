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

    private final Integer EMPTY_VALUE_ERROR_CODE = 500;
    private final Integer PASSWORDS_DOESNT_MATCH_ERROR_CODE = 501;
    private final Integer USERNAME_TAKEN_ERROR_CODE = 502;
    private final String PASSWORDS_DOESNT_MATCH_ERROR_MESSAGE = "passowords does not match!";
    private final String USERNAME_TAKEN_ERROR_MESSAGE = "This username is already registered!";


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
            errorCode = EMPTY_VALUE_ERROR_CODE;
            return;
        }

        User user = modelMapper.map(o, User.class);

        if (!user.getPassword().equals(user.getPasswordConfirmation())) {
            errorCode = PASSWORDS_DOESNT_MATCH_ERROR_CODE;
            errors.rejectValue("password", PASSWORDS_DOESNT_MATCH_ERROR_MESSAGE);
            return;
        }
        if (userManager.findByUsername(user.getUsername()).isPresent()) {
            errorCode = USERNAME_TAKEN_ERROR_CODE;
            errors.rejectValue("username", USERNAME_TAKEN_ERROR_MESSAGE);
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

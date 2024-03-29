package pl.lipinski.engineerdegree.util.validator;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.*;
import pl.lipinski.engineerdegree.dao.entity.BudgetList;

import java.util.ArrayList;
import java.util.List;

import static pl.lipinski.engineerdegree.util.error.ErrorCodes.EMPTY_VALUE_ERROR_CODE;
import static pl.lipinski.engineerdegree.util.error.ErrorCodes.WRONG_BUDGET_VALUE_ERROR_CODE;
import static pl.lipinski.engineerdegree.util.error.ErrorMessages.WRONG_BUDGET_VALUE_ERROR_MESSAGE;

@Component
public class BudgetListValidator implements Validator {

    private int errorCode;
    private ModelMapper modelMapper;

    @Autowired
    public BudgetListValidator() {
        this.modelMapper = new ModelMapper();
    }

    @Override
    public boolean supports(Class<?> aClass) {
        return BudgetList.class.equals(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {

        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "name", "name is empty!");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "budgetValue", "budgetValue is empty!");

        if (errors.hasErrors()) {
            errorCode = EMPTY_VALUE_ERROR_CODE.getValue();
            return;
        }

        BudgetList budgetList = modelMapper.map(o, BudgetList.class);

        if (budgetList.getBudgetValue() <= 0) {
            errorCode = WRONG_BUDGET_VALUE_ERROR_CODE.getValue();
            errors.rejectValue("BudgetValue", WRONG_BUDGET_VALUE_ERROR_MESSAGE.getMessage());
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

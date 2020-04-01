package pl.lipinski.engineerdegree.util.validator;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.*;
import pl.lipinski.engineerdegree.dao.entity.BudgetList;
import pl.lipinski.engineerdegree.dao.entity.Expense;

import java.util.ArrayList;
import java.util.List;

@Component
public class BudgetListValidator implements Validator {

    private final Integer EMPTY_VALUE_ERROR_CODE = 500;
    private final Integer WRONG_BUDGET_VALUE_ERROR_CODE = 505;
    private final String WRONG_BUDGET_VALUE_ERROR_MESSAGE = "Budget value can not be negative value or zero!";


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
            errorCode = EMPTY_VALUE_ERROR_CODE;
            return;
        }

        BudgetList budgetList = modelMapper.map(o, BudgetList.class);

        if(budgetList.getBudgetValue() <= 0){
            errorCode = WRONG_BUDGET_VALUE_ERROR_CODE;
            errors.rejectValue("BudgetValue",WRONG_BUDGET_VALUE_ERROR_MESSAGE);
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

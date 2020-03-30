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
            errorCode = 500;
            return;
        }

        BudgetList budgetList = modelMapper.map(o, BudgetList.class);

        if(budgetList.getBudgetValue() <= 0){
            errorCode = 505;
            errors.rejectValue("BudgetValue","Budget value can not be negative value!");
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

package pl.lipinski.engineerdegree.util.validator;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.*;
import pl.lipinski.engineerdegree.dao.entity.Expense;
import pl.lipinski.engineerdegree.manager.UserManager;

import java.util.ArrayList;
import java.util.List;

@Component
public class ExpenseValidator implements Validator {

    private final Integer EMPTY_VALUE_ERROR_CODE = 500;
    private final Integer WRONG_EXPENSE_VALUE_ERROR_CODE = 504;
    private final String WRONG_EXPENSE_VALUE_ERROR_MESSAGE = "Expense amount can not be negative value or zero!";

    private int errorCode;
    private ModelMapper modelMapper;

    @Autowired
    public ExpenseValidator() {
        this.modelMapper = new ModelMapper();
    }

    @Override
    public boolean supports(Class<?> aClass) {
        return Expense.class.equals(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {

        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "name", "Expense name is empty!");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "amount", "amount is empty!");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "dateOfExpense", "dateOfExpense is empty!");

        if (errors.hasErrors()) {
            errorCode = EMPTY_VALUE_ERROR_CODE;
            return;
        }

        Expense expense = modelMapper.map(o, Expense.class);

        if(expense.getAmount() <= 0){
            errorCode = WRONG_EXPENSE_VALUE_ERROR_CODE;
            errors.rejectValue("amount", WRONG_EXPENSE_VALUE_ERROR_MESSAGE);
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

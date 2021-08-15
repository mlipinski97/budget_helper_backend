package pl.lipinski.engineerdegree.util.validator;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.*;
import pl.lipinski.engineerdegree.dao.entity.Expense;

import java.util.ArrayList;
import java.util.List;

import static pl.lipinski.engineerdegree.util.error.ErrorCodes.EMPTY_VALUE_ERROR_CODE;
import static pl.lipinski.engineerdegree.util.error.ErrorCodes.WRONG_EXPENSE_VALUE_ERROR_CODE;
import static pl.lipinski.engineerdegree.util.error.ErrorMessages.WRONG_EXPENSE_VALUE_ERROR_MESSAGE;

@Component
public class ExpenseValidator implements Validator {

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

        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "name", "expense name is empty!");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "amount", "amount is empty!");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "dateOfExpense", "dateOfExpense is empty!");

        if (errors.hasErrors()) {
            errorCode = EMPTY_VALUE_ERROR_CODE.getValue();
            return;
        }

        Expense expense = modelMapper.map(o, Expense.class);

        if (expense.getAmount() <= 0) {
            errorCode = WRONG_EXPENSE_VALUE_ERROR_CODE.getValue();
            errors.rejectValue("amount", WRONG_EXPENSE_VALUE_ERROR_MESSAGE.getMessage());
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

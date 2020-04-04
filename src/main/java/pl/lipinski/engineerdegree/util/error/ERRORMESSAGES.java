package pl.lipinski.engineerdegree.util.error;

public enum ERRORMESSAGES {

    WRONG_BUDGET_VALUE_ERROR_MESSAGE("Budget value can not be negative value or zero!"),
    WRONG_EXPENSE_VALUE_ERROR_MESSAGE("Expense amount can not be negative value or zero!"),
    PASSWORDS_DOESNT_MATCH_ERROR_MESSAGE("passowords does not match!"),
    USERNAME_TAKEN_ERROR_MESSAGE("This username is already registered!"),
    EXPENSE_NOT_FOUND_ERROR_MESSAGE("Expense not found!"),
    BUDGET_LIST_NOT_FOUND_ERROR_MESSAGE("Budget list not found!"),
    INTERSECTION_ALREADY_EXISTS_ERROR_MESSAGE("User already have permission for that budget list!"),
    USER_NOT_FOUND_ERROR_MESSAGE("User not found!"),
    INTERSECTION_NOT_FOUND_ERROR_MESSAGE("This user does not have permission for that list, no permission to revoke!"),
    USER_DONT_HAVE_PERMISSIONS_ERROR_MESSAGE("This user dont have permission to access this entity!");

    private String message;

    public String getMessage()
    {
        return this.message;
    }

    ERRORMESSAGES(String message)
    {
        this.message = message;
    }
}

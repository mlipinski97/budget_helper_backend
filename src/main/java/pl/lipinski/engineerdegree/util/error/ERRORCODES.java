package pl.lipinski.engineerdegree.util.error;

public enum ERRORCODES {

    EMPTY_VALUE_ERROR_CODE(500),
    PASSWORDS_DOESNT_MATCH_ERROR_CODE(501),
    USERNAME_TAKEN_ERROR_CODE(502),
    WRONG_EXPENSE_VALUE_ERROR_CODE(503),
    WRONG_BUDGET_VALUE_ERROR_CODE(504),
    INTERSECTION_ALREADY_EXISTS_ERROR_CODE(505),
    USER_NOT_FOUND_ERROR_CODE(506),
    BUDGET_LIST_NOT_FOUND_ERROR_CODE(507),
    INTERSECTION_NOT_FOUND_ERROR_CODE(508),
    EXPENSE_NOT_FOUND_ERROR_CODE(509);

    private Integer value;

    public Integer getValue()
    {
        return this.value;
    }

    ERRORCODES(Integer value)
    {
        this.value = value;
    }
}

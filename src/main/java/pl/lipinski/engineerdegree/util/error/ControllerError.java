package pl.lipinski.engineerdegree.util.error;

import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;

public class ControllerError {
    private HttpStatus status;
    private Integer errorCode;

    private List<String> errors;
    private LocalDateTime errorTime;

    public ControllerError(HttpStatus status, Integer errorCode, List<String> errors) {
        this.status = status;
        this.errorCode = errorCode;
        this.errors = errors;
        this.errorTime = LocalDateTime.now();
    }

    public ControllerError(HttpStatus status, Integer errorCode, List<String> errors, LocalDateTime errorTime) {
        this.status = status;
        this.errorCode = errorCode;
        this.errors = errors;
        this.errorTime = errorTime;
    }

    public ControllerError() {
    }

    public HttpStatus getStatus() {
        return status;
    }

    public void setStatus(HttpStatus status) {
        this.status = status;
    }

    public Integer getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(Integer errorCode) {
        this.errorCode = errorCode;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }

    public LocalDateTime getErrorTime() {
        return errorTime;
    }

    public void setErrorTime(LocalDateTime errorTime) {
        this.errorTime = errorTime;
    }
}

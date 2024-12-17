package mg.itu.prom16.exception;

import mg.itu.prom16.model.ValidationError;

public class ValidationException extends Exception {
    private ValidationError validationError;
    private String errorUrl;

    public ValidationException() {
        super("Validation échouée");
    }

    public String getErrorUrl() {
        return errorUrl;
    }

    public void setErrorUrl(String errorUrl) {
        this.errorUrl = errorUrl;
    }

    public ValidationException(ValidationError validationError) {
        super("Validation échouée");
        this.validationError = validationError;
    }

    public ValidationException(ValidationError validationError, String errorUrl) {
        super("Validation échouée");
        this.validationError = validationError;
        this.errorUrl = errorUrl;
    }

    public ValidationError getValidationError() {
        return validationError;
    }

}

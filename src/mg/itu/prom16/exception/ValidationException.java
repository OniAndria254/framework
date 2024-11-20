package mg.itu.prom16.exception;

import mg.itu.prom16.model.ValidationError;

public class ValidationException extends Exception {
    private ValidationError validationError;
    private Object formData;

    public ValidationException(ValidationError validationError, Object formData) {
        super("Validation échouée");
        this.validationError = validationError;
        this.formData = formData;
    }

    public ValidationError getValidationError() {
        return validationError;
    }

    public Object getFormData() {
        return formData;
    }
}

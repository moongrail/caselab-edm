package ru.caselab.edm.backend.validation.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;
import ru.caselab.edm.backend.validation.annotations.PasswordMatcher;
import ru.caselab.edm.backend.validation.interfaces.PasswordValidatable;

public class PasswordMatcherValidator implements ConstraintValidator<PasswordMatcher, PasswordValidatable> {

    private static final String PASSWORD_CONFIRMATION_PROPERTY_NODE = "passwordConfirmation";

    @Override
    public void initialize(PasswordMatcher constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(PasswordValidatable value, ConstraintValidatorContext context) {
        if (passwordsValuesAreNotNull(value) && passwordsValuesAreEquals(value)) {
            return true;
        }

        context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                .addPropertyNode(PASSWORD_CONFIRMATION_PROPERTY_NODE)
                .addConstraintViolation();

        return false;
    }

    private boolean passwordsValuesAreNotNull(PasswordValidatable value) {
        return value != null && value.getPassword() != null && value.getPasswordConfirmation() != null;
    }

    private boolean passwordsValuesAreEquals(PasswordValidatable value) {
        return value.getPassword().equals(value.getPasswordConfirmation());
    }
}

package ru.caselab.edm.backend.validation.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import ru.caselab.edm.backend.validation.annotations.PasswordMatcher;
import ru.caselab.edm.backend.validation.interfaces.PasswordValidatable;

public class PasswordMatcherValidator implements ConstraintValidator<PasswordMatcher, PasswordValidatable> {

    @Override
    public void initialize(PasswordMatcher constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(PasswordValidatable value, ConstraintValidatorContext context) {
        return value != null && value.getPassword()
                .equals(value.getPasswordConfirmation());
    }
}

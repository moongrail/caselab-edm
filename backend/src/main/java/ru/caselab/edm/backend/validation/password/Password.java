package ru.caselab.edm.backend.validation.password;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = {})
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Size(min = 5, max = 20, message = "Password length must be between {min} and {max} characters")
@Pattern(regexp = ".*\\d.*", message = "Password must contain at least one digit")
@Pattern(regexp = ".*[\\W_].*", message = "Password must contain at least one special character")
public @interface Password {

    String message() default "Invalid password";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

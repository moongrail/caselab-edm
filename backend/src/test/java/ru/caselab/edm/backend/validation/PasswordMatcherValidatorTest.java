package ru.caselab.edm.backend.validation;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.caselab.edm.backend.validation.validators.PasswordMatcherValidator;
import ru.caselab.edm.backend.validation.interfaces.PasswordValidatable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PasswordMatcherValidatorTest {


    @InjectMocks
    private PasswordMatcherValidator passwordMatcherValidator;
    @Mock
    private PasswordValidatable passwordValidatable;
    @Mock
    private ConstraintValidatorContext context;

    @Test
    void isValid_passwordsAreEquals_shouldReturnTrue() {
        String password = "test";
        when(passwordValidatable.getPassword()).thenReturn(password);
        when(passwordValidatable.getPasswordConfirmation()).thenReturn(password);

        boolean result = passwordMatcherValidator.isValid(passwordValidatable, context);

        verify(passwordValidatable, times(2)).getPassword();
        verify(passwordValidatable, times(2)).getPasswordConfirmation();
        assertThat(result).isTrue();
    }

    @Test
    void isValid_passwordsDoNotEquals_shouldReturnFalse() {
        when(passwordValidatable.getPassword()).thenReturn("password");
        when(passwordValidatable.getPasswordConfirmation()).thenReturn("password-confirmation");

        boolean result = passwordMatcherValidator.isValid(passwordValidatable, context);

        verify(passwordValidatable, times(2)).getPassword();
        verify(passwordValidatable, times(2)).getPasswordConfirmation();
        assertThat(result).isFalse();
    }

}

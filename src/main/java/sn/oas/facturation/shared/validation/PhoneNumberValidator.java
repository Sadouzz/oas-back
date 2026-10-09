package sn.oas.facturation.shared.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PhoneNumberValidator implements ConstraintValidator<ValidPhone, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) return true;

        String phone = value.trim();
        if (!phone.matches("\\+?[0-9() .-]+")) return false;

        long digitCount = phone.chars().filter(Character::isDigit).count();
        return digitCount >= 7 && digitCount <= 15;
    }
}

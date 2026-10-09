package sn.oas.facturation.shared.validation;

import org.junit.jupiter.api.Test;
import jakarta.validation.Validation;
import sn.oas.facturation.features.client.data.enums.TypeClient;
import sn.oas.facturation.features.client.dto.ClientCreateRequest;

import static org.junit.jupiter.api.Assertions.*;

class PhoneNumberValidatorTest {
    private final PhoneNumberValidator validator = new PhoneNumberValidator();

    @Test
    void acceptsInternationalNumberWithCommonSeparators() {
        assertTrue(validator.isValid("+221 77 123 45 67", null));
        assertTrue(validator.isValid("+33 (0)6-12-34-56-78", null));
    }

    @Test
    void rejectsLettersAndNumbersOutsideSupportedLength() {
        assertFalse(validator.isValid("+221 77 ABC 45 67", null));
        assertFalse(validator.isValid("123456", null));
        assertFalse(validator.isValid("1234567890123456", null));
    }

    @Test
    void acceptsEmptyOptionalValues() {
        assertTrue(validator.isValid(null, null));
        assertTrue(validator.isValid("  ", null));
    }

    @Test
    void dtoConstraintRejectsLettersAtTheApiBoundary() {
        var validatorFactory = Validation.buildDefaultValidatorFactory();
        try {
            var request = new ClientCreateRequest("Awa", "Diop", "+221 77 ABC 45 67", null,
                    null, null, TypeClient.PARTICULIER, null, null, null, null, null);

            assertTrue(validatorFactory.getValidator().validate(request).stream()
                    .anyMatch(violation -> violation.getPropertyPath().toString().equals("phone")));
        } finally {
            validatorFactory.close();
        }
    }
}

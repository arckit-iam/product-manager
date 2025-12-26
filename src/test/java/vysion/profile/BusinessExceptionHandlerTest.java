package vysion.profile;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import secop.ppm1.domain.exception.BusinessExceptionHandler;
import secop.ppm1.domain.exception.ExistProductNameConstraintViolationException;
import secop.ppm1.domain.exception.NotFoundException;
import secop.ppm1.domain.exception.UnidentifiedException;
import secop.ppm1.domain.exception.UniqueProductNameConstraintViolationException;

public class BusinessExceptionHandlerTest {

    private BusinessExceptionHandler businessExceptionHandler;

    @BeforeEach
    public void setUp() {
        businessExceptionHandler = new BusinessExceptionHandler();
    }

    @Test
    public void testHandleUniqueConstraintViolation() {
        String message = "Product name already exists";
        UniqueProductNameConstraintViolationException exception = 
            new UniqueProductNameConstraintViolationException(message);

        ResponseEntity<String> response = businessExceptionHandler.handleUniqueConstraintViolation(exception);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(message, response.getBody());
    }

    @Test
    public void testHandleResourceNotFoundException() {
        String message = "Resource not found";
        NotFoundException exception = new NotFoundException(message);

        ResponseEntity<String> response = businessExceptionHandler.handleResourceNotFoundException(exception);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(message, response.getBody());
    }

    @Test
    public void testHandleUnidentiedException() {
        String message = "An unidentified error occurred";
        UnidentifiedException exception = new UnidentifiedException(message);

        ResponseEntity<String> response = businessExceptionHandler.handleUnidentiedException(exception);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(message, response.getBody());
    }

    @Test
    public void testHandleExistProductNameConstraintViolation() {
        String message = "Product name constraint violation";
        ExistProductNameConstraintViolationException exception = 
            new ExistProductNameConstraintViolationException(message);

        ResponseEntity<String> response = businessExceptionHandler.handleExistProductNameConstraintViolation(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(message, response.getBody());
    }

    @Test
    public void testHandleUniqueConstraintViolation_NullMessage() {
        UniqueProductNameConstraintViolationException exception = 
            new UniqueProductNameConstraintViolationException(null);

        ResponseEntity<String> response = businessExceptionHandler.handleUniqueConstraintViolation(exception);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    public void testHandleResourceNotFoundException_EmptyMessage() {
        NotFoundException exception = new NotFoundException("");

        ResponseEntity<String> response = businessExceptionHandler.handleResourceNotFoundException(exception);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("", response.getBody());
    }

    @Test
    public void testHandleUnidentiedException_LongMessage() {
        String longMessage = "This is a very long error message: " + "a".repeat(1000);
        UnidentifiedException exception = new UnidentifiedException(longMessage);

        ResponseEntity<String> response = businessExceptionHandler.handleUnidentiedException(exception);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(longMessage, response.getBody());
    }

    @Test
    public void testHandleExistProductNameConstraintViolation_SpecialCharacters() {
        String messageWithSpecialChars = "Error with special chars: !@#$%^&*()";
        ExistProductNameConstraintViolationException exception = 
            new ExistProductNameConstraintViolationException(messageWithSpecialChars);

        ResponseEntity<String> response = businessExceptionHandler.handleExistProductNameConstraintViolation(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(messageWithSpecialChars, response.getBody());
    }

    @Test
    public void testAllExceptionHandlersReturnCorrectStatusCodes() {
        // Test que chaque handler retourne le bon code de statut HTTP
        ResponseEntity<String> conflictResponse = businessExceptionHandler.handleUniqueConstraintViolation(
            new UniqueProductNameConstraintViolationException("test"));
        assertEquals(409, conflictResponse.getStatusCodeValue()); // CONFLICT

        ResponseEntity<String> notFoundResponse = businessExceptionHandler.handleResourceNotFoundException(
            new NotFoundException("test"));
        assertEquals(404, notFoundResponse.getStatusCodeValue()); // NOT_FOUND

        ResponseEntity<String> serverErrorResponse = businessExceptionHandler.handleUnidentiedException(
            new UnidentifiedException("test"));
        assertEquals(500, serverErrorResponse.getStatusCodeValue()); // INTERNAL_SERVER_ERROR

        ResponseEntity<String> badRequestResponse = businessExceptionHandler.handleExistProductNameConstraintViolation(
            new ExistProductNameConstraintViolationException("test"));
        assertEquals(400, badRequestResponse.getStatusCodeValue()); // BAD_REQUEST
    }
}
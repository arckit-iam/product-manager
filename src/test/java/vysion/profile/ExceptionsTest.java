package vysion.profile;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import secop.ppm1.domain.exception.ExistProductNameConstraintViolationException;
import secop.ppm1.domain.exception.NotFoundException;
import secop.ppm1.domain.exception.UnidentifiedException;
import secop.ppm1.domain.exception.UniqueProductNameConstraintViolationException;

public class ExceptionsTest {

    @Test
    public void testUniqueProductNameConstraintViolationException() {
        String message = "Product name already exists";
        UniqueProductNameConstraintViolationException exception = 
            new UniqueProductNameConstraintViolationException(message);

        assertEquals(message, exception.getMessage());
        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    public void testUniqueProductNameConstraintViolationException_NullMessage() {
        UniqueProductNameConstraintViolationException exception = 
            new UniqueProductNameConstraintViolationException(null);

        assertNull(exception.getMessage());
    }

    @Test
    public void testNotFoundException() {
        String message = "Resource not found";
        NotFoundException exception = new NotFoundException(message);

        assertEquals(message, exception.getMessage());
        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    public void testNotFoundException_NullMessage() {
        NotFoundException exception = new NotFoundException(null);

        assertNull(exception.getMessage());
    }

    @Test
    public void testUnidentifiedException() {
        String message = "An unidentified error occurred";
        UnidentifiedException exception = new UnidentifiedException(message);

        assertEquals(message, exception.getMessage());
        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    public void testUnidentifiedException_NullMessage() {
        UnidentifiedException exception = new UnidentifiedException(null);

        assertNull(exception.getMessage());
    }

    @Test
    public void testExistProductNameConstraintViolationException() {
        String message = "Product name constraint violation";
        ExistProductNameConstraintViolationException exception = 
            new ExistProductNameConstraintViolationException(message);

        assertEquals(message, exception.getMessage());
        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    public void testExistProductNameConstraintViolationException_NullMessage() {
        ExistProductNameConstraintViolationException exception = 
            new ExistProductNameConstraintViolationException(null);

        assertNull(exception.getMessage());
    }

    @Test
    public void testExceptionInheritance() {
        // Test que toutes les exceptions héritent bien de RuntimeException
        assertTrue(RuntimeException.class.isAssignableFrom(UniqueProductNameConstraintViolationException.class));
        assertTrue(RuntimeException.class.isAssignableFrom(NotFoundException.class));
        assertTrue(RuntimeException.class.isAssignableFrom(UnidentifiedException.class));
        assertTrue(RuntimeException.class.isAssignableFrom(ExistProductNameConstraintViolationException.class));
    }

    @Test
    public void testExceptionMessages() {
        String[] testMessages = {
            "Test message 1",
            "Test message with special chars !@#$%^&*()",
            "Very long message: " + "a".repeat(1000),
            "",
            "Message with\nnewlines\nand\ttabs"
        };

        for (String message : testMessages) {
            UniqueProductNameConstraintViolationException unique = 
                new UniqueProductNameConstraintViolationException(message);
            NotFoundException notFound = new NotFoundException(message);
            UnidentifiedException unidentified = new UnidentifiedException(message);
            ExistProductNameConstraintViolationException exist = 
                new ExistProductNameConstraintViolationException(message);

            assertEquals(message, unique.getMessage());
            assertEquals(message, notFound.getMessage());
            assertEquals(message, unidentified.getMessage());
            assertEquals(message, exist.getMessage());
        }
    }
}
package vysion.profile.adapters.in;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import vysion.profile.domain.exception.UniqueProductNameConstraintViolationException;

@RestControllerAdvice
public class ApiExceptionHandler  {
   
    @ExceptionHandler(UniqueProductNameConstraintViolationException.class)
    public ResponseEntity<String> handleUniqueConstraintViolation(UniqueProductNameConstraintViolationException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<String> handleResourceNotFoundException(ProductNotFoundException ex) {
        // Créez une réponse d'erreur personnalisée avec le message d'exception
   
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }
}

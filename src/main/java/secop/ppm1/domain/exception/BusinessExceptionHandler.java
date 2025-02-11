package secop.ppm1.domain.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class BusinessExceptionHandler  {
   
    @ExceptionHandler(UniqueProductNameConstraintViolationException.class)
    public ResponseEntity<String> handleUniqueConstraintViolation(UniqueProductNameConstraintViolationException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<String> handleResourceNotFoundException(NotFoundException ex) {   
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(UnidentifiedException.class)
    public ResponseEntity<String> handleUnidentiedException(UnidentifiedException ex) {   
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
    }

    @ExceptionHandler(ExistProductNameConstraintViolationException.class)
    public ResponseEntity<String> handleExistProductNameConstraintViolation(ExistProductNameConstraintViolationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }
}

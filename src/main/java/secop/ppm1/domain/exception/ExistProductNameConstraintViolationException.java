package secop.ppm1.domain.exception;

public class ExistProductNameConstraintViolationException extends RuntimeException{
    public ExistProductNameConstraintViolationException(String message){
        super(message);
    }
}

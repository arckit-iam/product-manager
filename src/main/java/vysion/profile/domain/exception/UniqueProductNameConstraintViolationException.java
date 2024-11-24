package vysion.profile.domain.exception;

public class UniqueProductNameConstraintViolationException extends RuntimeException{
    public UniqueProductNameConstraintViolationException(String message){
        super(message);
    }
}

package vysion.profile.domain.exception;

public class ExistProductNameConstraintViolationException extends RuntimeException{
    public ExistProductNameConstraintViolationException(String message){
        super(message);
    }
}

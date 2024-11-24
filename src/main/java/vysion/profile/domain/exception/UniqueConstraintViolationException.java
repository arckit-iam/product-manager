package vysion.profile.domain.exception;

public class UniqueConstraintViolationException extends RuntimeException{
    public UniqueConstraintViolationException(String message){
        super(message);
    }
}

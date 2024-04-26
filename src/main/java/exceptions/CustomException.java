package exceptions;

public class CustomException extends RuntimeException{

    public CustomException(Exception e){
        super(e);
    }
}

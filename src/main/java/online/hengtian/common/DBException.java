package online.hengtian.common;

import lombok.Data;

@Data
public class DBException extends Exception{
    private String code;
    public DBException(String code,String message){
        super(message);
        this.code=code;
    }
}

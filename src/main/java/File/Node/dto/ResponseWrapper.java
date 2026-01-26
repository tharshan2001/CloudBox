package File.Node.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ResponseWrapper<T> {
    private boolean success;
    private String message;
    private T data;
}

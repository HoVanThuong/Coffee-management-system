package network;

import lombok.*;

import java.io.Serializable;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class Response implements Serializable {
    private boolean success;
    private Object data;
    private String message;
}

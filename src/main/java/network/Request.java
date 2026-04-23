package network;

import lombok.*;

import java.io.Serializable;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class Request implements Serializable {
    private CommandType commandType;
    private Object data;

}

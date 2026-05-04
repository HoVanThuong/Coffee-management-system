package dto;

import lombok.*;
import java.io.Serializable;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoUongDTO implements Serializable {
    private String maDoUong;
    private String tenDoUong;
    private String moTaDoUong;
    private Double giaTien;
    private String loaiDoUong;
}
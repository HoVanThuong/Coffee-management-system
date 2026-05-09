package dto;

import lombok.*;
import java.io.Serializable;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoUongDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String maDoUong;
    private String tenDoUong;
    private String moTaDoUong;
    private String giaTien;
    private String loaiDoUong;
    private byte[] hinhAnh;
}
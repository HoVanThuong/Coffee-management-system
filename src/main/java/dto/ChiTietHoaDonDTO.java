package dto;

import lombok.*;
import java.io.Serializable;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChiTietHoaDonDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private int soLuong;
    private Double donGia;
    private DoUongDTO doUong;
}

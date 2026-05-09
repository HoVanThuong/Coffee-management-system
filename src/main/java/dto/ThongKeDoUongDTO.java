package dto;

import lombok.*;
import java.io.Serializable;

/**
 * DTO thống kê doanh thu của từng loại đồ uống
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ThongKeDoUongDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String maDoUong;
    private String tenDoUong;
    private String loaiDoUong;
    private int soLuongDaBan;
    private double doanhThu;
}

package dto;
import lombok.*;
import java.io.Serializable;
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class HoaDonDTO implements Serializable {
    private String maHoaDon;
    private String ngayTao;
    private String trangThai;
    private Double tongTien;
    private String maPhieuGoi;
}
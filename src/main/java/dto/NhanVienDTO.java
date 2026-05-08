package dto;

import lombok.*;
import java.io.Serializable;
import java.time.LocalDate;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NhanVienDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String maNhanVien;
    private String hoTen;
    private String sdt;
    private String email;
    private String chucVu;
    private LocalDate ngayVaoLam;
    private LocalDate ngayThoiViec;
}
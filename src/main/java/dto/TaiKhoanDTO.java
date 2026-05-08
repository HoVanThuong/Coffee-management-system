package dto;

import lombok.*;
import java.io.Serializable;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaiKhoanDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String maTaiKhoan;
    private String tenDangNhap;
    private String matKhau;
    private boolean taiKhoanQuanLi;
    private String trangThai;
    private NhanVienDTO nhanVien;
}

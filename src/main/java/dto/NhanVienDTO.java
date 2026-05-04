package dto;
import lombok.*;
import java.io.Serializable;

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
}
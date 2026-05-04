package dto;

import jakarta.persistence.Column;
import lombok.*;

import java.io.Serializable;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BanDTO implements Serializable {
    private String maBan;
    private String trangThai;
    private String viTri;
}

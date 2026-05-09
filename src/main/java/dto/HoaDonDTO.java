package dto;

import lombok.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HoaDonDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String maHoaDon;
    private LocalDate ngayTao;
    private String trangThai;
    private Double tongTien;
    private String ghiChu;
    private String phuongThucTT;
    private BanDTO ban;
    private NhanVienDTO nhanVien;
    private List<ChiTietHoaDonDTO> chiTietHoaDons;

    public String getMaHoaDon() { return maHoaDon; }
    public void setMaHoaDon(String maHoaDon) { this.maHoaDon = maHoaDon; }

    public LocalDate getNgayTao() { return ngayTao; }
    public void setNgayTao(LocalDate ngayTao) { this.ngayTao = ngayTao; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public Double getTongTien() { return tongTien; }
    public void setTongTien(Double tongTien) { this.tongTien = tongTien; }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }

    public String getPhuongThucTT() { return phuongThucTT; }
    public void setPhuongThucTT(String phuongThucTT) { this.phuongThucTT = phuongThucTT; }

    public BanDTO getBan() { return ban; }
    public void setBan(BanDTO ban) { this.ban = ban; }

    public NhanVienDTO getNhanVien() { return nhanVien; }
    public void setNhanVien(NhanVienDTO nhanVien) { this.nhanVien = nhanVien; }

    public List<ChiTietHoaDonDTO> getChiTietHoaDons() { return chiTietHoaDons; }
    public void setChiTietHoaDons(List<ChiTietHoaDonDTO> chiTietHoaDons) { this.chiTietHoaDons = chiTietHoaDons; }
}
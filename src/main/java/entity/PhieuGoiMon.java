/*
 * @ PhieuGoiMon.java    1.0 23
 * Copyright (c) 2024 IUH. All rights reserved
 */

package entity;

/*
 * @description: Entity mapping cho bảng PhieuGoiMon
 * @author: Ho Van Thuong
 * @date: 23/04/2026
 * @version: 1.0
 */

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"ban", "nhanVien", "chiTietPhieuGois", "hoaDon"})
@Builder

@Entity
@Table(name = "phieu_goi_mon")
public class PhieuGoiMon {

    @Id
    @Column(name = "ma_phieu")
    private String maPhieu;

    @Column(name = "ngay_tao")
    private String ngayTao;

    @Column(name = "trang_thai")
    private String trangThai;

    @Column(name = "ghi_chu")
    private String ghiChu;

    @Column(name = "tong_tien")
    private Double tongTien;

    // PhieuGoiMon * --- 1 Ban
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_ban", nullable = false)
    private Ban ban;

    // PhieuGoiMon * --- 1 NhanVien
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_nhan_vien", nullable = false)
    private NhanVien nhanVien;

    // PhieuGoiMon 1 --- * ChiTietPhieuGoi
    @OneToMany(mappedBy = "phieuGoiMon", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ChiTietPhieuGoi> chiTietPhieuGois;

    // PhieuGoiMon 1 --- 1 HoaDon
    @OneToOne(mappedBy = "phieuGoiMon", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private HoaDon hoaDon;
}
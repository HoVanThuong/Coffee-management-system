/*
 * @ HoaDon.java    1.0 23
 * Copyright (c) 2024 IUH. All rights reserved
 */

package entity;

/*
 * @description: Entity mapping cho bảng HoaDon
 * @author: Ho Van Thuong
 * @date: 23/04/2026
 * @version: 1.0
 */

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDate;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"phieuGoiMon"})
@Builder

@Entity
@Table(name = "hoa_don")
public class HoaDon implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "ma_hoa_don")
    private String maHoaDon;

    @Column(name = "ngay_tao")
    private LocalDate ngayTao;

    @Column(name = "trang_thai")
    private String trangThai;

    @Column(name = "phuong_thuc_tt")
    private String phuongThucTT;

    @Column(name = "ghi_chu")
    private String ghiChu;

    @Column(name = "tong_tien")
    private Double tongTien;

    // HoaDon 1 --- 1 PhieuGoiMon (HoaDon giữ FK)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_phieu", nullable = false, unique = true)
    private PhieuGoiMon phieuGoiMon;
}
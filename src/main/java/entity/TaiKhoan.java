/*
 * @ TaiKhoan.java    1.0 23
 * Copyright (c) 2024 IUH. All rights reserved
 */

package entity;

/*
 * @description: Entity mapping cho bảng TaiKhoan
 * @author: Ho Van Thuong
 * @date: 23/04/2026
 * @version: 1.0
 */

import jakarta.persistence.*;
import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"nhanVien"})
@Builder

@Entity
@Table(name = "tai_khoan")
public class TaiKhoan {

    @Id
    @Column(name = "ma_tai_khoan")
    private String maTaiKhoan;

    @Column(name = "ten_dang_nhap", unique = true)
    private String tenDangNhap;

    @Column(name = "mat_khau")
    private String matKhau;

    @Column(name = "tai_khoan_quan_li")
    private boolean taiKhoanQuanLi;

    // TaiKhoan 1 --- 1 NhanVien (TaiKhoan giữ FK)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_nhan_vien", nullable = false, unique = true)
    private NhanVien nhanVien;
}
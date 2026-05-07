/*
 * @ ChiTietHoaDon.java    1.0 23
 * Copyright (c) 2024 IUH. All rights reserved
 */

package entity;

/*
 * @description: Entity mapping cho bảng ChiTietHoaDon
 * @author: Ho Van Thuong
 * @date: 23/04/2026
 * @version: 1.0
 */

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"hoaDon", "doUong"})
@Builder

@Entity
@Table(name = "chi_tiet_hoa_don")
public class ChiTietHoaDon implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "ma_chi_tiet")
    private String id;

    @Column(name = "so_luong")
    private int soLuong;

    @Column(name = "don_gia")
    private Double donGia;

    // ChiTietHoaDon * --- 1 HoaDon
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_hoa_don", nullable = false)
    private HoaDon hoaDon;

    // ChiTietHoaDon * --- 1 DoUong
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_do_uong", nullable = false)
    private DoUong doUong;
}

/*
 * @ ChiTietPhieuGoi.java    1.0 23
 * Copyright (c) 2024 IUH. All rights reserved
 */

package entity;

/*
 * @description: Entity mapping cho bảng ChiTietPhieuGoi
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
@ToString(exclude = {"phieuGoiMon", "doUong"})
@Builder

@Entity
@Table(name = "chi_tiet_phieu_goi")
public class ChiTietPhieuGoi implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "so_luong")
    private int soLuong;

    @Column(name = "don_gia")
    private Double donGia;

    // ChiTietPhieuGoi * --- 1 PhieuGoiMon
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_phieu", nullable = false)
    private PhieuGoiMon phieuGoiMon;

    // ChiTietPhieuGoi * --- 1 DoUong
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_do_uong", nullable = false)
    private DoUong doUong;
}
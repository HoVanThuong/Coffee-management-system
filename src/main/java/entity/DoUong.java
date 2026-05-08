/*
 * @ DoUong.java    1.0 23
 * Copyright (c) 2024 IUH. All rights reserved
 */

package entity;

/*
 * @description: Entity mapping cho bảng DoUong
 * @author: Ho Van Thuong
 * @date: 23/04/2026
 * @version: 1.0
 */

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"chiTietHoaDons"})
@Builder

@Entity
@Table(name = "do_uong")
public class DoUong implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "ma_do_uong")
    private String maDoUong;

    @Column(name = "ten_do_uong")
    private String tenDoUong;

    @Column(name = "mo_ta_do_uong")
    private String moTaDoUong;

    @Column(name = "gia_tien")
    private String giaTien;

    @Column(name = "loai_do_uong")
    private String loaiDoUong;

    // DoUong 1 --- * ChiTietHoaDon
    @JsonIgnore
    @OneToMany(mappedBy = "doUong", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ChiTietHoaDon> chiTietHoaDons;
}
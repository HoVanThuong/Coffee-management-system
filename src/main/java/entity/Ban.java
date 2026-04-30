/*
 * @ Ban.java    1.0 23
 * Copyright (c) 2024 IUH. All rights reserved
 */

package entity;

/*
 * @description: Entity mapping cho bảng Ban
 * @author: Ho Van Thuong
 * @date: 23/04/2026
 * @version: 1.0
 */

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"phieuGoiMons"})
@Builder

@Entity
@Table(name = "ban")
public class Ban implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "ma_ban")
    private String maBan;

    @Column(name = "trang_thai")
    private String trangThai;

    @Column(name = "vi_tri")
    private String viTri;

    // Ban 1 --- * PhieuGoiMon
    @OneToMany(mappedBy = "ban", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PhieuGoiMon> phieuGoiMons;
}
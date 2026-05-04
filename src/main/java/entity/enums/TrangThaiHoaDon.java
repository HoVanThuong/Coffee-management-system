package entity.enums;

/**
 * Enum biểu diễn trạng thái của Hóa Đơn.
 * Giá trị label phải khớp với giá trị lưu trong database.
 */
public enum TrangThaiHoaDon {
    CHUA_THANH_TOAN("Chưa thanh toán"),
    DA_THANH_TOAN("Đã thanh toán");

    private final String label;

    TrangThaiHoaDon(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    /**
     * Tìm enum từ chuỗi label. Trả về CHUA_THANH_TOAN nếu không tìm thấy.
     */
    public static TrangThaiHoaDon fromLabel(String label) {
        for (TrangThaiHoaDon t : values()) {
            if (t.label.equals(label)) return t;
        }
        return CHUA_THANH_TOAN;
    }

    @Override
    public String toString() {
        return label;
    }
}

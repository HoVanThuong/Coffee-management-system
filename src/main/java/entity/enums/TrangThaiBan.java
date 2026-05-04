package entity.enums;

/**
 * Enum biểu diễn trạng thái của Bàn.
 * Giá trị label phải khớp với giá trị lưu trong database.
 */
public enum TrangThaiBan {
    TRONG("Trống"),
    CO_KHACH("Có khách");

    private final String label;

    TrangThaiBan(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    /**
     * Tìm enum từ chuỗi label. Trả về TRONG nếu không tìm thấy.
     */
    public static TrangThaiBan fromLabel(String label) {
        for (TrangThaiBan t : values()) {
            if (t.label.equals(label)) return t;
        }
        return TRONG;
    }

    @Override
    public String toString() {
        return label;
    }
}

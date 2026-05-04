package ui;

public interface TableActionEvent {
    void onEdit(int row);
    void onDelete(int row);
}

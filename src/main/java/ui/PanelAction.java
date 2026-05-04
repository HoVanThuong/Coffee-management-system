package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.border.EmptyBorder;

public class PanelAction extends JPanel {
    private JButton cmdEdit;
    private JButton cmdDelete;

    public PanelAction() {
        initComponents();
        setOpaque(true);
        setBackground(Color.WHITE);
    }

    public void initEvent(TableActionEvent event, int row) {
        cmdEdit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                event.onEdit(row);
            }
        });
        cmdDelete.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                event.onDelete(row);
            }
        });
    }

    private void initComponents() {
        setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
        
        cmdEdit = new JButton("Sửa");
        cmdEdit.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cmdEdit.setForeground(Color.WHITE);
        cmdEdit.setBackground(new Color(237, 169, 38)); // Warning color
        cmdEdit.setFocusPainted(false);
        cmdEdit.setBorderPainted(false);
        cmdEdit.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cmdEdit.setBorder(new EmptyBorder(4, 8, 4, 8));

        cmdDelete = new JButton("Xóa");
        cmdDelete.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cmdDelete.setForeground(Color.WHITE);
        cmdDelete.setBackground(new Color(245, 101, 96)); // Danger color
        cmdDelete.setFocusPainted(false);
        cmdDelete.setBorderPainted(false);
        cmdDelete.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cmdDelete.setBorder(new EmptyBorder(4, 8, 4, 8));

        add(cmdEdit);
        add(cmdDelete);
    }
}

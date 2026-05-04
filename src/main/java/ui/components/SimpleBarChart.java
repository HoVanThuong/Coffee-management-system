package ui.components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedHashMap;
import java.util.Map;

public class SimpleBarChart extends JPanel {

    private Map<String, Double> data = new LinkedHashMap<>();
    private String title;
    private Color barColor = new Color(99, 179, 237); // C_ACCENT
    private Color hoverColor = new Color(66, 153, 225);
    private int hoveredBarIndex = -1;

    // Dimensions
    private int padding = 40;
    private int topPadding = 50;

    public SimpleBarChart(String title) {
        this.title = title;
        setBackground(Color.WHITE);
        
        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                if (data == null || data.isEmpty()) return;
                int oldHover = hoveredBarIndex;
                hoveredBarIndex = -1;
                
                int width = getWidth();
                int height = getHeight();
                int chartWidth = width - padding * 2;
                int chartHeight = height - padding - topPadding;
                int barWidth = chartWidth / data.size() - 20;
                
                double maxValue = data.values().stream().mapToDouble(v -> v).max().orElse(1);
                if (maxValue == 0) maxValue = 1;

                int i = 0;
                for (Double value : data.values()) {
                    int x = padding + i * (barWidth + 20) + 10;
                    int barHeight = (int) ((value / maxValue) * chartHeight);
                    int y = height - padding - barHeight;
                    
                    if (e.getX() >= x && e.getX() <= x + barWidth && e.getY() >= y && e.getY() <= height - padding) {
                        hoveredBarIndex = i;
                        break;
                    }
                    i++;
                }
                
                if (oldHover != hoveredBarIndex) {
                    repaint();
                }
            }
        });
        
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                if (hoveredBarIndex != -1) {
                    hoveredBarIndex = -1;
                    repaint();
                }
            }
        });
    }

    public void updateData(Map<String, Double> data) {
        this.data = data;
        repaint();
    }
    
    public void setBarColor(Color color) {
        this.barColor = color;
        this.hoverColor = color.darker();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        // Title
        g2.setColor(new Color(26, 32, 44));
        g2.setFont(new Font("Segoe UI", Font.BOLD, 16));
        FontMetrics fmTitle = g2.getFontMetrics();
        g2.drawString(title, padding, topPadding / 2 + fmTitle.getAscent() / 2);

        if (data == null || data.isEmpty()) {
            g2.setColor(Color.GRAY);
            g2.setFont(new Font("Segoe UI", Font.ITALIC, 14));
            g2.drawString("Chưa có dữ liệu", width / 2 - 50, height / 2);
            return;
        }

        int chartWidth = width - padding * 2;
        int chartHeight = height - padding - topPadding;
        
        double maxValue = data.values().stream().mapToDouble(v -> v).max().orElse(1);
        if (maxValue == 0) maxValue = 1;

        // Draw axes
        g2.setColor(new Color(226, 232, 240));
        g2.drawLine(padding, height - padding, width - padding, height - padding); // X
        // g2.drawLine(padding, height - padding, padding, topPadding); // Y
        
        // Draw Y axis lines
        g2.setColor(new Color(240, 242, 245));
        for (int i = 0; i <= 4; i++) {
            int y = topPadding + (chartHeight * i / 4);
            g2.drawLine(padding, y, width - padding, y);
        }

        int barWidth = chartWidth / data.size() - 20;
        int maxBarWidth = 60;
        if (barWidth > maxBarWidth) barWidth = maxBarWidth;

        int i = 0;
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        FontMetrics fm = g2.getFontMetrics();

        for (Map.Entry<String, Double> entry : data.entrySet()) {
            String label = entry.getKey();
            Double value = entry.getValue();

            int barHeight = (int) ((value / maxValue) * chartHeight);
            int x = padding + i * (chartWidth / data.size()) + (chartWidth / data.size() - barWidth) / 2;
            int y = height - padding - barHeight;

            // Draw Bar
            if (i == hoveredBarIndex) {
                g2.setColor(hoverColor);
            } else {
                g2.setColor(barColor);
            }
            g2.fillRoundRect(x, y, barWidth, barHeight, 8, 8);
            
            // Value text
            if (i == hoveredBarIndex || value > 0) {
                g2.setColor(new Color(113, 128, 150));
                String vStr = value == Math.floor(value) ? String.format("%.0f", value) : String.valueOf(value);
                int vWidth = fm.stringWidth(vStr);
                g2.drawString(vStr, x + (barWidth - vWidth) / 2, y - 5);
            }

            // X axis label
            g2.setColor(new Color(113, 128, 150));
            int lWidth = fm.stringWidth(label);
            g2.drawString(label, x + (barWidth - lWidth) / 2, height - padding + 20);

            i++;
        }
    }
}

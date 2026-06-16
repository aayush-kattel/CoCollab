package components;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class CardPanel extends JPanel {

    private int radius;
    private Color bg;
    private boolean showBorder;
    private Color borderColor;

    public CardPanel() {
        this(Theme.BG_CARD, 16, true);
    }

    public CardPanel(Color bg, int radius, boolean showBorder) {
        this.bg = bg;
        this.radius = radius;
        this.showBorder = showBorder;
        this.borderColor = Theme.BORDER;
        setOpaque(false);
    }

    public void setBorderColor(Color c) { this.borderColor = c; }
    public void setCardBackground(Color c) { this.bg = c; }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(bg);
        g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), radius, radius));
        if (showBorder) {
            g2.setColor(borderColor);
            g2.setStroke(new BasicStroke(1f));
            g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth()-1, getHeight()-1, radius, radius));
        }
        g2.dispose();
        super.paintComponent(g);
    }
}
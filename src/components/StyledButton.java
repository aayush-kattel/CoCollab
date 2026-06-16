package components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

public class StyledButton extends JButton {

    public enum Style { PRIMARY, SECONDARY, SUCCESS, DANGER, GHOST }

    private Style style;
    private boolean hovered = false;
    private int radius;

    public StyledButton(String text, Style style) {
        super(text);
        this.style = style;
        this.radius = 10;
        setup();
    }

    public StyledButton(String text) {
        this(text, Style.PRIMARY);
    }

    private void setup() {
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setFont(Theme.FONT_BOLD);
        setForeground(Theme.TEXT_PRIMARY);

        addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { hovered = true; repaint(); }
            public void mouseExited(MouseEvent e)  { hovered = false; repaint(); }
        });
    }

    public void setRadius(int r) { this.radius = r; }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Color base, hover;
        switch (style) {
            case PRIMARY:
                base  = Theme.ACCENT;
                hover = new Color(79, 83, 220);
                break;
            case SUCCESS:
                base  = new Color(22, 160, 70);
                hover = new Color(15, 130, 55);
                break;
            case DANGER:
                base  = new Color(200, 50, 50);
                hover = new Color(170, 30, 30);
                break;
            case GHOST:
                base  = new Color(30, 37, 60);
                hover = new Color(40, 50, 80);
                break;
            default:
                base  = new Color(30, 37, 60);
                hover = new Color(40, 50, 80);
        }

        Color fill = hovered ? hover : base;

        // Glow effect on hover for primary
        if (hovered && style == Style.PRIMARY) {
            g2.setColor(new Color(99, 102, 241, 60));
            g2.fill(new RoundRectangle2D.Float(-4, -4, getWidth() + 8, getHeight() + 8, radius + 4, radius + 4));
        }

        g2.setColor(fill);
        g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), radius, radius));

        // Border for ghost
        if (style == Style.GHOST || style == Style.SECONDARY) {
            g2.setColor(Theme.BORDER_BRIGHT);
            g2.setStroke(new BasicStroke(1f));
            g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, radius, radius));
        }

        g2.dispose();
        super.paintComponent(g);
    }
}
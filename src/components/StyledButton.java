package components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

//Button Design
public class StyledButton extends JButton {

    public static final int PRIMARY = 0;
    public static final int SECONDARY = 1;

    private int type;
    private boolean hover = false;

    public StyledButton(String text) {
        this(text, PRIMARY);
    }

    public StyledButton(String text, int type) {
        super(text);
        this.type = type;

        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setForeground(type == PRIMARY ? Color.WHITE : Theme.TEXT);
        setFont(Theme.FONT_LABEL);
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                hover = true;
                repaint();
            }
            public void mouseExited(MouseEvent e) {
                hover = false;
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Color fill;
        if (type == PRIMARY) {
            fill = hover ? Theme.ORANGE_DARK : Theme.ORANGE;
        } else {
            fill = hover ? new Color(230, 225, 215) : Theme.CARD_BG;
        }

        g2.setColor(fill);
        g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));

        if (type == SECONDARY) {
            g2.setColor(Theme.BORDER);
            g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 8, 8));
        }

        g2.dispose();
        super.paintComponent(g);
    }
}
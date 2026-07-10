package components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

public class Navbar extends JPanel {

    public interface NavClickListener {
        void onNavClick(String section);
    }

    private String[] items = { "Home", "Rooms", "Leaderboard", "Profile" };
    private String active = "Home";
    private NavClickListener listener;
    private JPanel navItemsPanel;

    public Navbar(NavClickListener listener) {
        this.listener = listener;
        setPreferredSize(new Dimension(0, 60));
        setBackground(Theme.CARD_BG);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER));
        build();
    }

    private void build() {
        // brand section — logo icon + text, wrapped so it stays vertically centered
        JPanel brandWrap = new JPanel(new GridBagLayout());
        brandWrap.setOpaque(false);

        JPanel brandInner = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        brandInner.setOpaque(false);

        File iconFile = new File("assets/logo_icon.png");
        if (iconFile.exists()) {
            ImageIcon icon = new ImageIcon("assets/logo_icon.png");
            Image scaled = icon.getImage().getScaledInstance(32, 32, Image.SCALE_SMOOTH);
            brandInner.add(new JLabel(new ImageIcon(scaled)));
        }

        JLabel brand = new JLabel("CoCollab");
        brand.setFont(Theme.FONT_TITLE);
        brand.setForeground(Theme.TEAL);
        brandInner.add(brand);

        brandWrap.add(brandInner);
        add(brandWrap, BorderLayout.WEST);

        // center — nav items, also wrapped to stay vertically centered
        JPanel centerWrap = new JPanel(new GridBagLayout());
        centerWrap.setOpaque(false);

        navItemsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        navItemsPanel.setOpaque(false);
        for (String item : items) {
            navItemsPanel.add(makeNavLabel(item));
        }
        centerWrap.add(navItemsPanel);
        add(centerWrap, BorderLayout.CENTER);

        // right — user label
        JPanel userWrap = new JPanel(new GridBagLayout());
        userWrap.setOpaque(false);
        JLabel user = new JLabel("User  ");
        user.setFont(Theme.FONT_LABEL);
        user.setForeground(Theme.TEXT);
        userWrap.add(user);
        add(userWrap, BorderLayout.EAST);
    }

    private JLabel makeNavLabel(String item) {
        JLabel label = new JLabel(item);
        label.setFont(Theme.FONT_LABEL);
        label.setForeground(item.equals(active) ? Theme.ORANGE : Theme.TEXT_GRAY);
        label.setCursor(new Cursor(Cursor.HAND_CURSOR));
        label.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 6));

        label.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                active = item;
                refreshColors();
                if (listener != null) listener.onNavClick(item);
            }
        });
        return label;
    }

    public void setActiveSection(String section) {
        active = section;
        refreshColors();
    }

    private void refreshColors() {
        for (Component c : navItemsPanel.getComponents()) {
            if (c instanceof JLabel) {
                JLabel l = (JLabel) c;
                l.setForeground(l.getText().equals(active) ? Theme.ORANGE : Theme.TEXT_GRAY);
            }
        }
    }
}
package UI.components;
import java.awt.*;
import javax.swing.*;

public class RoundedLabel extends JLabel {
    private final int arc;

    public RoundedLabel(String text, int arc) {
        super(text);
        this.arc = arc;
        setOpaque(false); // We draw everything ourselves
        setHorizontalAlignment(CENTER); // Optional for centering
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();

        // Smooth rendering
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw rounded background
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);

        // Optional: outline
        g2.setColor(getForeground().darker());
        g2.setStroke(new BasicStroke(1f));
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);

        g2.dispose();

        paintLabelText(g);
    }

    private void paintLabelText(Graphics g) {
        super.paintComponent(g); // now safe to call — paints only text
    }
}


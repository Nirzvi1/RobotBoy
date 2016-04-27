package robotboy;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

/**
 * Created by Nirzvi on 2016-01-01.
 */
public class MouseHandler implements MouseListener, MouseMotionListener {

    @Override
    public void mouseClicked(MouseEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void mousePressed(MouseEvent e) {
        // TODO Auto-generated method stub
        int x = e.getX();
        int y = e.getY() - 20;

        if (x < Main.fieldWidth) {

            Main.code += "?||?";

            Main.points.add(x + (y * Main.f.getWidth()));

            Main.p.repaint();

            if (Main.points.size() > 1) {
                int oldX = Main.points.get(Main.points.size() - 2) % Main.f.getWidth();
                int oldY = Main.points.get(Main.points.size() - 2) / Main.f.getWidth();

                Main.convertToCode(oldX, oldY, x, y);
            }

        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        // TODO Auto-generated method stub
    }

    @Override
    public void mouseExited(MouseEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void mouseReleased(MouseEvent e) {
        // TODO Auto-generated method stub
    }

    @Override
    public void mouseDragged(MouseEvent e) {

        int x = e.getX();
        int y = e.getY() - 20;

        if (x < Main.fieldWidth) {

            Main.undoMove();
            Main.code += "?||?";

            Main.points.add(x + (y * Main.f.getWidth()));

            Main.p.repaint();

            if (Main.points.size() > 1) {
                int oldX = Main.points.get(Main.points.size() - 2) % Main.f.getWidth();
                int oldY = Main.points.get(Main.points.size() - 2) / Main.f.getWidth();

                Main.convertToCode(oldX, oldY, x, y);
            }

        }

    }

    @Override
    public void mouseMoved(MouseEvent e) {

    }
}

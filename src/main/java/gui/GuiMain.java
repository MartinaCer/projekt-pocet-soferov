package gui;

import dto.Data;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import konfiguracia.Konfiguracia;

/**
 *
 * @author Martina Cernekova
 */
public class GuiMain {

    public static void main(String[] args) {

        Konfiguracia konfiguracia = new Konfiguracia();
        Data data = new Data(konfiguracia);

        JFrame frame = new JFrame("Aplikácia");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1500, 600);

        JMenuBar mb = new JMenuBar();
        JMenu menu = new JMenu("Možnosti");
        JMenuItem i1 = new JMenuItem(new AcKonfiguracia(frame, data, konfiguracia));
        JMenuItem i2 = new JMenuItem(new AcData(frame, data));
        JMenuItem i3 = new JMenuItem(new AcVypisData(frame, data));
        JMenuItem i4 = new JMenuItem(new AcMinAutobusy(frame, data));
        JMenuItem i5 = new JMenuItem(new AcMinPrazdne(frame, data));
        JMenuItem i6 = new JMenuItem(new AcMinPrazdneGaraz(frame, data));
        JMenuItem i7 = new JMenuItem(new AcMinSoferi(frame, data));
        JMenuItem i8 = new JMenuItem(new AcMinSpoje(frame, data));
        menu.add(i1);
        menu.add(i2);
        menu.add(i3);
        menu.add(i4);
        menu.add(i5);
        menu.add(i6);
        menu.add(i7);
        menu.add(i8);
        mb.add(menu);

        frame.setJMenuBar(mb);
        frame.setVisible(true);
    }
}

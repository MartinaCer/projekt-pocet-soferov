package gui;

import dto.Data;
import dto.Zastavka;
import importExport.ImportExportDat;
import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 *
 * @author Martina Cernekova
 */
public class AcData extends AbstractAction {

    private final JFrame frame;
    private final Data data;
    private File zas;
    private File usek;
    private File spoj;

    public AcData(JFrame frame, Data data) {
        super("Načítaj dáta");
        this.frame = frame;
        this.data = data;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Runnable runnable = new Runnable() {
            public void run() {
                frame.getContentPane().removeAll();
                JPanel panel = new JPanel();
                panel.setBounds(40, 80, 200, 30);
                JPanel panelPolia = new JPanel();
                panelPolia.setLayout(new BoxLayout(panelPolia, BoxLayout.PAGE_AXIS));
                JPanel panelTlacitka = new JPanel();
                panelTlacitka.setLayout(new FlowLayout());
                JButton zasB = new JButton("zastávky");
                zasB.setBounds(50, 100, 95, 30);
                JLabel zasL = new JLabel("chýba súbor");
                JButton usekB = new JButton("úseky");
                usekB.setBounds(50, 100, 95, 30);
                JLabel usekL = new JLabel("chýba súbor");
                JButton spojB = new JButton("spoje");
                spojB.setBounds(50, 100, 95, 30);
                JLabel spojL = new JLabel("chýba súbor");
                JButton demo = new JButton("demo dáta");
                demo.setBounds(50, 100, 95, 30);
                JButton b = new JButton("načítaj");
                b.setBounds(50, 100, 95, 30);
                zasB.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        JFileChooser jfc = new JFileChooser();
                        int ret = jfc.showOpenDialog(panel);
                        if (ret == JFileChooser.APPROVE_OPTION) {
                            zasL.setText(jfc.getSelectedFile().getName());
                            zas = jfc.getSelectedFile();
                        }
                    }
                });
                usekB.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        JFileChooser jfc = new JFileChooser();
                        int ret = jfc.showOpenDialog(panel);
                        if (ret == JFileChooser.APPROVE_OPTION) {
                            usekL.setText(jfc.getSelectedFile().getName());
                            usek = jfc.getSelectedFile();
                        }
                    }
                });
                spojB.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        JFileChooser jfc = new JFileChooser();
                        int ret = jfc.showOpenDialog(panel);
                        if (ret == JFileChooser.APPROVE_OPTION) {
                            spojL.setText(jfc.getSelectedFile().getName());
                            spoj = jfc.getSelectedFile();
                        }
                    }
                });
                demo.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        try {
                            Map<Integer, Zastavka> zastavky = ImportExportDat.nacitajZastavky(null);
                            data.vytvorData(zastavky, ImportExportDat.nacitajUseky(null, zastavky), ImportExportDat.nacitajSpoje(null, zastavky));
                            JOptionPane.showMessageDialog(frame, "Hotovo.", "Načítanie dát", JOptionPane.INFORMATION_MESSAGE);
                        } catch (IOException ex) {
                            JOptionPane.showMessageDialog(frame, "Zlý formát súboru.", "Načítanie dát", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                });
                b.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (zas != null && usek != null && spoj != null) {
                            try {
                                Map<Integer, Zastavka> zastavky = ImportExportDat.nacitajZastavky(zas);
                                data.vytvorData(zastavky, ImportExportDat.nacitajUseky(usek, zastavky), ImportExportDat.nacitajSpoje(spoj, zastavky));
                                JOptionPane.showMessageDialog(frame, "Hotovo.", "Načítanie dát", JOptionPane.INFORMATION_MESSAGE);
                            } catch (IOException ex) {
                                JOptionPane.showMessageDialog(frame, "Zlý formát súboru.", "Načítanie dát", JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    }
                });
                panelPolia.add(zasB);
                panelPolia.add(zasL);
                panelPolia.add(usekB);
                panelPolia.add(usekL);
                panelPolia.add(spojB);
                panelPolia.add(spojL);
                panelTlacitka.add(demo);
                panelTlacitka.add(b);
                panel.add(panelPolia, BorderLayout.PAGE_START);
                frame.add(panelTlacitka, BorderLayout.PAGE_END);
                frame.add(panel);
                frame.revalidate();
                frame.repaint();
            }
        };
        EventQueue.invokeLater(runnable);
    }

}

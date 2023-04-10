package gui;

import dataObjekty.Data;
import dataObjekty.Zastavka;
import importExport.ImportExportDat;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import javax.swing.AbstractAction;
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
                JButton zasB = new JButton("zastávky");
                JLabel zasL = new JLabel("chýba súbor");
                JButton usekB = new JButton("úseky");
                JLabel usekL = new JLabel("chýba súbor");
                JButton spojB = new JButton("spoje");
                JLabel spojL = new JLabel("chýba súbor");
                JButton b = new JButton("načítaj");
                JButton demo = new JButton("demo dáta");
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
                        } catch (Exception ex) {
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
                            } catch (Exception ex) {
                                JOptionPane.showMessageDialog(frame, "Zlý formát súboru.", "Načítanie dát", JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    }
                });
                panel.add(zasB);
                panel.add(zasL);
                panel.add(usekB);
                panel.add(usekL);
                panel.add(spojB);
                panel.add(spojL);
                panel.add(b);
                panel.add(demo);
                frame.add(panel);
                frame.revalidate();
                frame.repaint();
            }
        };
        EventQueue.invokeLater(runnable);
    }

}

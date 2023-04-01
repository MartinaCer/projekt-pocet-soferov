package gui;

import algoritmus.Priority;
import com.itextpdf.text.DocumentException;
import dto.Data;
import gurobi.GRBException;
import gurobiModel.MinNeobsluzeneSpoje;
import gurobiModel.MinPocetSoferov;
import importExport.ImportExportDat;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 *
 * @author Martina Cernekova
 */
public class AcMinSpoje extends AbstractAction {

    private final JFrame frame;
    private final Data data;

    public AcMinSpoje(JFrame frame, Data data) {
        super("Minimálne neobslúžené spoje");
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
                JLabel autL = new JLabel("počet autobusov");
                JTextField aut = new JTextField(3);
                aut.setBounds(50, 50, 150, 20);
                JLabel sofL = new JLabel("počet šoférov");
                JTextField sof = new JTextField(3);
                sof.setBounds(50, 50, 150, 20);
                JButton b = new JButton("vypočítaj");
                b.setBounds(50, 100, 95, 30);
                b.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        try {
                            Priority.nastavPriority(data.getSpoje(), Priority.Strategia.ROVNAKE, null);
                            MinNeobsluzeneSpoje model = new MinNeobsluzeneSpoje();
                            MinNeobsluzeneSpoje.VysledokMinSpoje vysledok = model.optimalizuj(data, Integer.valueOf(aut.getText()), Integer.valueOf(sof.getText()));
                            JPanel panel = new JPanel();
                            panel.setBounds(40, 80, 200, 30);
                            JTextArea textArea = new JTextArea();
                            StringBuilder text = new StringBuilder();
                            text.append("Počet spojov: " + data.getSpoje().size() + "\n");
                            text.append("Počet šoférov: " + vysledok.getPocetSoferov() + "\n");
                            text.append("Počet turnusov: " + vysledok.getSmeny().size() + "\n");
                            text.append("Počet obslúžených spojov: " + vysledok.getPocetObsluzenych());
                            textArea.setText(text.toString());
                            JButton expZmenaB = new JButton("exportuj zmeny");
                            expZmenaB.setBounds(50, 100, 95, 30);
                            expZmenaB.addActionListener(new ActionListener() {
                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    try {
                                        ImportExportDat.vypisSmenyDoPdf(vysledok.getSmeny(), "zmeny");
                                        JOptionPane.showMessageDialog(frame, "Hotovo.", "Minimálne neobslúžené spoje", JOptionPane.INFORMATION_MESSAGE);
                                    } catch (FileNotFoundException | DocumentException ex) {
                                        JOptionPane.showMessageDialog(frame, "Chyba pri exporte.", "Minimálne neobslúžené spoje", JOptionPane.ERROR_MESSAGE);
                                    }
                                }
                            });
                            JButton expLinkaB = new JButton("exportuj linky");
                            expLinkaB.setBounds(50, 100, 95, 30);
                            expLinkaB.addActionListener(new ActionListener() {
                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    try {
                                        ImportExportDat.vypisLinkyDoPdf(vysledok.getLinky(), "linky");
                                        JOptionPane.showMessageDialog(frame, "Hotovo.", "Minimálne neobslúžené spoje", JOptionPane.INFORMATION_MESSAGE);
                                    } catch (FileNotFoundException | DocumentException ex) {
                                        JOptionPane.showMessageDialog(frame, "Chyba pri exporte.", "Minimálne neobslúžené spoje", JOptionPane.ERROR_MESSAGE);
                                    }
                                }
                            });
                            panel.add(textArea);
                            panel.add(expZmenaB);
                            panel.add(expLinkaB);
                            frame.getContentPane().removeAll();
                            frame.add(panel);
                            frame.revalidate();
                            frame.repaint();
                        } catch (GRBException ex) {
                            JOptionPane.showMessageDialog(frame, "Nie je možné vyriešiť model.", "Minimálne neobslúžené spoje", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                });
                panel.add(autL);
                panel.add(aut);
                panel.add(sofL);
                panel.add(sof);
                panel.add(b);
                frame.add(panel);
                frame.revalidate();
                frame.repaint();
            }
        };
        EventQueue.invokeLater(runnable);
    }

}

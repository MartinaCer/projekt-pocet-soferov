package gui;

import com.itextpdf.text.DocumentException;
import dataObjekty.Data;
import gurobi.GRBException;
import gurobiModel.MinPocetSoferov;
import importExport.ImportExportDat;
import java.awt.BorderLayout;
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
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 *
 * @author Martina Cernekova
 */
public class AcMinSoferi extends AbstractAction {

    private final JFrame frame;
    private final Data data;

    public AcMinSoferi(JFrame frame, Data data) {
        super("Minimálny počet šoférov");
        this.frame = frame;
        this.data = data;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Runnable runnable = new Runnable() {
            public void run() {
                frame.getContentPane().removeAll();
                if (!data.isNastaveneData()) {
                    JOptionPane.showMessageDialog(frame, "Chýbajú dáta.", "Minimálny počet šoférov", JOptionPane.ERROR_MESSAGE);
                } else {
                    JPanel panel = new JPanel();
                    panel.setBounds(40, 80, 200, 30);
                    JLabel autL = new JLabel("počet autobusov");
                    JTextField aut = new JTextField(3);
                    JButton b = new JButton("vypočítaj");
                    b.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            frame.getContentPane().removeAll();
                            MinPocetSoferov model = new MinPocetSoferov();
                            try {
                                MinPocetSoferov.VysledokMinSoferi vysledok = model.optimalizuj(data, Integer.valueOf(aut.getText()));
                                JPanel panel = new JPanel();
                                JTextArea textArea = new JTextArea();
                                textArea.setEditable(false);
                                StringBuilder text = new StringBuilder();
                                text.append("Počet spojov: " + data.getSpoje().size() + "\n");
                                text.append("Počet autobusov: " + Integer.valueOf(aut.getText()) + "\n");
                                text.append("Cena: " + vysledok.getCena() + " €\n");
                                text.append("Počet šoférov: " + vysledok.getPocetSoferov() + "\n");
                                text.append("Počet turnusov: " + vysledok.getSmeny().size());
                                textArea.setText(text.toString());
                                JButton expB = new JButton("exportuj zmeny");
                                expB.setBounds(50, 100, 95, 30);
                                expB.addActionListener(new ActionListener() {
                                    @Override
                                    public void actionPerformed(ActionEvent e) {
                                        String nazov = JOptionPane.showInputDialog(frame, "Názov súboru",
                                                "Export zmien", JOptionPane.INFORMATION_MESSAGE);
                                        if (nazov != null && !nazov.isEmpty()) {
                                            try {
                                                ImportExportDat.vypisSmenyDoPdf(vysledok.getSmeny(), nazov);
                                                JOptionPane.showMessageDialog(frame, "Hotovo.", "Minimálny počet šoférov", JOptionPane.INFORMATION_MESSAGE);
                                            } catch (FileNotFoundException | DocumentException ex) {
                                                JOptionPane.showMessageDialog(frame, "Chyba pri exporte.", "Minimálny počet šoférov", JOptionPane.ERROR_MESSAGE);
                                            }
                                        }
                                    }
                                });
                                panel.add(textArea);
                                panel.add(expB);
                                JScrollPane scTabulky = new JScrollPane(GuiTabulky.vytvorZmeny(vysledok.getSmeny()));
                                scTabulky.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
                                scTabulky.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
                                frame.add(panel, BorderLayout.PAGE_START);
                                frame.add(scTabulky);
                                frame.revalidate();
                                frame.repaint();
                            } catch (GRBException ex) {
                                JOptionPane.showMessageDialog(frame, "Nie je možné vyriešiť model.", "Minimálny počet šoférov", JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    });
                    panel.add(autL);
                    panel.add(aut);
                    panel.add(b);
                    frame.add(panel);
                    frame.revalidate();
                    frame.repaint();
                }
            }
        };
        EventQueue.invokeLater(runnable);
    }
}

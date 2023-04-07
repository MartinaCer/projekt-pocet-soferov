package gui;

import com.itextpdf.text.DocumentException;
import dto.Data;
import gurobi.GRBException;
import gurobiModel.MinPrazdnePrejazdy;
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
public class AcMinPrazdne extends AbstractAction {

    private final JFrame frame;
    private final Data data;

    public AcMinPrazdne(JFrame frame, Data data) {
        super("Minimálne prázdne prejazdy");
        this.frame = frame;
        this.data = data;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Runnable runnable = new Runnable() {
            public void run() {
                frame.getContentPane().removeAll();
                if (!data.isNastaveneData()) {
                    JOptionPane.showMessageDialog(frame, "Chýbajú dáta.", "Minimálne prázdne prejazdy", JOptionPane.ERROR_MESSAGE);
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
                            MinPrazdnePrejazdy model = new MinPrazdnePrejazdy();
                            try {
                                MinPrazdnePrejazdy.VysledokMinPrejazdy vysledok = model.optimalizuj(data, Integer.valueOf(aut.getText()));
                                JPanel panel = new JPanel();
                                JTextArea textArea = new JTextArea();
                                textArea.setEditable(false);
                                StringBuilder text = new StringBuilder();
                                text.append("Počet spojov: " + data.getSpoje().size() + "\n");
                                text.append("Počet turnusov: " + Integer.valueOf(aut.getText()) + "\n");
                                text.append("Počet prázdnych kilometrov: " + vysledok.getPocetKilometrov() + " km");
                                textArea.setText(text.toString());
                                JButton expB = new JButton("exportuj turnusy");
                                expB.setBounds(50, 100, 95, 30);
                                expB.addActionListener(new ActionListener() {
                                    @Override
                                    public void actionPerformed(ActionEvent e) {
                                        String nazov = JOptionPane.showInputDialog(frame, "Názov súboru",
                                                "Export turnusov", JOptionPane.INFORMATION_MESSAGE);
                                        if (nazov != null && !nazov.isEmpty()) {
                                            try {
                                                ImportExportDat.vypisTurnusyDoPdf(vysledok.getTurnusy(), nazov);
                                                JOptionPane.showMessageDialog(frame, "Hotovo.", "Minimálne prázdne prejazdy", JOptionPane.INFORMATION_MESSAGE);
                                            } catch (FileNotFoundException | DocumentException ex) {
                                                JOptionPane.showMessageDialog(frame, "Chyba pri exporte.", "Minimálne prázdne prejazdy", JOptionPane.ERROR_MESSAGE);
                                            }
                                        }
                                    }
                                });
                                panel.add(textArea);
                                panel.add(expB);
                                JScrollPane scTabulky = new JScrollPane(GuiTabulky.vytvorTurnusy(vysledok.getTurnusy()));
                                scTabulky.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
                                scTabulky.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
                                frame.add(panel, BorderLayout.PAGE_START);
                                frame.add(scTabulky);
                                frame.revalidate();
                                frame.repaint();
                            } catch (GRBException ex) {
                                JOptionPane.showMessageDialog(frame, "Nie je možné vyriešiť model.", "Minimálne prázdne prejazdy", JOptionPane.ERROR_MESSAGE);
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

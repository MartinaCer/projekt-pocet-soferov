package gui;

import com.itextpdf.text.DocumentException;
import dto.Data;
import gurobi.GRBException;
import gurobiModel.MinPocetAutobusov;
import gurobiModel.MinPrazdnePrejazdy;
import importExport.ImportExportDat;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.util.logging.Level;
import java.util.logging.Logger;
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
                JPanel panel = new JPanel();
                panel.setBounds(40, 80, 200, 30);
                JLabel autL = new JLabel("počet autobusov");
                JTextField aut = new JTextField(3);
                aut.setBounds(50, 50, 150, 20);
                JButton b = new JButton("vypočítaj");
                b.setBounds(50, 100, 95, 30);
                b.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        try {
                            MinPrazdnePrejazdy model = new MinPrazdnePrejazdy();
                            MinPrazdnePrejazdy.VysledokMinPrejazdy vysledok = model.optimalizuj(data, Integer.valueOf(aut.getText()));
                            JPanel panel = new JPanel();
                            panel.setBounds(40, 80, 200, 30);
                            JTextArea textArea = new JTextArea();
                            StringBuilder text = new StringBuilder();
                            text.append("Počet spojov: " + data.getSpoje().size() + "\n");
                            text.append("Počet autobusov: " + Integer.valueOf(aut.getText()) + "\n");
                            text.append("Počet prázdnych kilometrov: " + vysledok.getPocetKilometrov());
                            textArea.setText(text.toString());
                            JButton expB = new JButton("exportuj turnusy");
                            expB.setBounds(50, 100, 95, 30);
                            expB.addActionListener(new ActionListener() {
                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    try {
                                        ImportExportDat.vypisTurnusyDoPdf(vysledok.getTurnusy(), "turnusy");
                                        JOptionPane.showMessageDialog(frame, "Hotovo.", "Minimálne prázdne prejazdy", JOptionPane.INFORMATION_MESSAGE);
                                    } catch (FileNotFoundException | DocumentException ex) {
                                        JOptionPane.showMessageDialog(frame, "Chyba pri exporte.", "Minimálne prázdne prejazdy", JOptionPane.ERROR_MESSAGE);
                                    }
                                }
                            });
                            panel.add(textArea);
                            panel.add(expB);
                            frame.getContentPane().removeAll();
                            frame.add(panel);
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
        };
        EventQueue.invokeLater(runnable);
    }
}

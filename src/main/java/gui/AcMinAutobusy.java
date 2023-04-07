package gui;

import com.itextpdf.text.DocumentException;
import dto.Data;
import gurobi.GRBException;
import gurobiModel.MinPocetAutobusov;
import importExport.ImportExportDat;
import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 *
 * @author Martina Cernekova
 */
public class AcMinAutobusy extends AbstractAction {

    private final JFrame frame;
    private final Data data;

    public AcMinAutobusy(JFrame frame, Data data) {
        super("Minimálny počet autobusov");
        this.frame = frame;
        this.data = data;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Runnable runnable = new Runnable() {
            public void run() {
                frame.getContentPane().removeAll();
                if (!data.isNastaveneData()) {
                    JOptionPane.showMessageDialog(frame, "Chýbajú dáta.", "Minimálny počet autobusov", JOptionPane.ERROR_MESSAGE);
                } else {
                    MinPocetAutobusov model = new MinPocetAutobusov();
                    try {
                        MinPocetAutobusov.VysledokMinAutobusy vysledok = model.optimalizuj(data);
                        JPanel panel = new JPanel();
                        JTextArea textArea = new JTextArea();
                        textArea.setEditable(false);
                        StringBuilder text = new StringBuilder();
                        text.append("Počet spojov: " + data.getSpoje().size() + "\n");
                        text.append("Počet turnusov: " + vysledok.getPocetAutobusov());
                        textArea.setText(text.toString());
                        JButton b = new JButton("exportuj turnusy");
                        b.setBounds(50, 100, 95, 30);
                        b.addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                String nazov = JOptionPane.showInputDialog(frame, "Názov súboru",
                                        "Export turnusov", JOptionPane.INFORMATION_MESSAGE);
                                if (nazov != null && !nazov.isEmpty()) {
                                    try {
                                        ImportExportDat.vypisTurnusyDoPdf(vysledok.getTurnusy(), nazov);
                                        JOptionPane.showMessageDialog(frame, "Hotovo.", "Minimálny počet autobusov", JOptionPane.INFORMATION_MESSAGE);
                                    } catch (FileNotFoundException | DocumentException ex) {
                                        JOptionPane.showMessageDialog(frame, "Chyba pri exporte.", "Minimálny počet autobusov", JOptionPane.ERROR_MESSAGE);
                                    }
                                }
                            }
                        });
                        panel.add(textArea);
                        panel.add(b);
                        JScrollPane scTabulky = new JScrollPane(GuiTabulky.vytvorTurnusy(vysledok.getTurnusy()));
                        scTabulky.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
                        scTabulky.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
                        frame.add(panel, BorderLayout.PAGE_START);
                        frame.add(scTabulky);
                        frame.revalidate();
                        frame.repaint();
                    } catch (GRBException ex) {
                        JOptionPane.showMessageDialog(frame, "Nie je možné vyriešiť model.", "Minimálny počet autobusov", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        };
        EventQueue.invokeLater(runnable);
    }
}

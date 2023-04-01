package gui;

import com.itextpdf.text.DocumentException;
import dto.Data;
import gurobi.GRBException;
import gurobiModel.MinPocetAutobusov;
import importExport.ImportExportDat;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
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
                MinPocetAutobusov model = new MinPocetAutobusov();
                try {
                    MinPocetAutobusov.VysledokMinAutobusy vysledok = model.optimalizuj(data);
                    JPanel panel = new JPanel();
                    panel.setBounds(40, 80, 200, 30);
                    JTextArea textArea = new JTextArea();
                    StringBuilder text = new StringBuilder();
                    text.append("Počet spojov: " + data.getSpoje().size() + "\n");
                    text.append("Počet autobusov: " + vysledok.getPocetAutobusov());
                    textArea.setText(text.toString());
                    JButton b = new JButton("exportuj turnusy");
                    b.setBounds(50, 100, 95, 30);
                    b.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            try {
                                ImportExportDat.vypisTurnusyDoPdf(vysledok.getTurnusy(), "turnusy");
                                JOptionPane.showMessageDialog(frame, "Hotovo.", "Minimálny počet autobusov", JOptionPane.INFORMATION_MESSAGE);
                            } catch (FileNotFoundException | DocumentException ex) {
                                JOptionPane.showMessageDialog(frame, "Chyba pri exporte.", "Minimálny počet autobusov", JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    });
                    panel.add(textArea);
                    panel.add(b);
                    frame.add(panel);
                    frame.revalidate();
                    frame.repaint();
                } catch (GRBException ex) {
                    JOptionPane.showMessageDialog(frame, "Nie je možné vyriešiť model.", "Minimálny počet autobusov", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        EventQueue.invokeLater(runnable);
    }
}

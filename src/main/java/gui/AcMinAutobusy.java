package gui;

import com.itextpdf.text.DocumentException;
import dto.Data;
import dto.Spoj;
import gurobi.GRBException;
import gurobiModel.MinPocetAutobusov;
import importExport.ImportExportDat;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
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

                        JPanel panelTabulky = new JPanel();
                        panelTabulky.setLayout(new BoxLayout(panelTabulky, BoxLayout.PAGE_AXIS));
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
                        int poradieTurnusu = 1;
                        for (List<Spoj> turnus : vysledok.getTurnusy()) {
                            String dataSpoj[][] = new String[turnus.size()][6];
                            for (int i = 0; i < turnus.size(); i++) {
                                Spoj spoj = turnus.get(i);
                                dataSpoj[i][0] = String.valueOf(spoj.getKluc().getId());
                                dataSpoj[i][1] = String.valueOf(spoj.getKluc().getLinka());
                                dataSpoj[i][2] = spoj.getMiestoOdchodu().getId() + " - " + spoj.getMiestoOdchodu().getNazov();
                                dataSpoj[i][3] = formatter.format(spoj.getCasOdchodu());
                                dataSpoj[i][4] = spoj.getMiestoPrichodu().getId() + " - " + spoj.getMiestoPrichodu().getNazov();
                                dataSpoj[i][5] = formatter.format(spoj.getCasPrichodu());
                            }
                            String stlSpoj[] = {"id", "linka", "miesto odchodu", "čas odchodu", "miesto príchodu", "čas príchodu"};
                            JTable jtSpoj = new JTable(dataSpoj, stlSpoj);
                            jtSpoj.setPreferredScrollableViewportSize(new Dimension((int) jtSpoj.getPreferredSize().getWidth(), jtSpoj.getRowHeight() * (turnus.size())));
                            JScrollPane scSpoj = new JScrollPane(jtSpoj);
                            scSpoj.setBorder(BorderFactory.createTitledBorder("Turnus " + poradieTurnusu + ":"));
                            panelTabulky.add(scSpoj);
                            poradieTurnusu++;
                        }

                        panel.add(textArea);
                        panel.add(b);
                        JScrollPane scTabulky = new JScrollPane(panelTabulky);
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

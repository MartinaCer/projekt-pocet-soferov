package gui;

import com.itextpdf.text.DocumentException;
import dto.Data;
import dto.Spoj;
import gurobi.GRBException;
import gurobiModel.MinPrazdnePrejazdyGaraz;
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
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 *
 * @author Martina Cernekova
 */
public class AcMinPrazdneGaraz extends AbstractAction {

    private final JFrame frame;
    private final Data data;

    public AcMinPrazdneGaraz(JFrame frame, Data data) {
        super("Minimálne prázdne prejazdy s garážou");
        this.frame = frame;
        this.data = data;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Runnable runnable = new Runnable() {
            public void run() {
                frame.getContentPane().removeAll();
                if (!data.isNastaveneData()) {
                    JOptionPane.showMessageDialog(frame, "Chýbajú dáta.", "Minimálne prázdne prejazdy s garážou", JOptionPane.ERROR_MESSAGE);
                } else {
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
                            frame.getContentPane().removeAll();
                            MinPrazdnePrejazdyGaraz model = new MinPrazdnePrejazdyGaraz();
                            try {
                                MinPrazdnePrejazdyGaraz.VysledokMinPrejazdyGaraz vysledok = model.optimalizuj(data, Integer.valueOf(aut.getText()));
                                JPanel panel = new JPanel();
                                JTextArea textArea = new JTextArea();
                                textArea.setEditable(false);
                                StringBuilder text = new StringBuilder();
                                text.append("Počet spojov: " + data.getSpoje().size() + "\n");
                                text.append("Počet autobusov: " + Integer.valueOf(aut.getText()) + "\n");
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
                                                JOptionPane.showMessageDialog(frame, "Hotovo.", "Minimálne prázdne prejazdy s garážou", JOptionPane.INFORMATION_MESSAGE);
                                            } catch (FileNotFoundException | DocumentException ex) {
                                                JOptionPane.showMessageDialog(frame, "Chyba pri exporte.", "Minimálne prázdne prejazdy s garážou", JOptionPane.ERROR_MESSAGE);
                                            }
                                        }
                                    }
                                });

                                JPanel panelTabulky = new JPanel();
                                panelTabulky.setLayout(new BoxLayout(panelTabulky, BoxLayout.PAGE_AXIS));
                                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
                                int poradieTurnusu = 1;
                                for (List<Spoj> turnus : vysledok.getTurnusy()) {
                                    String dataSpoj[][] = new String[turnus.size() + 2][6];
                                    dataSpoj[0][0] = "";
                                    dataSpoj[0][1] = "";
                                    dataSpoj[0][2] = "Garáž";
                                    dataSpoj[0][3] = formatter.format(turnus.get(0).getCasOdchodu().minusSeconds(data.getCasVzdialenosti()
                                            .get(data.getKonfiguracia().getGaraz()).get(turnus.get(0).getMiestoOdchodu().getId())));
                                    dataSpoj[0][4] = turnus.get(0).getMiestoOdchodu().getId() + " - " + turnus.get(0).getMiestoOdchodu().getNazov();
                                    dataSpoj[0][5] = formatter.format(turnus.get(0).getCasOdchodu());
                                    for (int i = 0; i < turnus.size(); i++) {
                                        Spoj spoj = turnus.get(i);
                                        dataSpoj[i + 1][0] = String.valueOf(spoj.getKluc().getId());
                                        dataSpoj[i + 1][1] = String.valueOf(spoj.getKluc().getLinka());
                                        dataSpoj[i + 1][2] = spoj.getMiestoOdchodu().getId() + " - " + spoj.getMiestoOdchodu().getNazov();
                                        dataSpoj[i + 1][3] = formatter.format(spoj.getCasOdchodu());
                                        dataSpoj[i + 1][4] = spoj.getMiestoPrichodu().getId() + " - " + spoj.getMiestoPrichodu().getNazov();
                                        dataSpoj[i + 1][5] = formatter.format(spoj.getCasPrichodu());
                                    }
                                    dataSpoj[turnus.size() + 1][0] = "";
                                    dataSpoj[turnus.size() + 1][1] = "";
                                    dataSpoj[turnus.size() + 1][2] = turnus.get(turnus.size() - 1).getMiestoPrichodu().getNazov();
                                    dataSpoj[turnus.size() + 1][3] = formatter.format(turnus.get(turnus.size() - 1).getCasPrichodu());
                                    dataSpoj[turnus.size() + 1][4] = "Garáž";
                                    dataSpoj[turnus.size() + 1][5] = formatter.format(turnus.get(turnus.size() - 1).getCasPrichodu().plusSeconds(data.getCasVzdialenosti()
                                            .get(turnus.get(turnus.size() - 1).getMiestoPrichodu().getId()).get(data.getKonfiguracia().getGaraz())));
                                    String stlSpoj[] = {"id", "linka", "miesto odchodu", "čas odchodu", "miesto príchodu", "čas príchodu"};
                                    JTable jtSpoj = new JTable(dataSpoj, stlSpoj);
                                    jtSpoj.setPreferredScrollableViewportSize(new Dimension((int) jtSpoj.getPreferredSize().getWidth(), jtSpoj.getRowHeight() * (turnus.size()+2)));
                                    JScrollPane scSpoj = new JScrollPane(jtSpoj);
                                    scSpoj.setBorder(BorderFactory.createTitledBorder("Turnus " + poradieTurnusu + ":"));
                                    panelTabulky.add(scSpoj);
                                    poradieTurnusu++;
                                }

                                panel.add(textArea);
                                panel.add(expB);
                                JScrollPane scTabulky = new JScrollPane(panelTabulky);
                                scTabulky.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
                                scTabulky.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
                                frame.add(panel, BorderLayout.PAGE_START);
                                frame.add(scTabulky);
                                frame.revalidate();
                                frame.repaint();
                            } catch (GRBException ex) {
                                JOptionPane.showMessageDialog(frame, "Nie je možné vyriešiť model.", "Minimálne prázdne prejazdy s garážou", JOptionPane.ERROR_MESSAGE);
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

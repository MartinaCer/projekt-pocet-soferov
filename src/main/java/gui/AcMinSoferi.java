package gui;

import com.itextpdf.text.DocumentException;
import dto.Data;
import dto.Spoj;
import gurobi.GRBException;
import gurobiModel.MinPocetSoferov;
import gurobiModelVypisy.SmenaSofera;
import importExport.ImportExportDat;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.time.LocalTime;
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
                    aut.setBounds(50, 50, 150, 20);
                    JButton b = new JButton("vypočítaj");
                    b.setBounds(50, 100, 95, 30);
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

                                JPanel panelTabulky = new JPanel();
                                panelTabulky.setLayout(new BoxLayout(panelTabulky, BoxLayout.PAGE_AXIS));
                                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
                                int poradieTurnusu = 1;
                                for (List<SmenaSofera> turnus : vysledok.getSmeny()) {
                                    JPanel panelTurnus = new JPanel();
                                    panelTurnus.setLayout(new BoxLayout(panelTurnus, BoxLayout.PAGE_AXIS));
                                    int poradieZmeny = 1;
                                    for (SmenaSofera zmena : turnus) {
                                        String dataSpoj[][] = new String[zmena.getSpoje().size() + 2][7];
                                        dataSpoj[0][0] = "";
                                        dataSpoj[0][1] = "";
                                        dataSpoj[0][2] = "Garáž";
                                        dataSpoj[0][3] = formatter.format(zmena.getSpoje().get(0).getSpoj().getCasOdchodu().minusSeconds(zmena.getCestaZgaraze()));
                                        dataSpoj[0][4] = zmena.getSpoje().get(0).getSpoj().getMiestoOdchodu().getId() + " - " + zmena.getSpoje().get(0).getSpoj().getMiestoOdchodu().getNazov();
                                        dataSpoj[0][5] = formatter.format(zmena.getSpoje().get(0).getSpoj().getCasOdchodu());
                                        dataSpoj[0][6] = "";
                                        for (int i = 0; i < zmena.getSpoje().size(); i++) {
                                            Spoj spoj = zmena.getSpoje().get(i).getSpoj();
                                            dataSpoj[i + 1][0] = String.valueOf(spoj.getKluc().getId());
                                            dataSpoj[i + 1][1] = String.valueOf(spoj.getKluc().getLinka());
                                            dataSpoj[i + 1][2] = spoj.getMiestoOdchodu().getId() + " - " + spoj.getMiestoOdchodu().getNazov();
                                            dataSpoj[i + 1][3] = formatter.format(spoj.getCasOdchodu());
                                            dataSpoj[i + 1][4] = spoj.getMiestoPrichodu().getId() + " - " + spoj.getMiestoPrichodu().getNazov();
                                            dataSpoj[i + 1][5] = formatter.format(spoj.getCasPrichodu());
                                            dataSpoj[i + 1][6] = formatter.format(LocalTime.ofSecondOfDay(zmena.getSpoje().get(i).getPrestavkaPoSpoji()));
                                        }
                                        dataSpoj[zmena.getSpoje().size() + 1][0] = "";
                                        dataSpoj[zmena.getSpoje().size() + 1][1] = "";
                                        dataSpoj[zmena.getSpoje().size() + 1][2] = zmena.getSpoje().get(turnus.size() - 1).getSpoj().getMiestoPrichodu().getId() + " - " + zmena.getSpoje().get(turnus.size() - 1).getSpoj().getMiestoPrichodu().getNazov();
                                        dataSpoj[zmena.getSpoje().size() + 1][3] = formatter.format(zmena.getSpoje().get(zmena.getSpoje().size() - 1).getSpoj().getCasPrichodu());
                                        dataSpoj[zmena.getSpoje().size() + 1][4] = "Garáž";
                                        dataSpoj[zmena.getSpoje().size() + 1][5] = formatter.format(zmena.getSpoje().get(zmena.getSpoje().size() - 1).getSpoj().getCasPrichodu().plusSeconds(zmena.getCestaDoGaraze()));
                                        dataSpoj[zmena.getSpoje().size() + 1][6] = "";
                                        String stlSpoj[] = {"id", "linka", "miesto odchodu", "čas odchodu", "miesto príchodu", "čas príchodu", "prestávka"};
                                        JTable jtSpoj = new JTable(dataSpoj, stlSpoj);
                                        jtSpoj.setPreferredScrollableViewportSize(new Dimension((int) jtSpoj.getPreferredSize().getWidth(), jtSpoj.getRowHeight() * (zmena.getSpoje().size() + 2)));
                                        JScrollPane scSpoj = new JScrollPane(jtSpoj);
                                        scSpoj.setBorder(BorderFactory.createTitledBorder("Smena " + poradieZmeny + " - trvanie smeny " + LocalTime.ofSecondOfDay(zmena.trvanieSmeny()).format(formatter)
                                                + " - trvanie jazdy " + LocalTime.ofSecondOfDay(zmena.trvanieJazdy()).format(formatter)));
                                        panelTurnus.add(scSpoj);
                                        poradieZmeny++;
                                    }
                                    JScrollPane scTurnus = new JScrollPane(panelTurnus);
                                    scTurnus.setBorder(BorderFactory.createTitledBorder("Turnus " + poradieTurnusu + " - celkové trvanie "
                                            + LocalTime.ofSecondOfDay(turnus.get(turnus.size() - 1).koniecSmeny() - turnus.get(0).zaciatokSmeny()).format(formatter)));
                                    panelTabulky.add(scTurnus);
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

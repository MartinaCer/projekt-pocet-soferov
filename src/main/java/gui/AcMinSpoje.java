package gui;

import algoritmus.Priority;
import algoritmus.Priority.Strategia;
import com.itextpdf.text.DocumentException;
import dto.Data;
import dto.Spoj;
import gurobi.GRBException;
import gurobiModel.MinNeobsluzeneSpoje;
import gurobiModelVypisy.SmenaSofera;
import gurobiModelVypisy.SpojeLinky;
import gurobiModelVypisy.SpojeLinky.SpojLinky;
import importExport.ImportExportDat;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
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
                if (!data.isNastaveneData()) {
                    JOptionPane.showMessageDialog(frame, "Chýbajú dáta.", "Minimálne neobslúžené spoje", JOptionPane.ERROR_MESSAGE);
                } else {
                    JPanel panel = new JPanel();
                    panel.setBounds(40, 80, 200, 30);
                    JLabel priL = new JLabel("spôsob nastavenia priorít");
                    JComboBox pri = new JComboBox(Priority.Strategia.values());
                    JLabel autL = new JLabel("počet autobusov");
                    JTextField aut = new JTextField(3);
                    aut.setBounds(50, 50, 150, 20);
                    JLabel sofL = new JLabel("počet šoférov");
                    JTextField sof = new JTextField(3);
                    sof.setBounds(50, 50, 150, 20);
                    JLabel casL = new JLabel("časový limit [s]");
                    JTextField cas = new JTextField(5);
                    cas.setBounds(50, 50, 150, 20);
                    JButton b = new JButton("vypočítaj");
                    b.setBounds(50, 100, 95, 30);
                    b.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            frame.getContentPane().removeAll();
                            Priority.nastavPriority(data.getSpoje(), (Strategia) pri.getSelectedItem(), null);
                            MinNeobsluzeneSpoje model = new MinNeobsluzeneSpoje();
                            try {
                                MinNeobsluzeneSpoje.VysledokMinSpoje vysledok = model.optimalizuj(data,
                                        Integer.valueOf(aut.getText()), Integer.valueOf(sof.getText()), Integer.valueOf(cas.getText()));
                                JPanel panel = new JPanel();
                                JTextArea textArea = new JTextArea();
                                textArea.setEditable(false);
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
                                        String nazov = JOptionPane.showInputDialog(frame, "Názov súboru",
                                                "Export zmien", JOptionPane.INFORMATION_MESSAGE);
                                        if (nazov != null && !nazov.isEmpty()) {
                                            try {
                                                ImportExportDat.vypisSmenyDoPdf(vysledok.getSmeny(), nazov);
                                                JOptionPane.showMessageDialog(frame, "Hotovo.", "Minimálne neobslúžené spoje", JOptionPane.INFORMATION_MESSAGE);
                                            } catch (FileNotFoundException | DocumentException ex) {
                                                JOptionPane.showMessageDialog(frame, "Chyba pri exporte.", "Minimálne neobslúžené spoje", JOptionPane.ERROR_MESSAGE);
                                            }
                                        }
                                    }
                                });
                                JButton expLinkaB = new JButton("exportuj linky");
                                expLinkaB.setBounds(50, 100, 95, 30);
                                expLinkaB.addActionListener(new ActionListener() {
                                    @Override
                                    public void actionPerformed(ActionEvent e) {
                                        String nazov = JOptionPane.showInputDialog(frame, "Názov súboru",
                                                "Export liniek", JOptionPane.INFORMATION_MESSAGE);
                                        if (nazov != null && !nazov.isEmpty()) {
                                            try {
                                                ImportExportDat.vypisLinkyDoPdf(vysledok.getLinky(), nazov);
                                                JOptionPane.showMessageDialog(frame, "Hotovo.", "Minimálne neobslúžené spoje", JOptionPane.INFORMATION_MESSAGE);
                                            } catch (FileNotFoundException | DocumentException ex) {
                                                JOptionPane.showMessageDialog(frame, "Chyba pri exporte.", "Minimálne neobslúžené spoje", JOptionPane.ERROR_MESSAGE);
                                            }
                                        }
                                    }
                                });

                                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
                                JPanel panelZmeny = new JPanel();
                                panelZmeny.setLayout(new BoxLayout(panelZmeny, BoxLayout.PAGE_AXIS));
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
                                    panelZmeny.add(scTurnus);
                                    poradieTurnusu++;
                                }

                                JPanel panelLinky = new JPanel();
                                panelLinky.setLayout(new BoxLayout(panelLinky, BoxLayout.PAGE_AXIS));
                                for (SpojeLinky spojeLinky : vysledok.getLinky()) {
                                    JPanel panelSmer = new JPanel();
                                    panelSmer.setLayout(new BoxLayout(panelSmer, BoxLayout.PAGE_AXIS));
                                    int poradieSmeru = 1;
                                    for (List<SpojLinky> spojLinky : spojeLinky.getSpoje().values()) {
                                        Collections.sort(spojLinky, Comparator.comparing(sp -> sp.getSpoj().getCasOdchodu()));
                                        String dataSpoj[][] = new String[spojLinky.size()][6];
                                        for (int i = 0; i < spojLinky.size(); i++) {
                                            Spoj spoj = spojLinky.get(i).getSpoj();
                                            dataSpoj[i][0] = String.valueOf(spoj.getKluc().getId());
                                            dataSpoj[i][1] = spoj.getMiestoOdchodu().getId() + " - " + spoj.getMiestoOdchodu().getNazov();
                                            dataSpoj[i][2] = formatter.format(spoj.getCasOdchodu());
                                            dataSpoj[i][3] = spoj.getMiestoPrichodu().getId() + " - " + spoj.getMiestoPrichodu().getNazov();
                                            dataSpoj[i][4] = formatter.format(spoj.getCasPrichodu());
                                            dataSpoj[i][5] = spojLinky.get(i).isObsluzeny() ? "Áno" : "Nie";
                                        }
                                        String stlSpoj[] = {"spoj", "miesto odchodu", "čas odchodu", "miesto príchodu", "čas príchodu", "obslúžený"};
                                        JTable jtSpoj = new JTable(dataSpoj, stlSpoj);
                                        jtSpoj.setPreferredScrollableViewportSize(new Dimension((int) jtSpoj.getPreferredSize().getWidth(), jtSpoj.getRowHeight() * (spojLinky.size())));
                                        JScrollPane scSpoj = new JScrollPane(jtSpoj);
                                        scSpoj.setBorder(BorderFactory.createTitledBorder(BorderFactory.createTitledBorder("Smer " + poradieSmeru + " - obslúžených " + spojLinky.stream().filter(s -> s.isObsluzeny()).count()
                                                + " z " + spojLinky.size() + " spojov")));
                                        panelSmer.add(scSpoj);
                                        poradieSmeru++;
                                    }
                                    JScrollPane scTurnus = new JScrollPane(panelSmer);
                                    scTurnus.setBorder(BorderFactory.createTitledBorder(BorderFactory.createTitledBorder("Linka " + spojeLinky.getLinka())));
                                    panelLinky.add(scTurnus);
                                    poradieTurnusu++;
                                }

                                panel.add(textArea);
                                panel.add(expZmenaB);
                                panel.add(expLinkaB);
                                JScrollPane scZmeny = new JScrollPane(panelZmeny);
                                scZmeny.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
                                scZmeny.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
                                JScrollPane scLinky = new JScrollPane(panelLinky);
                                scLinky.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
                                scLinky.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
                                JTabbedPane zalozky = new JTabbedPane();
                                zalozky.add("zmeny", scZmeny);
                                zalozky.add("linky", scLinky);
                                frame.add(panel, BorderLayout.PAGE_START);
                                frame.add(zalozky);
                                frame.revalidate();
                                frame.repaint();
                            } catch (GRBException ex) {
                                JOptionPane.showMessageDialog(frame, "Nie je možné vyriešiť model.", "Minimálne neobslúžené spoje", JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    });
                    panel.add(priL);
                    panel.add(pri);
                    panel.add(autL);
                    panel.add(aut);
                    panel.add(sofL);
                    panel.add(sof);
                    panel.add(casL);
                    panel.add(cas);
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

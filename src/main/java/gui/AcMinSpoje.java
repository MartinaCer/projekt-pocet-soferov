package gui;

import algoritmus.Priority;
import static algoritmus.Priority.P1;
import static algoritmus.Priority.P2;
import algoritmus.Priority.Strategia;
import static algoritmus.Priority.Strategia.PRVY_POSLEDNY;
import static algoritmus.Priority.Strategia.RUCNE;
import com.itextpdf.text.DocumentException;
import dataObjekty.Data;
import dataObjekty.Spoj;
import dataObjekty.Spoj.KlucSpoja;
import gurobi.GRBException;
import gurobiModel.MinNeobsluzeneSpoje;
import gurobiModelVypisy.SpojeLinky;
import gurobiModelVypisy.SpojeLinky.SpojLinky;
import importExport.ImportExportDat;
import importExport.ImportExportDat.PriorityImport;
import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
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
    private File subor;

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
                    JLabel sofL = new JLabel("počet vodičov");
                    JTextField sof = new JTextField(3);
                    JLabel casL = new JLabel("časový limit [s]");
                    JTextField cas = new JTextField(5);
                    JButton b = new JButton("vypočítaj");
                    b.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            frame.getContentPane().removeAll();
                            if ((Strategia) pri.getSelectedItem() == Strategia.RUCNE) {
                                JPanel stPanel = new JPanel();
                                JButton subB = new JButton("priority");
                                JLabel subL = new JLabel("chýba súbor");
                                JButton impB = new JButton("importuj");
                                JButton demoB = new JButton("demo priority");
                                JButton rucB = new JButton("ručne");
                                subB.addActionListener(new ActionListener() {
                                    @Override
                                    public void actionPerformed(ActionEvent e) {
                                        JFileChooser jfc = new JFileChooser();
                                        int ret = jfc.showOpenDialog(panel);
                                        if (ret == JFileChooser.APPROVE_OPTION) {
                                            subL.setText(jfc.getSelectedFile().getName());
                                            subor = jfc.getSelectedFile();
                                        }
                                    }
                                });
                                impB.addActionListener(new ActionListener() {
                                    @Override
                                    public void actionPerformed(ActionEvent e) {
                                        if (subor != null) {
                                            try {
                                                PriorityImport priority = ImportExportDat.naciatajPriority(subor);
                                                JOptionPane.showMessageDialog(frame, "Hotovo.", "Načítanie dát", JOptionPane.INFORMATION_MESSAGE);
                                                String predvolena = JOptionPane.showInputDialog(frame, "Predvolená priorita",
                                                        "Ručné nastavenie priorít", JOptionPane.INFORMATION_MESSAGE);
                                                vypocitajRucne(Integer.valueOf(aut.getText()), Integer.valueOf(sof.getText()), Integer.valueOf(cas.getText()),
                                                        Integer.valueOf(predvolena), priority);
                                            } catch (Exception ex) {
                                                JOptionPane.showMessageDialog(frame, "Zlý formát súboru.", "Načítanie dát", JOptionPane.ERROR_MESSAGE);
                                            }
                                        }
                                    }
                                });
                                demoB.addActionListener(new ActionListener() {
                                    @Override
                                    public void actionPerformed(ActionEvent e) {
                                        try {
                                            PriorityImport priority = ImportExportDat.naciatajPriority(null);
                                            JOptionPane.showMessageDialog(frame, "Hotovo.", "Načítanie dát", JOptionPane.INFORMATION_MESSAGE);
                                            String predvolena = JOptionPane.showInputDialog(frame, "Predvolená priorita",
                                                    "Ručné nastavenie priorít", JOptionPane.INFORMATION_MESSAGE);
                                            vypocitajRucne(Integer.valueOf(aut.getText()), Integer.valueOf(sof.getText()), Integer.valueOf(cas.getText()),
                                                    Integer.valueOf(predvolena), priority);
                                        } catch (Exception ex) {
                                            JOptionPane.showMessageDialog(frame, "Zlý formát súboru.", "Načítanie dát", JOptionPane.ERROR_MESSAGE);
                                        }
                                    }
                                });
                                rucB.addActionListener(new ActionListener() {
                                    @Override
                                    public void actionPerformed(ActionEvent e) {
                                        String predvolena = JOptionPane.showInputDialog(frame, "Predvolená priorita",
                                                "Ručné nastavenie priorít", JOptionPane.INFORMATION_MESSAGE);
                                        vypocitajRucne(Integer.valueOf(aut.getText()), Integer.valueOf(sof.getText()), Integer.valueOf(cas.getText()),
                                                Integer.valueOf(predvolena), null);
                                    }
                                });
                                stPanel.add(subB);
                                stPanel.add(subL);
                                stPanel.add(impB);
                                stPanel.add(demoB);
                                stPanel.add(rucB);
                                frame.add(stPanel);
                                frame.revalidate();
                                frame.repaint();
                            } else {
                                Priority.nastavPriority(data.getSpoje(), (Strategia) pri.getSelectedItem(), null, 0);
                                vypocitajModel(Integer.valueOf(aut.getText()), Integer.valueOf(sof.getText()), Integer.valueOf(cas.getText()), (Strategia) pri.getSelectedItem());
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

    private void vypocitajRucne(int pocetAutobusov, int pocetSoferov, int limit, int predvolena, PriorityImport priority) {
        frame.getContentPane().removeAll();
        JPanel impPanel = new JPanel(new BorderLayout());
        JButton vypB = new JButton("vypočítaj");
        vypB.setBounds(50, 100, 95, 30);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        List<Spoj> spoje = data.getSpoje() == null ? Collections.emptyList() : new ArrayList<>(data.getSpoje().values());
        Collections.sort(spoje, Comparator.comparing(s -> s.getKluc()));
        String dataSpoj[][] = new String[spoje.size()][9];
        for (int i = 0; i < spoje.size(); i++) {
            Spoj spoj = spoje.get(i);
            dataSpoj[i][0] = String.valueOf(spoj.getKluc().getId());
            dataSpoj[i][1] = String.valueOf(spoj.getKluc().getLinka());
            dataSpoj[i][2] = spoj.getMiestoOdchodu().getId() + " - " + spoj.getMiestoOdchodu().getNazov();
            dataSpoj[i][3] = formatter.format(spoj.getCasOdchodu());
            dataSpoj[i][4] = spoj.getMiestoPrichodu().getId() + " - " + spoj.getMiestoPrichodu().getNazov();
            dataSpoj[i][5] = formatter.format(spoj.getCasPrichodu());
            dataSpoj[i][6] = String.valueOf(spoj.getKilometre());
            dataSpoj[i][7] = priority == null ? "" : priority.getPriority().containsKey(spoj.getKluc()) ? String.valueOf(priority.getPriority().get(spoj.getKluc())) : "";
            dataSpoj[i][8] = priority == null ? "" : priority.getMusiObsluzit().contains(spoj.getKluc()) ? "1" : "";
        }
        String stlSpoj[] = {"id", "linka", "miesto odchodu", "čas odchodu", "miesto príchodu", "čas príchodu", "vzdialenosť [km]", "priorita", "musí obslúžiť"};
        JTable jtSpoj = new JTable(dataSpoj, stlSpoj);
        JScrollPane spSpoj = new JScrollPane(jtSpoj);
        spSpoj.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        spSpoj.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        vypB.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Map<KlucSpoja, Integer> priority = new HashMap<>();
                for (int i = 0; i < jtSpoj.getRowCount(); i++) {
                    KlucSpoja kluc = new KlucSpoja(Integer.valueOf((String) jtSpoj.getValueAt(i, 0)), Integer.valueOf((String) jtSpoj.getValueAt(i, 1)));
                    String hodnota = (String) jtSpoj.getValueAt(i, 7);
                    String musiObsluzit = (String) jtSpoj.getValueAt(i, 8);
                    if (!hodnota.isEmpty()) {
                        priority.put(kluc, Integer.valueOf((String) jtSpoj.getValueAt(i, 7)));
                    }
                    data.getSpoje().get(kluc).setMusiObsluzit(musiObsluzit.equals("1"));
                }
                Priority.nastavPriority(data.getSpoje(), RUCNE, priority, predvolena);
                vypocitajModel(pocetAutobusov, pocetSoferov, limit, RUCNE);
            }
        });
        impPanel.add(spSpoj, BorderLayout.CENTER);
        impPanel.add(vypB, BorderLayout.PAGE_END);
        frame.add(impPanel);
        frame.revalidate();
        frame.repaint();
    }

    private void vypocitajModel(int pocetAutobusov, int pocetSoferov, int limit, Strategia strategia) {
        frame.getContentPane().removeAll();
        MinNeobsluzeneSpoje model = new MinNeobsluzeneSpoje();
        try {
            MinNeobsluzeneSpoje.VysledokMinSpoje vysledok = model.optimalizuj(data,
                    pocetAutobusov, pocetSoferov, limit);
            JPanel panel = new JPanel();
            JTextArea textArea = new JTextArea();
            textArea.setEditable(false);
            StringBuilder text = new StringBuilder();
            text.append("Počet spojov: " + data.getSpoje().size() + "\n");
            text.append("Počet vodičov: " + vysledok.getPocetSoferov() + "\n");
            text.append("Počet turnusov: " + vysledok.getSmeny().size() + "\n");
            text.append("Počet obslúžených spojov: " + vysledok.getPocetObsluzenych());
            switch (strategia) {
                case ROVNAKE:
                    break;
                case PRVY_POSLEDNY:
                    int pocetNastavenych = 0;
                    int pocetObsluzenych = 0;
                    for (SpojeLinky spojeLinky : vysledok.getLinky()) {
                        for (List<SpojLinky> linka : spojeLinky.getSpoje().values()) {
                            for (SpojLinky spoj : linka) {
                                if (spoj.getSpoj().getPriorita() == P2) {
                                    pocetNastavenych++;
                                    if (spoj.isObsluzeny()) {
                                        pocetObsluzenych++;
                                    }
                                }
                            }
                        }
                    }
                    text.append("\nPočet spojov s prioritou: " + pocetNastavenych + "\n");
                    text.append("Počet obslúžených spojov s prioritou: " + pocetObsluzenych);
                    break;
                case KAZDY_DRUHY:
                    int pocetNastavenych2 = 0;
                    int pocetObsluzenych2 = 0;
                    for (SpojeLinky spojeLinky : vysledok.getLinky()) {
                        for (List<SpojLinky> linka : spojeLinky.getSpoje().values()) {
                            for (SpojLinky spoj : linka) {
                                if (spoj.getSpoj().getPriorita() >= P2) {
                                    pocetNastavenych2++;
                                    if (spoj.isObsluzeny()) {
                                        pocetObsluzenych2++;
                                    }
                                }
                            }
                        }
                    }
                    text.append("\nPočet spojov s prioritou: " + pocetNastavenych2 + "\n");
                    text.append("Počet obslúžených spojov s prioritou: " + pocetObsluzenych2);
                    break;
                case PRVY_POSLEDNY_KAZDY_DRUHY:
                    int pocetNastavenychTop = 0;
                    int pocetObsluzenychTop = 0;
                    int pocetNastavenych3 = 0;
                    int pocetObsluzenych3 = 0;
                    for (SpojeLinky spojeLinky : vysledok.getLinky()) {
                        for (List<SpojLinky> linka : spojeLinky.getSpoje().values()) {
                            for (SpojLinky spoj : linka) {
                                if (spoj.getSpoj().getPriorita() == P1) {
                                    pocetNastavenychTop++;
                                    if (spoj.isObsluzeny()) {
                                        pocetObsluzenychTop++;
                                    }
                                }
                                else if (spoj.getSpoj().getPriorita() >= P2) {
                                    pocetNastavenych3++;
                                    if (spoj.isObsluzeny()) {
                                        pocetObsluzenych3++;
                                    }
                                }
                            }
                        }
                    }
                    text.append("\nPočet spojov s top prioritou: " + pocetNastavenychTop + "\n");
                    text.append("Počet obslúžených spojov s top prioritou: " + pocetObsluzenychTop + "\n");
                    text.append("Počet spojov s prioritou: " + pocetNastavenych3 + "\n");
                    text.append("Počet obslúžených spojov s prioritou: " + pocetObsluzenych3);
                    break;
                case RUCNE:
                    break;
                default:
                    break;
            }
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
            panel.add(textArea);
            panel.add(expZmenaB);
            panel.add(expLinkaB);
            JScrollPane scZmeny = new JScrollPane(GuiTabulky.vytvorZmeny(vysledok.getSmeny()));
            scZmeny.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            scZmeny.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            JScrollPane scLinky = new JScrollPane(GuiTabulky.vytvorLinky(vysledok.getLinky()));
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
}

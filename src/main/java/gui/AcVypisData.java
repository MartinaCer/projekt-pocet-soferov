package gui;

import dataObjekty.Data;
import dataObjekty.Spoj;
import dataObjekty.Usek;
import dataObjekty.Zastavka;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;

/**
 *
 * @author Martina Cernekova
 */
public class AcVypisData extends AbstractAction {

    private final JFrame frame;
    private final Data data;

    public AcVypisData(JFrame frame, Data data) {
        super("Vypíš dáta");
        this.frame = frame;
        this.data = data;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Runnable runnable = new Runnable() {
            public void run() {
                frame.getContentPane().removeAll();
                JTabbedPane zalozky = new JTabbedPane();

                List<Zastavka> zastavky = data.getZastavky() == null ? Collections.emptyList() : new ArrayList<>(data.getZastavky().values());
                String dataZas[][] = new String[zastavky.size()][2];
                for (int i = 0; i < zastavky.size(); i++) {
                    Zastavka zas = zastavky.get(i);
                    dataZas[i][0] = String.valueOf(zas.getId());
                    dataZas[i][1] = zas.getNazov();
                }
                String stlZas[] = {"id", "názov"};
                JTable jtZas = new JTable(dataZas, stlZas);
                jtZas.setBounds(30, 40, 200, 100);
                JScrollPane spZas = new JScrollPane(jtZas);
                spZas.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
                spZas.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

                List<Usek> useky = data.getUseky() == null ? Collections.emptyList() : data.getUseky();
                String dataUsek[][] = new String[useky.size()][4];
                for (int i = 0; i < useky.size(); i++) {
                    Usek usek = useky.get(i);
                    dataUsek[i][0] = usek.getZaciatok().getId() + " - " + usek.getZaciatok().getNazov();
                    dataUsek[i][1] = usek.getKoniec().getId() + " - " + usek.getKoniec().getNazov();
                    dataUsek[i][2] = String.valueOf(usek.getSekundy());
                    dataUsek[i][3] = String.valueOf(usek.getKilometre());
                }
                String stlUsek[] = {"začiatok", "koniec", "trvanie [s]", "vzdialenosť [km]"};
                JTable jtUsek = new JTable(dataUsek, stlUsek);
                jtUsek.setBounds(30, 40, 200, 100);
                JScrollPane spUsek = new JScrollPane(jtUsek);
                spUsek.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
                spUsek.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
                List<Spoj> spoje = data.getSpoje() == null ? Collections.emptyList() : new ArrayList<>(data.getSpoje().values());
                Collections.sort(spoje, Comparator.comparing(s -> s.getKluc()));
                String dataSpoj[][] = new String[spoje.size()][7];
                for (int i = 0; i < spoje.size(); i++) {
                    Spoj spoj = spoje.get(i);
                    dataSpoj[i][0] = String.valueOf(spoj.getKluc().getId());
                    dataSpoj[i][1] = String.valueOf(spoj.getKluc().getLinka());
                    dataSpoj[i][2] = spoj.getMiestoOdchodu().getId() + " - " + spoj.getMiestoOdchodu().getNazov();
                    dataSpoj[i][3] = formatter.format(spoj.getCasOdchodu());
                    dataSpoj[i][4] = spoj.getMiestoPrichodu().getId() + " - " + spoj.getMiestoPrichodu().getNazov();
                    dataSpoj[i][5] = formatter.format(spoj.getCasPrichodu());
                    dataSpoj[i][6] = String.valueOf(spoj.getKilometre());
                }
                String stlSpoj[] = {"id", "linka", "miesto odchodu", "čas odchodu", "miesto príchodu", "čas príchodu", "vzdialenosť [km]"};
                JTable jtSpoj = new JTable(dataSpoj, stlSpoj);
                jtSpoj.setBounds(30, 40, 200, 100);
                JScrollPane spSpoj = new JScrollPane(jtSpoj);
                spSpoj.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
                spSpoj.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

                zalozky.add("zastávky", spZas);
                zalozky.add("úseky", spUsek);
                zalozky.add("spoje", spSpoj);
                frame.add(zalozky);
                frame.revalidate();
                frame.repaint();
            }
        };
        EventQueue.invokeLater(runnable);
    }
}

package gui;

import dto.Data;
import dto.Spoj;
import gurobiModelVypisy.SmenaSofera;
import gurobiModelVypisy.SpojeLinky;
import java.awt.Dimension;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

/**
 *
 * @author Martina Cernekova
 */
public final class GuiTabulky {

    private GuiTabulky() {
    }

    public static JPanel vytvorTurnusy(List<List<Spoj>> turnusy) {
        JPanel panelTabulky = new JPanel();
        panelTabulky.setLayout(new BoxLayout(panelTabulky, BoxLayout.PAGE_AXIS));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        int poradieTurnusu = 1;
        for (List<Spoj> turnus : turnusy) {
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
        return panelTabulky;
    }

    public static JPanel vytvorTurnusyGaraz(List<List<Spoj>> turnusy, Data data) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        JPanel panelTabulky = new JPanel();
        panelTabulky.setLayout(new BoxLayout(panelTabulky, BoxLayout.PAGE_AXIS));
        int poradieTurnusu = 1;
        for (List<Spoj> turnus : turnusy) {
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
            jtSpoj.setPreferredScrollableViewportSize(new Dimension((int) jtSpoj.getPreferredSize().getWidth(), jtSpoj.getRowHeight() * (turnus.size() + 2)));
            JScrollPane scSpoj = new JScrollPane(jtSpoj);
            scSpoj.setBorder(BorderFactory.createTitledBorder("Turnus " + poradieTurnusu + ":"));
            panelTabulky.add(scSpoj);
            poradieTurnusu++;
        }
        return panelTabulky;
    }

    public static JPanel vytvorZmeny(List<List<SmenaSofera>> zmeny) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        JPanel panelZmeny = new JPanel();
        panelZmeny.setLayout(new BoxLayout(panelZmeny, BoxLayout.PAGE_AXIS));
        int poradieTurnusu = 1;
        for (List<SmenaSofera> turnus : zmeny) {
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
        return panelZmeny;
    }

    public static JPanel vytvorLinky(List<SpojeLinky> linky) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        JPanel panelLinky = new JPanel();
        panelLinky.setLayout(new BoxLayout(panelLinky, BoxLayout.PAGE_AXIS));
        for (SpojeLinky spojeLinky : linky) {
            JPanel panelSmer = new JPanel();
            panelSmer.setLayout(new BoxLayout(panelSmer, BoxLayout.PAGE_AXIS));
            int poradieSmeru = 1;
            for (List<SpojeLinky.SpojLinky> spojLinky : spojeLinky.getSpoje().values()) {
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
        }
        return panelLinky;
    }
}

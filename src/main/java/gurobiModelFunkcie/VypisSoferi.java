package gurobiModelFunkcie;

import dto.Spoj;
import dto.Spoj.KlucSpoja;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Martina Cernekova
 */
public class VypisSoferi {

    public static List<List<SpojSofer>> vytvorTurnusy(List<String> nasledujuceSpoje, Map<KlucSpoja, Spoj> spoje) {
        List<List<SpojSofer>> turnusy = new ArrayList<>();
        for (String spoj : nasledujuceSpoje) {
            String[] data = spoj.split("_");
            String[] sISpoj = data[1].split(";");
            String[] sJSpoj = data[2].split(";");
            boolean zmena = data[0].charAt(0) == 'y';
            KlucSpoja iSpoj = new KlucSpoja(Integer.valueOf(sISpoj[0]), Integer.valueOf(sISpoj[1]));
            KlucSpoja jSpoj = new KlucSpoja(Integer.valueOf(sJSpoj[0]), Integer.valueOf(sJSpoj[1]));
            boolean novyTurnus = true;
            for (List<SpojSofer> turnus : turnusy) {
                if (turnus.get(turnus.size() - 1).spoj.getKluc().equals(iSpoj)) {
                    turnus.add(new SpojSofer(spoje.get(jSpoj), zmena));
                    novyTurnus = false;
                    break;
                }
                if (turnus.get(0).spoj.getKluc().equals(jSpoj)) {
                    turnus.add(0, new SpojSofer(spoje.get(iSpoj), zmena));
                    novyTurnus = false;
                    break;
                }
            }
            if (novyTurnus) {
                turnusy.add(new ArrayList<>(Arrays.asList(new SpojSofer(spoje.get(iSpoj), zmena), new SpojSofer(spoje.get(jSpoj), zmena))));
            } else {
                int index = -1;
                for (int i = 0; i < turnusy.size(); i++) {
                    List<SpojSofer> turnus = turnusy.get(i);
                    SpojSofer prvy = turnus.get(0);
                    SpojSofer posledny = turnus.get(turnus.size() - 1);
                    for (int j = 0; j < turnusy.size(); j++) {
                        List<SpojSofer> turnus2 = turnusy.get(j);
                        SpojSofer prvy2 = turnus2.get(0);
                        SpojSofer posledny2 = turnus2.get(turnus2.size() - 1);
                        if (prvy.spoj.getKluc().equals(posledny2.spoj.getKluc())) {
                            turnus2.remove(posledny2);
                            turnus.addAll(0, turnus2);
                            index = j;
                            break;
                        }
                        if (posledny.spoj.getKluc().equals(prvy2.spoj.getKluc())) {
                            turnus2.remove(prvy2);
                            turnus.addAll(turnus2);
                            index = j;
                            break;
                        }
                    }
                }
                if (index != -1) {
                    turnusy.remove(index);
                }
            }
        }
        return turnusy;
    }

    public static void vypisTurnusy(List<List<SpojSofer>> turnusy, Map<KlucSpoja, Double> tHodnoty, Map<Integer, Map<Integer, Integer>> vzdialenosti, int idGaraze) {
        System.out.println("Turnusy:");
        for (int i = 0; i < turnusy.size(); i++) {
            String turnus = i + 1 + ": jazda z garáže " + vzdialenosti.get(idGaraze).get(turnusy.get(i).get(0).getSpoj().getMiestoOdchodu().getId()) + " -> ";
            for (int j = 0; j < turnusy.get(i).size(); j++) {
                SpojSofer spoj = turnusy.get(i).get(j);
                turnus += (spoj.zmeneny
                        ? "jazda do garáže " + vzdialenosti.get(turnusy.get(i).get(j - 1).getSpoj().getMiestoPrichodu().getId()).get(idGaraze)
                        + " -> nový šofér -> jazda z garáže " + vzdialenosti.get(idGaraze).get(spoj.spoj.getMiestoOdchodu().getId()) + " -> "
                        : "")
                        + spoj.spoj.getKluc().toString()
                        + "(" + spoj.spoj.getCasOdchodu() + ";" + spoj.spoj.getCasPrichodu()
                        + ") tHodnota-" + tHodnoty.get(spoj.spoj.getKluc()) + " -> ";
            }
            turnus += "jazda do garáže " + vzdialenosti.get(turnusy.get(i).get(turnusy.get(i).size() - 1).getSpoj().getMiestoPrichodu().getId()).get(idGaraze);
            System.out.println(turnus);
        }
    }

    public static class SpojSofer {

        private final Spoj spoj;
        private final boolean zmeneny;

        public SpojSofer(Spoj spoj, boolean zmeneny) {
            this.spoj = spoj;
            this.zmeneny = zmeneny;
        }

        public Spoj getSpoj() {
            return spoj;
        }

        public boolean getZmeneny() {
            return zmeneny;
        }

    }
}

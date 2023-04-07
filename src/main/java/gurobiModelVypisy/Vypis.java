package gurobiModelVypisy;

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
public final class Vypis {

    private Vypis() {
    }

    public static List<List<Spoj>> vytvorTurnusy(List<String> nasledujuceSpoje, Map<KlucSpoja, Spoj> spoje) {
        List<List<Spoj>> turnusy = new ArrayList<>();
        for (String spoj : nasledujuceSpoje) {
            String[] data = spoj.split("_");
            String[] sISpoj = data[1].split(";");
            String[] sJSpoj = data[2].split(";");
            KlucSpoja iSpoj = new KlucSpoja(Integer.valueOf(sISpoj[0]), Integer.valueOf(sISpoj[1]));
            KlucSpoja jSpoj = new KlucSpoja(Integer.valueOf(sJSpoj[0]), Integer.valueOf(sJSpoj[1]));
            boolean novyTurnus = true;
            for (List<Spoj> turnus : turnusy) {
                if (turnus.get(turnus.size() - 1).getKluc().equals(iSpoj)) {
                    turnus.add(spoje.get(jSpoj));
                    novyTurnus = false;
                    break;
                }
                if (turnus.get(0).getKluc().equals(jSpoj)) {
                    turnus.add(0, spoje.get(iSpoj));
                    novyTurnus = false;
                    break;
                }
            }
            if (novyTurnus) {
                turnusy.add(new ArrayList<>(Arrays.asList(spoje.get(iSpoj), spoje.get(jSpoj))));
            } else {
                int index = -1;
                for (int i = 0; i < turnusy.size(); i++) {
                    List<Spoj> turnus = turnusy.get(i);
                    Spoj prvy = turnus.get(0);
                    Spoj posledny = turnus.get(turnus.size() - 1);
                    for (int j = 0; j < turnusy.size(); j++) {
                        List<Spoj> turnus2 = turnusy.get(j);
                        Spoj prvy2 = turnus2.get(0);
                        Spoj posledny2 = turnus2.get(turnus2.size() - 1);
                        if (prvy.getKluc().equals(posledny2.getKluc())) {
                            turnus2.remove(posledny2);
                            turnus.addAll(0, turnus2);
                            index = j;
                            break;
                        }
                        if (posledny.getKluc().equals(prvy2.getKluc())) {
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
}

package gurobiModel;

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
public final class VypisyPreModel {

    private VypisyPreModel() {
    }

    public static List<List<Spoj>> vytvorTurnusy(List<String> nasledujuceSpoje, Map<KlucSpoja, Spoj> spoje) {
        List<List<Spoj>> turnusy = new ArrayList<>();
        for (String spoj : nasledujuceSpoje) {
            String[] data = spoj.split("_");
            String[] sZaciatok = data[1].split(";");
            String[] sKoniec = data[2].split(";");
            KlucSpoja zaciatok = new KlucSpoja(Integer.valueOf(sZaciatok[0]), Integer.valueOf(sZaciatok[1]));
            KlucSpoja koniec = new KlucSpoja(Integer.valueOf(sKoniec[0]), Integer.valueOf(sKoniec[1]));
            for (List<Spoj> turnus : turnusy) {
                if (turnus.get(turnus.size() - 1).getKluc().equals(zaciatok)) {
                    turnus.add(spoje.get(zaciatok));
                    break;
                }
                if (turnus.get(0).getKluc().equals(koniec)) {
                    turnus.add(0, spoje.get(koniec));
                    break;
                }
                turnusy.add(Arrays.asList(spoje.get(zaciatok)));
            }
        }
        return turnusy;
    }
}

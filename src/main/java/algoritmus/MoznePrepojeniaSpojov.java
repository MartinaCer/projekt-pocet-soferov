package algoritmus;

import dto.Spoj;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import konfiguracia.Konstanty;

/**
 *
 * @author Martina Cernekova
 */
public final class MoznePrepojeniaSpojov {

    private MoznePrepojeniaSpojov() {
    }

    public static void vypocitajNasledovne(List<Spoj> spoje, Map<Integer, Map<Integer, Integer>> vzdialenosti) {
        for (int i = 0; i < spoje.size(); i++) {
            List<Spoj> moznePrepojenia = new ArrayList<>();
            Spoj spoj1 = spoje.get(i);
            for (int j = 0; j < spoje.size(); j++) {
                Spoj spoj2 = spoje.get(j);
                int vzdialenost = vzdialenosti.get(spoj1.getMiestoPrichodu().getId()).get(spoj2.getMiestoOdchodu().getId());
                vzdialenost += Konstanty.REZERVA;
                int prichod = spoj1.getCasPrichodu().toSecondOfDay();
                int odchod = spoj2.getCasOdchodu().toSecondOfDay();
                if (prichod + vzdialenost < odchod && odchod - prichod < 28 * 60 * 60) {
                    moznePrepojenia.add(spoj2);
                    spoj2.getMoznePredosleSpojenia().add(spoj1);
                }
            }
            spoj1.getMozneNasledovneSpojenia().addAll(moznePrepojenia);
        }
    }
    
    public static void vypocitajZmenaSoferov(List<Spoj> spoje, Map<Integer, Map<Integer, Integer>> vzdialenosti, int idGaraze) {
        for (int i = 0; i < spoje.size(); i++) {
            List<Spoj> moznePrepojenia = new ArrayList<>();
            Spoj spoj1 = spoje.get(i);
            for (int j = 0; j < spoje.size(); j++) {
                Spoj spoj2 = spoje.get(j);
                int vzdialenost = vzdialenosti.get(spoj1.getMiestoPrichodu().getId()).get(idGaraze) + vzdialenosti.get(idGaraze).get(spoj2.getMiestoOdchodu().getId());
                vzdialenost += Konstanty.REZERVA;
                int prichod = spoj1.getCasPrichodu().toSecondOfDay();
                int odchod = spoj2.getCasOdchodu().toSecondOfDay();
                if (prichod + vzdialenost < odchod && odchod - prichod < 28 * 60 * 60) {
                    moznePrepojenia.add(spoj2);
                    spoj2.getMoznePredosleZmenySofera().add(spoj1);
                }
            }
            spoj1.getMozneNasledovneZmenySofera().addAll(moznePrepojenia);
        }
    }
}

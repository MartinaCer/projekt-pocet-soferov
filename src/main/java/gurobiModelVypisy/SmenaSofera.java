package gurobiModelVypisy;

import java.util.ArrayList;
import java.util.List;
import static konfiguracia.Konstanty.BEZ_PRESTAVKY;
import static konfiguracia.Konstanty.MIN_PRESTAVKA;
import static konfiguracia.Konstanty.MIN_SUCET_PRESTAVOK;

/**
 *
 * @author Martina Cernekova
 */
public class SmenaSofera {

    private final int cestaZgaraze;
    private final int cestaDoGaraze;
    private final List<SpojSofera> spoje;

    public SmenaSofera(int cestaZgaraze, int cestaDoGaraze) {
        this.cestaZgaraze = cestaZgaraze;
        this.cestaDoGaraze = cestaDoGaraze;
        this.spoje = new ArrayList<>();
    }

    public int getCestaZgaraze() {
        return cestaZgaraze;
    }

    public int getCestaDoGaraze() {
        return cestaDoGaraze;
    }

    public List<SpojSofera> getSpoje() {
        return spoje;
    }
    
    public int zaciatokSmeny() {
        return spoje.get(0).getSpoj().getCasOdchodu().toSecondOfDay() - cestaZgaraze;
    }
    
    public int koniecSmeny() {
        return spoje.get(spoje.size() - 1).getSpoj().getCasPrichodu().toSecondOfDay() + cestaDoGaraze;
    }

    public int trvanieSmeny() {
        return koniecSmeny() - zaciatokSmeny();
    }

    public boolean splnaPrestavku() {
        int prestavka = 0;
        int prejdenych = cestaZgaraze + trvanieSpoja(spoje.get(0));
        if (prejdenych > BEZ_PRESTAVKY) {
            return false;
        }
        if (spoje.get(0).getPrestavkaPoSpoji() > MIN_PRESTAVKA) {
            prestavka += spoje.get(0).getPrestavkaPoSpoji();
        }
        if (prestavka >= MIN_SUCET_PRESTAVOK) {
            prestavka = 0;
            prejdenych = 0;
        }
        if (spoje.size() > 2) {
            for (int i = 1; i < spoje.size() - 1; i++) {
                prejdenych += trvanieSpoja(spoje.get(i));
                if (prejdenych > BEZ_PRESTAVKY) {
                    return false;
                }
                if (spoje.get(i).getPrestavkaPoSpoji() > MIN_PRESTAVKA) {
                    prestavka += spoje.get(0).getPrestavkaPoSpoji();
                }
                if (prestavka >= MIN_SUCET_PRESTAVOK) {
                    prestavka = 0;
                    prejdenych = 0;
                }
            }
        }
        prejdenych += cestaZgaraze;
        return prejdenych <= BEZ_PRESTAVKY;
    }

    private int trvanieSpoja(SpojSofera spoj) {
        return spoj.getSpoj().getCasPrichodu().toSecondOfDay() - spoj.getSpoj().getCasOdchodu().toSecondOfDay();
    }
}

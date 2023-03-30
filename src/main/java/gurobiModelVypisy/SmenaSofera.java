package gurobiModelVypisy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import konfiguracia.Konstanty;
import konfiguracia.Konstanty.Prestavka;

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

    public int trvanieJazdy() {
        int trvanie = cestaZgaraze + cestaDoGaraze + (Konstanty.REZERVA * (spoje.size() - 1));
        for (SpojSofera spoj : spoje) {
            trvanie += spoj.getSpoj().getCasPrichodu().toSecondOfDay() - spoj.getSpoj().getCasOdchodu().toSecondOfDay();
            trvanie += spoj.getPrazdnyPrejazdPoSpoji();
        }
        return trvanie;
    }

    public List<SpojSofera> porusujePrestavku(Prestavka nastavenie) {
        List<SpojSofera> zoznam = new ArrayList<>();
        for (int i = 0; i < spoje.size() - 1; i++) {
            int prestavka = spoje.get(i).getPrestavkaPoSpoji() >= nastavenie.getMinPrestavka()
                    ? spoje.get(i).getPrestavkaPoSpoji()
                    : 0;
            int zaciatok = spoje.get(i).getSpoj().getCasOdchodu().toSecondOfDay();
            if (i == 0) {
                zaciatok -= cestaZgaraze;
            }
            zoznam.clear();
            zoznam.add(spoje.get(i));
            for (int j = i + 1; j < spoje.size(); j++) {
                SpojSofera spoj = spoje.get(j);
                zoznam.add(spoj);
                int prichod = j == spoje.size() - 1
                        ? spoj.getSpoj().getCasPrichodu().toSecondOfDay() + cestaDoGaraze
                        : spoj.getSpoj().getCasPrichodu().toSecondOfDay();
                if (prichod - zaciatok > nastavenie.getBezPrestavky()) {
                    if (prestavka < nastavenie.getMinSucetPrestavok()) {
                        return zoznam;
                    }
                    break;
                }
                prestavka += spoj.getPrestavkaPoSpoji();
            }
        }
        return Collections.emptyList();
    }

}

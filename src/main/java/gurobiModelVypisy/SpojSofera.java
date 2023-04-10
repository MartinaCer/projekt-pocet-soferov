package gurobiModelVypisy;

import dataObjekty.Spoj;

/**
 *
 * @author Martina Cernekova
 */
public class SpojSofera {

    private final Spoj spoj;
    private final int prazdnyPrejazdPoSpoji;
    private final int prestavkaPoSpoji;

    public SpojSofera(Spoj spoj, int prazdnyPrejazdPoSpoji, int prestavkaPoSpoji) {
        this.spoj = spoj;
        this.prazdnyPrejazdPoSpoji = prazdnyPrejazdPoSpoji;
        this.prestavkaPoSpoji = prestavkaPoSpoji;
    }

    public Spoj getSpoj() {
        return spoj;
    }

    public int getPrazdnyPrejazdPoSpoji() {
        return prazdnyPrejazdPoSpoji;
    }

    public int getPrestavkaPoSpoji() {
        return prestavkaPoSpoji;
    }

}

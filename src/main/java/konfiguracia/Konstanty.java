package konfiguracia;

import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Martina Cernekova
 */
public final class Konstanty {

    public static final int GARAZ = 470;
    public static final int REZERVA = 600;
    public static final int REZERVA_GARAZ = 1800;
    public static final int MAX_DOBA_JAZDY = 36000;
    public static final int MAX_DOBA_SMENY = 46800;
    public static final List<Prestavka> PRESTAVKY = Arrays.asList(
            new Prestavka(900, 1800, 16200), new Prestavka(1800, 1800, 23400));
    public static final int PRESTAVKA_V_DOBE_JAZDY = 600;
    public static final int CENA_SOFERA = 50;
    public static final int CENA_KILOMETER = 2;

    private Konstanty() {
    }

    public static class Prestavka {

        private final int minPrestavka;
        private final int minSucetPrestavok;
        private final int bezPrestavky;

        public Prestavka(int minPrestavka, int minSucetPrestavok, int bezPrestavky) {
            this.minPrestavka = minPrestavka;
            this.minSucetPrestavok = minSucetPrestavok;
            this.bezPrestavky = bezPrestavky;
        }

        public int getMinPrestavka() {
            return minPrestavka;
        }

        public int getMinSucetPrestavok() {
            return minSucetPrestavok;
        }

        public int getBezPrestavky() {
            return bezPrestavky;
        }

    }
}

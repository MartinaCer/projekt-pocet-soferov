package konfiguracia;

import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Martina Cernekova
 */
public final class Konstanty {

    public static final List<Integer> GARAZE = Arrays.asList(470, 471);
    public static final int REZERVA = 600;
    public static final int MAX_DOBA_JAZDY = 36000;
    public static final int BEZ_PRESTAVKY = 16200;
    public static final int MIN_PRESTAVKA = 900;
    public static final int MIN_SUCET_PRESTAVOK = 1800;
    public static final int CENA_SOFERA = 50;
    public static final int CENA_KILOMETER = 2;

    private Konstanty() {
    }

}

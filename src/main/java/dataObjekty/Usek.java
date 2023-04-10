package dataObjekty;

/**
 *
 * @author Martina Cernekova
 */
public class Usek {

    private final Zastavka zaciatok;
    private final Zastavka koniec;
    private final int sekundy;
    private final double kilometre;

    public Usek(Zastavka zaciatok, Zastavka koniec, int sekundy, double kilometre) {
        this.zaciatok = zaciatok;
        this.koniec = koniec;
        this.sekundy = sekundy;
        this.kilometre = kilometre;
    }

    public Zastavka getZaciatok() {
        return zaciatok;
    }

    public Zastavka getKoniec() {
        return koniec;
    }

    public int getSekundy() {
        return sekundy;
    }

    public double getKilometre() {
        return kilometre;
    }

}

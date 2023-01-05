package dto;

import algoritmus.MoznePrepojeniaSpojov;
import algoritmus.Vzdialenosti;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Martina Cernekova
 */
public class Data {

    private final Map<Integer, Zastavka> zastavky;
    private final List<Usek> useky;
    private final List<Spoj> spoje;
    private final List<Integer> garaze;
    private final Map<Integer, Map<Integer, Integer>> vzdialenosti;

    public Data(Map<Integer, Zastavka> zastavky, List<Usek> useky, List<Spoj> spoje, List<Integer> garaze) {
        this.zastavky = zastavky;
        this.useky = useky;
        this.spoje = spoje;
        this.garaze = garaze;
        this.vzdialenosti = Vzdialenosti.vypocitaj(new ArrayList<>(zastavky.values()), useky);
        MoznePrepojeniaSpojov.vypocitaj(spoje, vzdialenosti);
    }

    public Map<Integer, Zastavka> getZastavky() {
        return zastavky;
    }

    public List<Usek> getUseky() {
        return useky;
    }

    public List<Spoj> getSpoje() {
        return spoje;
    }

    public List<Integer> getGaraze() {
        return garaze;
    }

    public Map<Integer, Map<Integer, Integer>> getVzdialenosti() {
        return vzdialenosti;
    }

}

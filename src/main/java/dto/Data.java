package dto;

import algoritmus.MoznePrepojeniaSpojov;
import algoritmus.Vzdialenosti;
import dto.Spoj.KlucSpoja;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import konfiguracia.Konfiguracia;

/**
 *
 * @author Martina Cernekova
 */
public class Data {

    private final Map<Integer, Zastavka> zastavky;
    private final List<Usek> useky;
    private final Map<KlucSpoja, Spoj> spoje;
    private final Map<Integer, Map<Integer, Integer>> casVzdialenosti;
    private final Map<Integer, Map<Integer, Integer>> kmVzdialenosti;
    private final Konfiguracia konfiguracia;

    public Data(Map<Integer, Zastavka> zastavky, List<Usek> useky, Map<KlucSpoja, Spoj> spoje, Konfiguracia konfiguracia) {
        this.zastavky = zastavky;
        this.useky = useky;
        this.spoje = spoje;
        this.casVzdialenosti = Vzdialenosti.vypocitaj(new ArrayList<>(zastavky.values()), useky, true);
        this.kmVzdialenosti = Vzdialenosti.vypocitaj(new ArrayList<>(zastavky.values()), useky, false);
        this.konfiguracia = konfiguracia;
        MoznePrepojeniaSpojov.vypocitajNasledovne(new ArrayList<>(spoje.values()), casVzdialenosti, konfiguracia.getRezerva());
        MoznePrepojeniaSpojov.vypocitajZmenaSoferov(new ArrayList<>(spoje.values()), casVzdialenosti, konfiguracia.getGaraz());
    }

    public Map<Integer, Zastavka> getZastavky() {
        return zastavky;
    }

    public List<Usek> getUseky() {
        return useky;
    }

    public Map<KlucSpoja, Spoj> getSpoje() {
        return spoje;
    }

    public Konfiguracia getKonfiguracia() {
        return konfiguracia;
    }

    public Map<Integer, Map<Integer, Integer>> getCasVzdialenosti() {
        return casVzdialenosti;
    }

    public Map<Integer, Map<Integer, Integer>> getKmVzdialenosti() {
        return kmVzdialenosti;
    }

}

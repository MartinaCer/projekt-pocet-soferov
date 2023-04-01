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

    private final Konfiguracia konfiguracia;

    private Map<Integer, Zastavka> zastavky;
    private List<Usek> useky;
    private Map<KlucSpoja, Spoj> spoje;
    private Map<Integer, Map<Integer, Integer>> casVzdialenosti;
    private Map<Integer, Map<Integer, Integer>> kmVzdialenosti;
    private boolean nastaveneData = false;

    public Data(Konfiguracia konfiguracia) {
        this.konfiguracia = konfiguracia;
    }

    public void vytvorData(Map<Integer, Zastavka> zastavky, List<Usek> useky, Map<KlucSpoja, Spoj> spoje) {
        this.zastavky = zastavky;
        this.useky = useky;
        this.spoje = spoje;
        this.casVzdialenosti = Vzdialenosti.vypocitaj(new ArrayList<>(zastavky.values()), useky, true);
        this.kmVzdialenosti = Vzdialenosti.vypocitaj(new ArrayList<>(zastavky.values()), useky, false);
        MoznePrepojeniaSpojov.vypocitajNasledovne(new ArrayList<>(spoje.values()), casVzdialenosti, konfiguracia.getRezerva());
        MoznePrepojeniaSpojov.vypocitajZmenaSoferov(new ArrayList<>(spoje.values()), casVzdialenosti, konfiguracia.getGaraz());
        nastaveneData = true;
    }

    public void zmenKonfiguraciu() {
        if (nastaveneData) {
            MoznePrepojeniaSpojov.vypocitajNasledovne(new ArrayList<>(spoje.values()), casVzdialenosti, konfiguracia.getRezerva());
            MoznePrepojeniaSpojov.vypocitajZmenaSoferov(new ArrayList<>(spoje.values()), casVzdialenosti, konfiguracia.getGaraz());
        }
    }

    public Konfiguracia getKonfiguracia() {
        return konfiguracia;
    }

    public boolean isNastaveneData() {
        return nastaveneData;
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

    public Map<Integer, Map<Integer, Integer>> getCasVzdialenosti() {
        return casVzdialenosti;
    }

    public Map<Integer, Map<Integer, Integer>> getKmVzdialenosti() {
        return kmVzdialenosti;
    }

}

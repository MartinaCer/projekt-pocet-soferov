package dto;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Martina Cernekova
 */
public class Spoj {

    private final int id;
    private final int linka;
    private final Zastavka miestoOdchodu;
    private final Zastavka miestoPrichodu;
    private final LocalTime casOdchodu;
    private final LocalTime casPrichodu;
    private final int kilometre;

    private final List<Spoj> moznePredosleSpojenia;
    private final List<Spoj> mozneNasledovneSpojenia;

    private Spoj predoslySpoj;
    private Spoj nasledovnySpoj;

    public Spoj(int id, int linka, Zastavka miestoOdchodu, Zastavka miestoPrichodu,
            LocalTime casOdchodu, LocalTime casPrichodu, int kilometre) {
        this.id = id;
        this.linka = linka;
        this.miestoOdchodu = miestoOdchodu;
        this.miestoPrichodu = miestoPrichodu;
        this.casOdchodu = casOdchodu;
        this.casPrichodu = casPrichodu;
        this.kilometre = kilometre;
        this.moznePredosleSpojenia = new ArrayList<>();
        this.mozneNasledovneSpojenia = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public int getLinka() {
        return linka;
    }

    public Zastavka getMiestoOdchodu() {
        return miestoOdchodu;
    }

    public Zastavka getMiestoPrichodu() {
        return miestoPrichodu;
    }

    public LocalTime getCasOdchodu() {
        return casOdchodu;
    }

    public LocalTime getCasPrichodu() {
        return casPrichodu;
    }

    public int getKilometre() {
        return kilometre;
    }

    public List<Spoj> getMoznePredosleSpojenia() {
        return moznePredosleSpojenia;
    }

    public List<Spoj> getMozneNasledovneSpojenia() {
        return mozneNasledovneSpojenia;
    }

    public Spoj getPredoslySpoj() {
        return predoslySpoj;
    }

    public Spoj getNasledovnySpoj() {
        return nasledovnySpoj;
    }

    public void setPredoslySpoj(Spoj predoslySpoj) {
        this.predoslySpoj = predoslySpoj;
    }

    public void setNasledovnySpoj(Spoj nasledovnySpoj) {
        this.nasledovnySpoj = nasledovnySpoj;
    }

}

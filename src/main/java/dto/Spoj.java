package dto;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Martina Cernekova
 */
public class Spoj {

    private final KlucSpoja kluc;
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
        this.kluc = new KlucSpoja(id, linka);
        this.miestoOdchodu = miestoOdchodu;
        this.miestoPrichodu = miestoPrichodu;
        this.casOdchodu = casOdchodu;
        this.casPrichodu = casPrichodu;
        this.kilometre = kilometre;
        this.moznePredosleSpojenia = new ArrayList<>();
        this.mozneNasledovneSpojenia = new ArrayList<>();
    }

    public KlucSpoja getKluc() {
        return kluc;
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

    public static class KlucSpoja {

        private final int id;
        private final int linka;

        public KlucSpoja(int id, int linka) {
            this.id = id;
            this.linka = linka;
        }

        public int getId() {
            return id;
        }

        public int getLinka() {
            return linka;
        }

        @Override
        public String toString() {
            return id + ";" + linka;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final KlucSpoja other = (KlucSpoja) obj;
            if (this.id != other.id) {
                return false;
            }
            return this.linka == other.linka;
        }

    }

}

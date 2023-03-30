package gurobiModelVypisy;

import dto.Spoj;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Martina Cernekova
 */
public class SpojeLinky {

    private final int linka;
    private final Map<Integer, List<SpojLinky>> spoje;

    public SpojeLinky(int linka) {
        this.linka = linka;
        this.spoje = new HashMap<>();
    }

    public int getLinka() {
        return linka;
    }

    public Map<Integer, List<SpojLinky>> getSpoje() {
        return spoje;
    }

    public void pridajSpoj(Spoj spoj, boolean obsluzeny) {
        spoje.computeIfAbsent(spoj.getKluc().getId() % 2, k -> new ArrayList<>()).add(new SpojLinky(spoj, obsluzeny));
    }

    public static class SpojLinky {

        private final Spoj spoj;
        private final boolean obsluzeny;

        public SpojLinky(Spoj spoj, boolean obsluzeny) {
            this.spoj = spoj;
            this.obsluzeny = obsluzeny;
        }

        public Spoj getSpoj() {
            return spoj;
        }

        public boolean isObsluzeny() {
            return obsluzeny;
        }

    }
}

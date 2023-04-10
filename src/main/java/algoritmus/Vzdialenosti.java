package algoritmus;

import dataObjekty.Usek;
import dataObjekty.Zastavka;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.PriorityQueue;

/**
 *
 * @author Martina Cernekova
 */
public final class Vzdialenosti {

    private Vzdialenosti() {
    }

    public static Map<Integer, Map<Integer, Integer>> vypocitaj(List<Zastavka> zastavky, List<Usek> useky, boolean cas) {
        Map<Integer, Map<Integer, Integer>> vzdialenosti = new HashMap<>();
        zastavky.forEach(n -> vzdialenosti.put(n.getId(), vypocitajVzdialenostiPreZastavku(n.getId(), zastavky, useky, cas)));
        return vzdialenosti;
    }

    private static Map<Integer, Integer> vypocitajVzdialenostiPreZastavku(int id, List<Zastavka> zastavky, List<Usek> useky, boolean cas) {
        PriorityQueue<PomZastavka> neprejdene = new PriorityQueue<>((o1, o2) -> Double.compare(o1.vzd, o2.vzd));
        Map<Integer, Integer> prejdene = new HashMap<>();
        zastavky.forEach(n -> {
            if (n.getId() == id) {
                neprejdene.add(new PomZastavka(n.getId(), 0));
            } else {
                neprejdene.add(new PomZastavka(n.getId(), Integer.MAX_VALUE));
            }
        });

        while (!neprejdene.isEmpty()) {
            PomZastavka pomZastavka = neprejdene.poll();
            prejdene.put(pomZastavka.id, (int) pomZastavka.vzd);
            useky.stream()
                    .filter(e -> e.getZaciatok().getId() == pomZastavka.id)
                    .forEach(e -> {
                        Optional<PomZastavka> pomZ = neprejdene.stream().filter(ee -> {
                            return ee.id == e.getKoniec().getId();
                        }).findFirst();
                        if (pomZ.isPresent()) {
                            double vzdialenost = (cas ? e.getSekundy() : e.getKilometre()) + pomZastavka.vzd;

                            if (pomZ.get().vzd > vzdialenost) {
                                neprejdene.remove(pomZ.get());
                                pomZ.get().vzd = vzdialenost;
                                neprejdene.add(pomZ.get());
                            }
                        }
                    });
        }

        return prejdene;
    }

    private static class PomZastavka {

        private int id;
        private double vzd;

        public PomZastavka(int id, double vzd) {
            this.id = id;
            this.vzd = vzd;
        }

    }
}

package algoritmus;

import dto.Usek;
import dto.Zastavka;
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

    public static Map<Integer, Map<Integer, Integer>> vypocitaj(List<Zastavka> zastavky, List<Usek> useky) {
        Map<Integer, Map<Integer, Integer>> vzdialenosti = new HashMap<>();
        zastavky.forEach(n -> vzdialenosti.put(n.getId(), vypocitajVzdialenostiPreZastavku(n.getId(), zastavky, useky)));
        return vzdialenosti;
    }

    private static Map<Integer, Integer> vypocitajVzdialenostiPreZastavku(int id, List<Zastavka> zastavky, List<Usek> useky) {
        PriorityQueue<PomZastavka> neprejdene = new PriorityQueue<>((o1, o2) -> Integer.compare(o1.sekundy, o2.sekundy));
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
            prejdene.put(pomZastavka.id, pomZastavka.sekundy);
            useky.stream()
                    .filter(e -> e.getZaciatok().getId() == pomZastavka.id)
                    .forEach(e -> {
                        Optional<PomZastavka> pomZ = neprejdene.stream().filter(ee -> {
                            return ee.id == e.getKoniec().getId();
                        }).findFirst();
                        if (pomZ.isPresent()) {
                            int vzdialenost = e.getSekundy() + pomZastavka.sekundy;

                            if (pomZ.get().sekundy > vzdialenost) {
                                neprejdene.remove(pomZ.get());
                                pomZ.get().sekundy = vzdialenost;
                                neprejdene.add(pomZ.get());
                            }
                        }
                    });
        }

        return prejdene;
    }

    private static class PomZastavka {

        private int id;
        private int sekundy;

        public PomZastavka(int id, int sekundy) {
            this.id = id;
            this.sekundy = sekundy;
        }

    }
}

package algoritmus;

import dataObjekty.Spoj;
import dataObjekty.Spoj.KlucSpoja;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Martina Cernekova
 */
public final class Priority {

    public final static int P1 = 100;
    public final static int P2 = 50;
    public final static int P3 = 1;

    private Priority() {
    }

    public static void nastavPriority(Map<KlucSpoja, Spoj> spoje, Strategia strategia, Map<KlucSpoja, Integer> rucnePriority, int predvolenaPriorita) {
        Map<Integer, Map<Integer, List<Spoj>>> linky = new HashMap<>();
        spoje.values().forEach(spoj
                -> linky.computeIfAbsent(spoj.getKluc().getLinka(), k -> new HashMap<>())
                        .computeIfAbsent(spoj.getKluc().getId() % 2, k -> new ArrayList<>()).add(spoj));
        for (Map<Integer, List<Spoj>> linka : linky.values()) {
            for (List<Spoj> linkaSmer : linka.values()) {
                Collections.sort(linkaSmer, Comparator.comparing(l -> l.getCasOdchodu()));
                switch (strategia) {
                    case ROVNAKE:
                        linkaSmer.forEach(spoj -> spoj.setPriorita(P3));
                        break;
                    case PRVY_POSLEDNY:
                        linkaSmer.get(0).setPriorita(P2);
                        linkaSmer.get(linkaSmer.size() - 1).setPriorita(P2);
                        int min = 1;
                        int max = linkaSmer.size() - 2;
                        for (int i = 1; i < linkaSmer.size() - 1; i++) {
                            int priorita = P3;
                            if (i % 2 == 0) {
                                priorita += max;
                                max--;
                            } else {
                                priorita += min;
                                min++;
                            }
                            linkaSmer.get(i).setPriorita(priorita);
                        }
                        break;
                    case KAZDY_DRUHY:
                        nastavPriorityKazdyDruhy(linkaSmer, 1, linkaSmer.size() / 2, 1, (int) Math.round((double) linkaSmer.size() / 2.0), false);
                        break;
                    case PRVY_POSLEDNY_KAZDY_DRUHY:
                        linkaSmer.get(0).setPriorita(P1);
                        linkaSmer.get(linkaSmer.size() - 1).setPriorita(P1);
                        nastavPriorityKazdyDruhy(linkaSmer, 1, (linkaSmer.size() - 2) / 2, 1, (int) Math.round((double) (linkaSmer.size() - 2) / 2.0), true);
                        break;
                    case RUCNE:
                        linkaSmer.forEach(spoj -> spoj.setPriorita(rucnePriority.getOrDefault(spoj.getKluc(), predvolenaPriorita)));
                        break;

                    default:
                        break;
                }
            }
        }
    }

    private static void nastavPriorityKazdyDruhy(List<Spoj> linkaSmer, int minP3, int maxP3, int minP2, int maxP2, boolean kombinacia) {
        int zaciatok = kombinacia ? 1 : 0;
        int koniec = kombinacia ? linkaSmer.size() -1 : linkaSmer.size();
        for (int i = zaciatok; i < koniec; i++) {
            switch (i % 4) {
                case 0:
                    linkaSmer.get(i).setPriorita(P2 + minP2);
                    minP2++;
                    break;
                case 1:
                    linkaSmer.get(i).setPriorita(P3 + minP3);
                    minP3++;
                    break;
                case 2:
                    linkaSmer.get(i).setPriorita(P2 + maxP2);
                    maxP2--;
                    break;
                case 3:
                    linkaSmer.get(i).setPriorita(P3 + maxP3);
                    maxP3--;
                    break;
                default:
                    break;
            }
        }
    }

    public static enum Strategia {
        ROVNAKE,
        PRVY_POSLEDNY,
        KAZDY_DRUHY,
        PRVY_POSLEDNY_KAZDY_DRUHY,
        RUCNE;
    }
}

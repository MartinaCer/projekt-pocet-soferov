package gurobiModel;

import dto.Spoj;
import dto.Spoj.KlucSpoja;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author Martina Cernekova
 */
public final class VypisyPreModel {

    private VypisyPreModel() {
    }

    public static void vytvorVypisTurnusy(List<String> nasledujuceSpoje, Map<KlucSpoja, Spoj> spoje) {
        List<List<Spoj>> turnusy = new ArrayList<>();
        for (String spoj : nasledujuceSpoje) {
            String[] data = spoj.split("_");
            String[] sISpoj = data[1].split(";");
            String[] sJSpoj = data[2].split(";");
            KlucSpoja iSpoj = new KlucSpoja(Integer.valueOf(sISpoj[0]), Integer.valueOf(sISpoj[1]));
            KlucSpoja jSpoj = new KlucSpoja(Integer.valueOf(sJSpoj[0]), Integer.valueOf(sJSpoj[1]));
            boolean novyTurnus = true;
            for (List<Spoj> turnus : turnusy) {
                if (turnus.get(turnus.size() - 1).getKluc().equals(iSpoj)) {
                    turnus.add(spoje.get(jSpoj));
                    novyTurnus = false;
                    break;
                }
                if (turnus.get(0).getKluc().equals(jSpoj)) {
                    turnus.add(0, spoje.get(iSpoj));
                    novyTurnus = false;
                    break;
                }
            }
            if (novyTurnus) {
                turnusy.add(new ArrayList<>(Arrays.asList(spoje.get(iSpoj), spoje.get(jSpoj))));
            } else {
                int index = -1;
                for (int i = 0; i < turnusy.size(); i++) {
                    List<Spoj> turnus = turnusy.get(i);
                    Spoj prvy = turnus.get(0);
                    Spoj posledny = turnus.get(turnus.size() - 1);
                    for (int j = 0; j < turnusy.size(); j++) {
                        List<Spoj> turnus2 = turnusy.get(j);
                        Spoj prvy2 = turnus2.get(0);
                        Spoj posledny2 = turnus2.get(turnus2.size() - 1);
                        if (prvy.getKluc().equals(posledny2.getKluc())) {
                            turnus2.remove(posledny2);
                            turnus.addAll(0, turnus2);
                            index = j;
                            break;
                        }
                        if (posledny.getKluc().equals(prvy2.getKluc())) {
                            turnus2.remove(prvy2);
                            turnus.addAll(turnus2);
                            index = j;
                            break;
                        }
                    }
                }
                if (index != -1) {
                    turnusy.remove(index);
                }
            }
        }
        List<String[]> data = new ArrayList<>();
        System.out.println("Turnusy:");
        for (int i = 0; i < turnusy.size(); i++) {
            String turnus = i + 1 + ": ";
            for (Spoj spoj : turnusy.get(i)) {
                turnus += spoj.getKluc().toString() + "(" + spoj.getCasOdchodu() + ";" + spoj.getCasPrichodu() + ") -> ";
            }
            System.out.println(turnus);
            data.add(new String[]{turnusy.get(i).get(0).getKluc().toString(), turnusy.get(i).get(turnusy.get(i).size() - 1).getKluc().toString()});
        }
        File fTur = new File("turnusy.csv");
        try {
            PrintWriter pw = new PrintWriter(fTur);
            for (String[] strings : data) {
                pw.println(Stream.of(strings).collect(Collectors.joining(";")));
            }
            pw.close();
        } catch (FileNotFoundException ex) {
        }
    }

    public static void vytvorVypisTurnusyGaraze(List<String> nasledujuceSpoje, Map<KlucSpoja, Spoj> spoje) {
        List<List<SpojGaraz>> turnusy = new ArrayList<>();
        for (String spoj : nasledujuceSpoje) {
            String[] data = spoj.split("_");
            String[] sISpoj = data[1].split(";");
            String[] sJSpoj = data[2].split(";");
            String idGaraze = data[3];
            KlucSpoja iSpoj = new KlucSpoja(Integer.valueOf(sISpoj[0]), Integer.valueOf(sISpoj[1]));
            KlucSpoja jSpoj = new KlucSpoja(Integer.valueOf(sJSpoj[0]), Integer.valueOf(sJSpoj[1]));
            boolean novyTurnus = true;
            for (List<SpojGaraz> turnus : turnusy) {
                if (turnus.get(turnus.size() - 1).spoj.getKluc().equals(iSpoj)) {
                    turnus.add(new SpojGaraz(spoje.get(jSpoj), idGaraze));
                    novyTurnus = false;
                    break;
                }
                if (turnus.get(0).spoj.getKluc().equals(jSpoj)) {
                    turnus.add(0, new SpojGaraz(spoje.get(iSpoj), idGaraze));
                    novyTurnus = false;
                    break;
                }
            }
            if (novyTurnus) {
                turnusy.add(new ArrayList<>(Arrays.asList(new SpojGaraz(spoje.get(iSpoj), idGaraze), new SpojGaraz(spoje.get(jSpoj), idGaraze))));
            } else {
                int index = -1;
                for (int i = 0; i < turnusy.size(); i++) {
                    List<SpojGaraz> turnus = turnusy.get(i);
                    SpojGaraz prvy = turnus.get(0);
                    SpojGaraz posledny = turnus.get(turnus.size() - 1);
                    for (int j = 0; j < turnusy.size(); j++) {
                        List<SpojGaraz> turnus2 = turnusy.get(j);
                        SpojGaraz prvy2 = turnus2.get(0);
                        SpojGaraz posledny2 = turnus2.get(turnus2.size() - 1);
                        if (prvy.spoj.getKluc().equals(posledny2.spoj.getKluc())) {
                            turnus2.remove(posledny2);
                            turnus.addAll(0, turnus2);
                            index = j;
                            break;
                        }
                        if (posledny.spoj.getKluc().equals(prvy2.spoj.getKluc())) {
                            turnus2.remove(prvy2);
                            turnus.addAll(turnus2);
                            index = j;
                            break;
                        }
                    }
                }
                if (index != -1) {
                    turnusy.remove(index);
                }
            }
        }
        List<String[]> data = new ArrayList<>();
        System.out.println("Turnusy:");
        for (int i = 0; i < turnusy.size(); i++) {
            String turnus = i + 1 + ": ";
            for (SpojGaraz spoj : turnusy.get(i)) {
                turnus += spoj.spoj.getKluc().toString() + ";" + spoj.garaz + "(" + spoj.spoj.getCasOdchodu() + ";" + spoj.spoj.getCasPrichodu() + ") -> ";
            }
            System.out.println(turnus);
            data.add(new String[]{turnusy.get(i).get(0).spoj.getKluc().toString(), turnusy.get(i).get(0).garaz,
                turnusy.get(i).get(turnusy.get(i).size() - 1).spoj.getKluc().toString(), turnusy.get(i).get(turnusy.get(i).size() - 1).garaz});
        }
        File fTur = new File("turnusy.csv");
        try {
            PrintWriter pw = new PrintWriter(fTur);
            for (String[] strings : data) {
                pw.println(Stream.of(strings).collect(Collectors.joining(";")));
            }
            pw.close();
        } catch (FileNotFoundException ex) {
        }
    }

    public static void vypisSpoje(List<String> sSpoje, Map<KlucSpoja, Spoj> spoje, boolean prve) {
        System.out.println(prve ? "Prvé spoje:" : "Posledné spoje:");
        File fTur = new File("spoje" + (prve ? "1" : "2") + ".csv");
        try {
            PrintWriter pw = new PrintWriter(fTur);
            for (String sSpoj : sSpoje) {
                String[] ssSpoj = sSpoj.split("_")[1].split(";");
                Spoj spoj = spoje.get(new KlucSpoja(Integer.valueOf(ssSpoj[0]), Integer.valueOf(ssSpoj[1])));
                System.out.println(spoj.getKluc().toString() + "(" + spoj.getCasOdchodu() + ";" + spoj.getCasPrichodu() + ")");
                pw.println(spoj.getKluc().toString());
            }
            pw.close();
        } catch (FileNotFoundException ex) {
        }
    }

    public static void vypisSpojeGaraze(List<String> sSpoje, Map<KlucSpoja, Spoj> spoje, boolean prve) {
        System.out.println(prve ? "Prvé spoje:" : "Posledné spoje:");
        File fTur = new File("spoje" + (prve ? "1" : "2") + ".csv");
        try {
            PrintWriter pw = new PrintWriter(fTur);
            for (String sSpoj : sSpoje) {
                String[] ssSpoj = sSpoj.split("_")[1].split(";");
                Spoj spoj = spoje.get(new KlucSpoja(Integer.valueOf(ssSpoj[0]), Integer.valueOf(ssSpoj[1])));
                System.out.println(spoj.getKluc().toString() + ";" + sSpoj.split("_")[2] + "(" + spoj.getCasOdchodu() + ";" + spoj.getCasPrichodu() + ")");
                pw.println(spoj.getKluc().toString() + ";" + sSpoj.split("_")[2]);
            }
            pw.close();
        } catch (FileNotFoundException ex) {
        }
    }

    private static class SpojGaraz {

        private final Spoj spoj;
        private final String garaz;

        public SpojGaraz(Spoj spoj, String garaz) {
            this.spoj = spoj;
            this.garaz = garaz;
        }

    }
}

package gurobiModelFunkcie;

import dto.Spoj;
import gurobi.GRB;
import gurobi.GRBException;
import gurobi.GRBLinExpr;
import gurobi.GRBModel;
import gurobi.GRBVar;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Martina Cernekova
 */
public class VseobecneFunkcie {

    private VseobecneFunkcie() {
    }

    public static char[] vytvorPoleRovny(int pocet) {
        char[] pole = new char[pocet];
        for (int i = 0; i < pocet; i++) {
            pole[i] = GRB.EQUAL;
        }
        return pole;
    }

    public static double[] vytvorPoleJednotiek(int pocet) {
        return vytvorPoleHodnot(pocet, 1);
    }

    public static double[] vytvorPoleHodnot(int pocet, double hodnota) {
        double[] pole = new double[pocet];
        for (int i = 0; i < pocet; i++) {
            pole[i] = hodnota;
        }
        return pole;
    }

    public static char[] vytvorPoleMensiRovny(int pocet) {
        char[] pole = new char[pocet];
        for (int i = 0; i < pocet; i++) {
            pole[i] = GRB.LESS_EQUAL;
        }
        return pole;
    }

    public static GRBLinExpr[] vytvorPolePodmienok(List<GRBLinExpr> podmienky) {
        GRBLinExpr[] pole = new GRBLinExpr[podmienky.size()];
        for (int i = 0; i < podmienky.size(); i++) {
            pole[i] = podmienky.get(i);
        }
        return pole;
    }

    public static GRBVar[] vytvorPolePremennych(List<GRBVar> premenne) {
        GRBVar[] pole = new GRBVar[premenne.size()];
        for (int i = 0; i < premenne.size(); i++) {
            pole[i] = premenne.get(i);
        }
        return pole;
    }

    public static String[] vytvorNazvyPodmienok(int pocet, String cisloPodmienky) {
        String[] pole = new String[pocet];
        for (int i = 0; i < pocet; i++) {
            pole[i] = cisloPodmienky + "_" + String.valueOf(i + 1);
        }
        return pole;
    }

    public static double[] vytvorPoleVzdialenosti(GRBVar[] premenneXij, Map<Spoj.KlucSpoja, Spoj> spoje,
            Map<Integer, Map<Integer, Integer>> vzdialenosti, int cena) throws GRBException {
        double[] pole = new double[premenneXij.length];
        for (int i = 0; i < premenneXij.length; i++) {
            String premenna = premenneXij[i].get(GRB.StringAttr.VarName);
            String[] data = premenna.split("_");
            String[] sISpoj = data[1].split(";");
            String[] sJSpoj = data[2].split(";");
            Spoj iSpoj = spoje.get(new Spoj.KlucSpoja(Integer.valueOf(sISpoj[0]), Integer.valueOf(sISpoj[1])));
            Spoj jSpoj = spoje.get(new Spoj.KlucSpoja(Integer.valueOf(sJSpoj[0]), Integer.valueOf(sJSpoj[1])));
            pole[i] = vzdialenosti.get(iSpoj.getMiestoPrichodu().getId()).get(jSpoj.getMiestoOdchodu().getId()) * cena;
        }
        return pole;
    }

    public static Map<Spoj.KlucSpoja, Map<Spoj.KlucSpoja, GRBVar>> vytvorPremenneXij(GRBModel model, List<Spoj> spoje) throws GRBException {
        Map<Spoj.KlucSpoja, Map<Spoj.KlucSpoja, GRBVar>> premenne = new HashMap<>();
        for (Spoj iSpoj : spoje) {
            Spoj.KlucSpoja iKluc = iSpoj.getKluc();
            Map<Spoj.KlucSpoja, GRBVar> iMapa = premenne.computeIfAbsent(iKluc, k -> new HashMap<>());
            for (Spoj jSpoj : iSpoj.getMozneNasledovneSpojenia()) {
                Spoj.KlucSpoja jKluc = jSpoj.getKluc();
                iMapa.put(jKluc, model.addVar(0, 1, 0, GRB.BINARY, "x_" + iKluc.toString() + "_" + jKluc.toString()));
            }
        }
        return premenne;
    }

    public static GRBVar[] vytvorSucetXij(Map<Spoj.KlucSpoja, Map<Spoj.KlucSpoja, GRBVar>> premenne) {
        List<GRBVar> retPremenne = new ArrayList<>();
        premenne.entrySet().forEach(e -> e.getValue().entrySet().forEach(e1 -> retPremenne.add(e1.getValue())));
        return vytvorPolePremennych(retPremenne);
    }

    public static GRBLinExpr[] vytvorPodmienkySucetXijPodlaI(Map<Spoj.KlucSpoja, Map<Spoj.KlucSpoja, GRBVar>> premenne, List<Spoj> spoje) {
        List<GRBLinExpr> podmienky = new ArrayList<>();
        for (Spoj iSpoj : spoje) {
            if (!iSpoj.getMozneNasledovneSpojenia().isEmpty()) {
                GRBLinExpr podmienka = new GRBLinExpr();
                iSpoj.getMozneNasledovneSpojenia().forEach(jSpoj -> podmienka.addTerm(1, premenne.get(iSpoj.getKluc()).get(jSpoj.getKluc())));
                podmienky.add(podmienka);
            }
        }
        return vytvorPolePodmienok(podmienky);
    }

    public static GRBLinExpr[] vytvorPodmienkySucetXijPodlaJ(Map<Spoj.KlucSpoja, Map<Spoj.KlucSpoja, GRBVar>> premenne, List<Spoj> spoje) {
        List<GRBLinExpr> podmienky = new ArrayList<>();
        for (Spoj jSpoj : spoje) {
            if (!jSpoj.getMoznePredosleSpojenia().isEmpty()) {
                GRBLinExpr podmienka = new GRBLinExpr();
                jSpoj.getMoznePredosleSpojenia().forEach(iSpoj -> podmienka.addTerm(1, premenne.get(iSpoj.getKluc()).get(jSpoj.getKluc())));
                podmienky.add(podmienka);
            }
        }
        return vytvorPolePodmienok(podmienky);
    }

}

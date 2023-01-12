package gurobiModel;

import dto.Spoj;
import dto.Spoj.KlucSpoja;
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
public final class FunkciePreModel {

    private FunkciePreModel() {
    }

    public static double[] vytvorPoleJednotiek(int pocet) {
        double[] pole = new double[pocet];
        for (int i = 0; i < pocet; i++) {
            pole[i] = 1;
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

    public static String[] vytvorNazvyPodmienok(int pocet, String cisloPodmienky) {
        String[] pole = new String[pocet];
        for (int i = 0; i < pocet; i++) {
            pole[i] = cisloPodmienky + "_" + String.valueOf(i + 1);
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

    public static GRBLinExpr[] vytvorPolePodmienok(List<GRBLinExpr> podmienky) {
        GRBLinExpr[] pole = new GRBLinExpr[podmienky.size()];
        for (int i = 0; i < podmienky.size(); i++) {
            pole[i] = podmienky.get(i);
        }
        return pole;
    }

    public static Map<KlucSpoja, Map<KlucSpoja, GRBVar>> vytvorPremenneXij(GRBModel model, List<Spoj> spoje) throws GRBException {
        Map<KlucSpoja, Map<KlucSpoja, GRBVar>> premenne = new HashMap<>();
        for (Spoj iSpoj : spoje) {
            KlucSpoja iKluc = iSpoj.getKluc();
            for (Spoj jSpoj : iSpoj.getMozneNasledovneSpojenia()) {
                KlucSpoja jKluc = jSpoj.getKluc();
                premenne.computeIfAbsent(iKluc, k -> new HashMap<>()).put(jKluc, model.addVar(0, 1, 0, GRB.BINARY, "x_" + iKluc.toString() + "_" + jKluc.toString()));
            }
        }
        return premenne;
    }

    public static GRBVar[] vytvorSucetXij(Map<KlucSpoja, Map<KlucSpoja, GRBVar>> premenne) {
        List<GRBVar> retPremenne = new ArrayList<>();
        premenne.entrySet().forEach(e -> e.getValue().entrySet().forEach(e1 -> retPremenne.add(e1.getValue())));
        return vytvorPolePremennych(retPremenne);
    }

    public static GRBLinExpr[] vytvorPodmienkySucetXijPodlaI(Map<KlucSpoja, Map<KlucSpoja, GRBVar>> premenne, List<Spoj> spoje) {
        List<GRBLinExpr> podmienky = new ArrayList<>();
        for (Spoj iSpoj : spoje) {
            if (!iSpoj.getMozneNasledovneSpojenia().isEmpty()) {
                GRBLinExpr podmienka = new GRBLinExpr();
                iSpoj.getMozneNasledovneSpojenia().forEach(jSpoj
                        -> podmienka.addTerm(1, premenne.get(iSpoj.getKluc()).get(jSpoj.getKluc())));
                podmienky.add(podmienka);
            }
        }
        return vytvorPolePodmienok(podmienky);
    }

    public static GRBLinExpr[] vytvorPodmienkySucetXijPodlaJ(Map<KlucSpoja, Map<KlucSpoja, GRBVar>> premenne, List<Spoj> spoje) {
        List<GRBLinExpr> podmienky = new ArrayList<>();
        for (Spoj iSpoj : spoje) {
            if (!iSpoj.getMoznePredosleSpojenia().isEmpty()) {
                GRBLinExpr podmienka = new GRBLinExpr();
                iSpoj.getMoznePredosleSpojenia().forEach(jSpoj
                        -> podmienka.addTerm(1, premenne.get(jSpoj.getKluc()).get(iSpoj.getKluc())));
                podmienky.add(podmienka);
            }
        }
        return vytvorPolePodmienok(podmienky);
    }

}

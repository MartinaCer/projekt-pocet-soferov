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

    public static char[] vytvorPoleRovny(int pocet) {
        char[] pole = new char[pocet];
        for (int i = 0; i < pocet; i++) {
            pole[i] = GRB.EQUAL;
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

    public static double[] vytvorPoleVzdialenosti(GRBVar[] premenneXij, Map<KlucSpoja, Spoj> spoje, Map<Integer, Map<Integer, Integer>> vzdialenosti) throws GRBException {
        double[] pole = new double[premenneXij.length];
        for (int i = 0; i < premenneXij.length; i++) {
            String premenna = premenneXij[i].get(GRB.StringAttr.VarName);
            String[] data = premenna.split("_");
            String[] sISpoj = data[1].split(";");
            String[] sJSpoj = data[2].split(";");
            Spoj iSpoj = spoje.get(new KlucSpoja(Integer.valueOf(sISpoj[0]), Integer.valueOf(sISpoj[1])));
            Spoj jSpoj = spoje.get(new KlucSpoja(Integer.valueOf(sJSpoj[0]), Integer.valueOf(sJSpoj[1])));
            pole[i] = vzdialenosti.get(iSpoj.getMiestoPrichodu().getId()).get(jSpoj.getMiestoOdchodu().getId());
        }
        return pole;
    }

    public static double[] vytvorPoleVzdialenostiGaraz(GRBVar[] premenneUjVi, Map<KlucSpoja, Spoj> spoje,
            Map<Integer, Map<Integer, Integer>> vzdialenosti, int idGaraze, boolean doGaraze) throws GRBException {
        double[] pole = new double[premenneUjVi.length];
        for (int i = 0; i < premenneUjVi.length; i++) {
            String premenna = premenneUjVi[i].get(GRB.StringAttr.VarName);
            String[] data = premenna.split("_");
            String[] sSpoj = data[1].split(";");
            Spoj spoj = spoje.get(new KlucSpoja(Integer.valueOf(sSpoj[0]), Integer.valueOf(sSpoj[1])));
            pole[i] = doGaraze
                    ? vzdialenosti.get(spoj.getMiestoPrichodu().getId()).get(idGaraze)
                    : vzdialenosti.get(idGaraze).get(spoj.getMiestoOdchodu().getId());
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

    public static Map<KlucSpoja, GRBVar> vytvorPremenneUjVi(GRBModel model, List<Spoj> spoje, String meno) throws GRBException {
        Map<KlucSpoja, GRBVar> premenne = new HashMap<>();
        for (Spoj spoj : spoje) {
            premenne.put(spoj.getKluc(), model.addVar(0, 1, 0, GRB.BINARY, meno + "_" + spoj.getKluc().toString()));
        }
        return premenne;
    }

    public static GRBVar[] vytvorSucetXij(Map<KlucSpoja, Map<KlucSpoja, GRBVar>> premenne) {
        List<GRBVar> retPremenne = new ArrayList<>();
        premenne.entrySet().forEach(e -> e.getValue().entrySet().forEach(e1 -> retPremenne.add(e1.getValue())));
        return vytvorPolePremennych(retPremenne);
    }

    public static GRBVar[] vytvorSucetUjVj(Map<KlucSpoja, GRBVar> premenne) {
        List<GRBVar> retPremenne = new ArrayList<>();
        premenne.entrySet().forEach(e -> retPremenne.add(e.getValue()));
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

    public static GRBLinExpr[] vytvorPodmienkySucetXijPodlaIaUj(Map<KlucSpoja, Map<KlucSpoja, GRBVar>> premenneXij, Map<KlucSpoja, GRBVar> premenneUj, List<Spoj> spoje) {
        List<GRBLinExpr> podmienky = new ArrayList<>();
        for (Spoj iSpoj : spoje) {
            GRBLinExpr podmienka = new GRBLinExpr();
            iSpoj.getMozneNasledovneSpojenia().forEach(jSpoj
                    -> podmienka.addTerm(1, premenneXij.get(iSpoj.getKluc()).get(jSpoj.getKluc())));
            podmienka.addTerm(1, premenneUj.get(iSpoj.getKluc()));
            podmienky.add(podmienka);
        }
        return vytvorPolePodmienok(podmienky);
    }

    public static GRBLinExpr[] vytvorPodmienkySucetXijPodlaJ(Map<KlucSpoja, Map<KlucSpoja, GRBVar>> premenne, List<Spoj> spoje) {
        List<GRBLinExpr> podmienky = new ArrayList<>();
        for (Spoj jSpoj : spoje) {
            if (!jSpoj.getMoznePredosleSpojenia().isEmpty()) {
                GRBLinExpr podmienka = new GRBLinExpr();
                jSpoj.getMoznePredosleSpojenia().forEach(iSpoj
                        -> podmienka.addTerm(1, premenne.get(iSpoj.getKluc()).get(jSpoj.getKluc())));
                podmienky.add(podmienka);
            }
        }
        return vytvorPolePodmienok(podmienky);
    }

    public static GRBLinExpr[] vytvorPodmienkySucetXijPodlaJaVi(Map<KlucSpoja, Map<KlucSpoja, GRBVar>> premenneXij, Map<KlucSpoja, GRBVar> premenneVi, List<Spoj> spoje) {
        List<GRBLinExpr> podmienky = new ArrayList<>();
        for (Spoj jSpoj : spoje) {
            GRBLinExpr podmienka = new GRBLinExpr();
            jSpoj.getMoznePredosleSpojenia().forEach(iSpoj
                    -> podmienka.addTerm(1, premenneXij.get(iSpoj.getKluc()).get(jSpoj.getKluc())));
            podmienka.addTerm(1, premenneVi.get(jSpoj.getKluc()));
            podmienky.add(podmienka);
        }
        return vytvorPolePodmienok(podmienky);
    }

}

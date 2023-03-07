package gurobiModelFunkcie;

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
public final class GarazeFunkcie {

    private GarazeFunkcie() {
    }

    public static double[] vytvorPoleVzdialenostiPreGaraze(GRBVar[] premenneUjkVik, Map<KlucSpoja, Spoj> spoje,
            Map<Integer, Map<Integer, Integer>> vzdialenosti, boolean doGaraze) throws GRBException {
        double[] pole = new double[premenneUjkVik.length];
        for (int i = 0; i < premenneUjkVik.length; i++) {
            String premenna = premenneUjkVik[i].get(GRB.StringAttr.VarName);
            String[] data = premenna.split("_");
            String[] sSpoj = data[1].split(";");
            Spoj spoj = spoje.get(new KlucSpoja(Integer.valueOf(sSpoj[0]), Integer.valueOf(sSpoj[1])));
            pole[i] = doGaraze
                    ? vzdialenosti.get(spoj.getMiestoPrichodu().getId()).get(Integer.valueOf(data[2]))
                    : vzdialenosti.get(Integer.valueOf(data[2])).get(spoj.getMiestoOdchodu().getId());
        }
        return pole;
    }

    public static Map<KlucSpoja, Map<KlucSpoja, Map<Integer, GRBVar>>> vytvorPremenneXijk(GRBModel model, List<Spoj> spoje, List<Integer> garaze) throws GRBException {
        Map<KlucSpoja, Map<KlucSpoja, Map<Integer, GRBVar>>> premenne = new HashMap<>();
        for (Spoj iSpoj : spoje) {
            KlucSpoja iKluc = iSpoj.getKluc();
            for (Spoj jSpoj : iSpoj.getMozneNasledovneSpojenia()) {
                KlucSpoja jKluc = jSpoj.getKluc();
                for (Integer idGaraze : garaze) {
                    premenne.computeIfAbsent(iKluc, k -> new HashMap<>()).computeIfAbsent(jKluc, k -> new HashMap<>())
                            .put(idGaraze, model.addVar(0, 1, 0, GRB.BINARY, "x_" + iKluc.toString() + "_" + jKluc.toString() + "_" + idGaraze));
                }
            }
        }
        return premenne;
    }

    public static Map<KlucSpoja, Map<Integer, GRBVar>> vytvorPremenneUjkVik(GRBModel model, List<Spoj> spoje, List<Integer> garaze, String meno) throws GRBException {
        Map<KlucSpoja, Map<Integer, GRBVar>> premenne = new HashMap<>();
        for (Spoj spoj : spoje) {
            for (Integer idGaraze : garaze) {
                premenne.computeIfAbsent(spoj.getKluc(), k -> new HashMap<>())
                        .put(idGaraze, model.addVar(0, 1, 0, GRB.BINARY, meno + "_" + spoj.getKluc().toString() + "_" + idGaraze));
            }
        }
        return premenne;
    }

    public static GRBVar[] vytvorSucetUjkVjk(Map<KlucSpoja, Map<Integer, GRBVar>> premenne) {
        List<GRBVar> retPremenne = new ArrayList<>();
        premenne.entrySet().forEach(e -> e.getValue().entrySet().forEach(e1 -> retPremenne.add(e1.getValue())));
        return VseobecneFunkcie.vytvorPolePremennych(retPremenne);
    }

    public static void pridajPodmienkyUjViRovneUjkVik(GRBModel model, Map<KlucSpoja, GRBVar> premenneUjVi,
            Map<KlucSpoja, Map<Integer, GRBVar>> premenneUjkVik, String cisloPodmienky) throws GRBException {
        int poradiePodmienky = 1;
        for (Map.Entry<KlucSpoja, GRBVar> entry : premenneUjVi.entrySet()) {
            GRBLinExpr podmienka = new GRBLinExpr();
            premenneUjkVik.get(entry.getKey()).entrySet().forEach(entry1 -> podmienka.addTerm(1, entry1.getValue()));
            model.addConstr(podmienka, GRB.EQUAL, entry.getValue(), cisloPodmienky + "_" + poradiePodmienky);
            poradiePodmienky++;
        }
    }

    public static void pridajPodmienkyXijRovneXijk(GRBModel model, Map<KlucSpoja, Map<KlucSpoja, GRBVar>> premenneXij,
            Map<KlucSpoja, Map<KlucSpoja, Map<Integer, GRBVar>>> premenneXijk, String cisloPodmienky) throws GRBException {
        int poradiePodmienky = 1;
        for (Map.Entry<KlucSpoja, Map<KlucSpoja, GRBVar>> entry : premenneXij.entrySet()) {
            for (Map.Entry<KlucSpoja, GRBVar> entry1 : entry.getValue().entrySet()) {
                GRBLinExpr podmienka = new GRBLinExpr();
                premenneXijk.get(entry.getKey()).get(entry1.getKey()).entrySet().forEach(entry2 -> podmienka.addTerm(1, entry2.getValue()));
                model.addConstr(podmienka, GRB.EQUAL, entry1.getValue(), cisloPodmienky + "_" + poradiePodmienky);
                poradiePodmienky++;
            }
        }
    }

    public static void pridajPodmienkyUjkXijkRovneVikXijk(GRBModel model, Map<KlucSpoja, Map<Integer, GRBVar>> premenneUjk,
            Map<KlucSpoja, Map<Integer, GRBVar>> premenneVik, Map<KlucSpoja, Map<KlucSpoja, Map<Integer, GRBVar>>> premenneXijk,
            List<Spoj> spoje, List<Integer> idGaraze, String cisloPodmienky) throws GRBException {
        int poradiePodmienky = 1;
        for (Integer idGaraz : idGaraze) {
            for (Spoj spoj : spoje) {
                GRBLinExpr podmienkaUj = new GRBLinExpr();
                GRBLinExpr podmienkaVi = new GRBLinExpr();
                spoj.getMozneNasledovneSpojenia().forEach(jSpoj
                        -> podmienkaUj.addTerm(1, premenneXijk.get(spoj.getKluc()).get(jSpoj.getKluc()).get(idGaraz)));
                podmienkaUj.addTerm(1, premenneUjk.get(spoj.getKluc()).get(idGaraz));
                spoj.getMoznePredosleSpojenia().forEach(iSpoj
                        -> podmienkaVi.addTerm(1, premenneXijk.get(iSpoj.getKluc()).get(spoj.getKluc()).get(idGaraz)));
                podmienkaVi.addTerm(1, premenneVik.get(spoj.getKluc()).get(idGaraz));
                model.addConstr(podmienkaUj, GRB.EQUAL, podmienkaVi, cisloPodmienky + "_" + poradiePodmienky);
                poradiePodmienky++;
            }
        }
    }
}

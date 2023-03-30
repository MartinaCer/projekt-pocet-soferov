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
public class GarazFunkcie {

    public static GRBLinExpr[] vytvorPodmienkySucetXijPodlaIaUj(Map<KlucSpoja, Map<KlucSpoja, GRBVar>> premenneXij, Map<KlucSpoja, GRBVar> premenneUj, List<Spoj> spoje) {
        List<GRBLinExpr> podmienky = new ArrayList<>();
        for (Spoj iSpoj : spoje) {
            GRBLinExpr podmienka = new GRBLinExpr();
            iSpoj.getMozneNasledovneSpojenia().forEach(jSpoj -> podmienka.addTerm(1, premenneXij.get(iSpoj.getKluc()).get(jSpoj.getKluc())));
            podmienka.addTerm(1, premenneUj.get(iSpoj.getKluc()));
            podmienky.add(podmienka);
        }
        return VseobecneFunkcie.vytvorPolePodmienok(podmienky);
    }

    public static GRBLinExpr[] vytvorPodmienkySucetXijPodlaJaVi(Map<KlucSpoja, Map<KlucSpoja, GRBVar>> premenneXij, Map<KlucSpoja, GRBVar> premenneVi, List<Spoj> spoje) {
        List<GRBLinExpr> podmienky = new ArrayList<>();
        for (Spoj jSpoj : spoje) {
            GRBLinExpr podmienka = new GRBLinExpr();
            jSpoj.getMoznePredosleSpojenia().forEach(iSpoj -> podmienka.addTerm(1, premenneXij.get(iSpoj.getKluc()).get(jSpoj.getKluc())));
            podmienka.addTerm(1, premenneVi.get(jSpoj.getKluc()));
            podmienky.add(podmienka);
        }
        return VseobecneFunkcie.vytvorPolePodmienok(podmienky);
    }

    public static Map<Spoj.KlucSpoja, GRBVar> vytvorPremenneVsetkySpoje(GRBModel model, List<Spoj> spoje, String meno) throws GRBException {
        Map<Spoj.KlucSpoja, GRBVar> premenne = new HashMap<>();
        for (Spoj spoj : spoje) {
            premenne.put(spoj.getKluc(), model.addVar(0, 1, 0, GRB.BINARY, meno + "_" + spoj.getKluc().toString()));
        }
        return premenne;
    }

    public static GRBVar[] vytvorSucetVsetkySpoje(Map<Spoj.KlucSpoja, GRBVar> premenne) {
        List<GRBVar> retPremenne = new ArrayList<>();
        premenne.entrySet().forEach(e -> retPremenne.add(e.getValue()));
        return VseobecneFunkcie.vytvorPolePremennych(retPremenne);
    }

    public static double[] vytvorPoleVzdialenostiPreGaraz(GRBVar[] premenneUjVi, Map<Spoj.KlucSpoja, Spoj> spoje,
            Map<Integer, Map<Integer, Integer>> vzdialenosti, int idGaraze, boolean doGaraze, int cena) throws GRBException {
        double[] pole = new double[premenneUjVi.length];
        for (int i = 0; i < premenneUjVi.length; i++) {
            String premenna = premenneUjVi[i].get(GRB.StringAttr.VarName);
            String[] data = premenna.split("_");
            String[] sSpoj = data[1].split(";");
            Spoj spoj = spoje.get(new Spoj.KlucSpoja(Integer.valueOf(sSpoj[0]), Integer.valueOf(sSpoj[1])));
            pole[i] = doGaraze
                    ? vzdialenosti.get(spoj.getMiestoPrichodu().getId()).get(idGaraze) * cena
                    : vzdialenosti.get(idGaraze).get(spoj.getMiestoOdchodu().getId()) * cena;
        }
        return pole;
    }

}

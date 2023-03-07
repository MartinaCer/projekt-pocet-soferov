package gurobiModelFunkcie;

import dto.Spoj;
import gurobi.GRB;
import gurobi.GRBException;
import gurobi.GRBLinExpr;
import gurobi.GRBModel;
import gurobi.GRBVar;
import static gurobiModelFunkcie.VseobecneFunkcie.vytvorPolePremennych;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import konfiguracia.Konstanty;

/**
 *
 * @author Martina Cernekova
 */
public class SoferiFunkcie {

    private SoferiFunkcie() {
    }

    public static double[] vytvorPoleCenySoferov(int pocet) {
        double[] pole = new double[pocet];
        for (int i = 0; i < pocet; i++) {
            pole[i] = -Konstanty.CENA_SOFERA;
        }
        return pole;
    }

    public static double[] vytvorPoleVzdialenostiZaDoGaraze(GRBVar[] premenneYij, Map<Spoj.KlucSpoja, Spoj> spoje,
            Map<Integer, Map<Integer, Integer>> vzdialenosti, int idGaraze, int cena) throws GRBException {
        double[] pole = new double[premenneYij.length];
        for (int i = 0; i < premenneYij.length; i++) {
            String premenna = premenneYij[i].get(GRB.StringAttr.VarName);
            String[] data = premenna.split("_");
            String[] sIspoj = data[1].split(";");
            String[] sJspoj = data[2].split(";");
            Spoj iSpoj = spoje.get(new Spoj.KlucSpoja(Integer.valueOf(sIspoj[0]), Integer.valueOf(sIspoj[1])));
            Spoj jSpoj = spoje.get(new Spoj.KlucSpoja(Integer.valueOf(sJspoj[0]), Integer.valueOf(sJspoj[1])));
            pole[i] = (vzdialenosti.get(iSpoj.getMiestoPrichodu().getId()).get(idGaraze)
                    + vzdialenosti.get(idGaraze).get(jSpoj.getMiestoOdchodu().getId())) * cena;
        }
        return pole;
    }

    public static Map<Spoj.KlucSpoja, Map<Spoj.KlucSpoja, GRBVar>> vytvorPremenneYij(GRBModel model, List<Spoj> spoje) throws GRBException {
        Map<Spoj.KlucSpoja, Map<Spoj.KlucSpoja, GRBVar>> premenne = new HashMap<>();
        for (Spoj iSpoj : spoje) {
            Spoj.KlucSpoja iKluc = iSpoj.getKluc();
            for (Spoj jSpoj : iSpoj.getMozneNasledovneZmenySofera()) {
                Spoj.KlucSpoja jKluc = jSpoj.getKluc();
                premenne.computeIfAbsent(iKluc, k -> new HashMap<>()).put(jKluc, model.addVar(0, 1, 0, GRB.BINARY, "y_" + iKluc.toString() + "_" + jKluc.toString()));
            }
        }
        return premenne;
    }

    public static GRBVar[] vytvorSucetYij(Map<Spoj.KlucSpoja, Map<Spoj.KlucSpoja, GRBVar>> premenne) {
        List<GRBVar> retPremenne = new ArrayList<>();
        premenne.entrySet().forEach(e -> e.getValue().entrySet().forEach(e1 -> retPremenne.add(e1.getValue())));
        return vytvorPolePremennych(retPremenne);
    }

    public static GRBLinExpr[] vytvorPodmienkySucetXijYijPodlaIaUj(Map<Spoj.KlucSpoja, Map<Spoj.KlucSpoja, GRBVar>> premenneXij,
            Map<Spoj.KlucSpoja, Map<Spoj.KlucSpoja, GRBVar>> premenneYij, Map<Spoj.KlucSpoja, GRBVar> premenneUj, List<Spoj> spoje) {
        List<GRBLinExpr> podmienky = new ArrayList<>();
        for (Spoj iSpoj : spoje) {
            GRBLinExpr podmienka = new GRBLinExpr();
            iSpoj.getMozneNasledovneSpojenia().forEach(jSpoj -> podmienka.addTerm(1, premenneXij.get(iSpoj.getKluc()).get(jSpoj.getKluc())));
            iSpoj.getMozneNasledovneZmenySofera().forEach(jSpoj -> podmienka.addTerm(1, premenneYij.get(iSpoj.getKluc()).get(jSpoj.getKluc())));
            podmienka.addTerm(1, premenneUj.get(iSpoj.getKluc()));
            podmienky.add(podmienka);
        }
        return VseobecneFunkcie.vytvorPolePodmienok(podmienky);
    }

    public static GRBLinExpr[] vytvorPodmienkySucetXijYijPodlaJaVi(Map<Spoj.KlucSpoja, Map<Spoj.KlucSpoja, GRBVar>> premenneXij,
            Map<Spoj.KlucSpoja, Map<Spoj.KlucSpoja, GRBVar>> premenneYij, Map<Spoj.KlucSpoja, GRBVar> premenneVi, List<Spoj> spoje) {
        List<GRBLinExpr> podmienky = new ArrayList<>();
        for (Spoj jSpoj : spoje) {
            GRBLinExpr podmienka = new GRBLinExpr();
            jSpoj.getMoznePredosleSpojenia().forEach(iSpoj -> podmienka.addTerm(1, premenneXij.get(iSpoj.getKluc()).get(jSpoj.getKluc())));
            jSpoj.getMoznePredosleZmenySofera().forEach(iSpoj -> podmienka.addTerm(1, premenneYij.get(iSpoj.getKluc()).get(jSpoj.getKluc())));
            podmienka.addTerm(1, premenneVi.get(jSpoj.getKluc()));
            podmienky.add(podmienka);
        }
        return VseobecneFunkcie.vytvorPolePodmienok(podmienky);
    }
}

package gurobiModelFunkcie;

import dto.Spoj;
import gurobi.GRB;
import gurobi.GRBException;
import gurobi.GRBLinExpr;
import gurobi.GRBModel;
import gurobi.GRBVar;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Martina Cernekova
 */
public class SpojeFunkcie {

    public static double[] vytvorPolePriorit(GRBVar[] premennePi, Map<Spoj.KlucSpoja, Spoj> spoje) throws GRBException {
        double[] pole = new double[premennePi.length];
        for (int i = 0; i < premennePi.length; i++) {
            String premenna = premennePi[i].get(GRB.StringAttr.VarName);
            String[] data = premenna.split("_");
            String[] sSpoj = data[1].split(";");
            Spoj spoj = spoje.get(new Spoj.KlucSpoja(Integer.valueOf(sSpoj[0]), Integer.valueOf(sSpoj[1])));
            pole[i] = -1000 * spoj.getPriorita();
        }
        return pole;
    }

    public static void pridajPodmienkySucetXijYijPodlaJaVi(GRBModel model, Map<Spoj.KlucSpoja, Map<Spoj.KlucSpoja, GRBVar>> premenneXij,
            Map<Spoj.KlucSpoja, Map<Spoj.KlucSpoja, GRBVar>> premenneYij, Map<Spoj.KlucSpoja, GRBVar> premenneVi,
            Map<Spoj.KlucSpoja, GRBVar> premennePi, List<Spoj> spoje, String cisloPodmienky) throws GRBException {
        int poradiePodmienky = 1;
        for (Spoj iSpoj : spoje) {
            GRBLinExpr podmienka = new GRBLinExpr();
            iSpoj.getMozneNasledovneSpojenia().forEach(jSpoj -> podmienka.addTerm(1, premenneXij.get(iSpoj.getKluc()).get(jSpoj.getKluc())));
            iSpoj.getMozneNasledovneZmenySofera().forEach(jSpoj -> podmienka.addTerm(1, premenneYij.get(iSpoj.getKluc()).get(jSpoj.getKluc())));
            podmienka.addTerm(1, premenneVi.get(iSpoj.getKluc()));
            model.addConstr(podmienka, GRB.EQUAL, premennePi.get(iSpoj.getKluc()), cisloPodmienky + "_" + poradiePodmienky);
            poradiePodmienky++;
        }
    }

    public static void pridajPodmienkySucetXijYijPodlaIaUj(GRBModel model, Map<Spoj.KlucSpoja, Map<Spoj.KlucSpoja, GRBVar>> premenneXij,
            Map<Spoj.KlucSpoja, Map<Spoj.KlucSpoja, GRBVar>> premenneYij, Map<Spoj.KlucSpoja, GRBVar> premenneUj,
            Map<Spoj.KlucSpoja, GRBVar> premennePi, List<Spoj> spoje, String cisloPodmienky) throws GRBException {
        int poradiePodmienky = 1;
        for (Spoj jSpoj : spoje) {
            GRBLinExpr podmienka = new GRBLinExpr();
            jSpoj.getMoznePredosleSpojenia().forEach(iSpoj -> podmienka.addTerm(1, premenneXij.get(iSpoj.getKluc()).get(jSpoj.getKluc())));
            jSpoj.getMoznePredosleZmenySofera().forEach(iSpoj -> podmienka.addTerm(1, premenneYij.get(iSpoj.getKluc()).get(jSpoj.getKluc())));
            podmienka.addTerm(1, premenneUj.get(jSpoj.getKluc()));
            model.addConstr(podmienka, GRB.EQUAL, premennePi.get(jSpoj.getKluc()), cisloPodmienky + "_" + poradiePodmienky);
            poradiePodmienky++;
        }
    }

}

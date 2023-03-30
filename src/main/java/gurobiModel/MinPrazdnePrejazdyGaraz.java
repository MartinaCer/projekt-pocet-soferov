package gurobiModel;

import gurobiModelVypisy.Vypis;
import dto.Data;
import dto.Spoj;
import dto.Spoj.KlucSpoja;
import gurobi.GRB;
import gurobi.GRBEnv;
import gurobi.GRBException;
import gurobi.GRBLinExpr;
import gurobi.GRBModel;
import gurobi.GRBVar;
import gurobiModelFunkcie.GarazFunkcie;
import gurobiModelFunkcie.VseobecneFunkcie;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Martina Cernekova
 */
public class MinPrazdnePrejazdyGaraz {

    public VysledokMinPrejazdyGaraz optimalizuj(Data data, int pocetAutobusov) throws GRBException {
        GRBEnv env = new GRBEnv("minPrazdnePrejazdyGaraz.log");
        GRBModel model = new GRBModel(env);

        List<Spoj> zoznamSpojov = new ArrayList<>(data.getSpoje().values());

        Map<KlucSpoja, Map<KlucSpoja, GRBVar>> xIJ = VseobecneFunkcie.vytvorPremenneXij(model, zoznamSpojov);
        Map<KlucSpoja, GRBVar> uJ = GarazFunkcie.vytvorPremenneVsetkySpoje(model, zoznamSpojov, "u");
        Map<KlucSpoja, GRBVar> vI = GarazFunkcie.vytvorPremenneVsetkySpoje(model, zoznamSpojov, "v");
        model.update();

        GRBVar[] premenneXij = VseobecneFunkcie.vytvorSucetNasledovnych(xIJ);
        GRBVar[] premenneUj = GarazFunkcie.vytvorSucetVsetkySpoje(uJ);
        GRBVar[] premenneVi = GarazFunkcie.vytvorSucetVsetkySpoje(vI);
        GRBLinExpr ucelovaFunkcia = new GRBLinExpr();
        ucelovaFunkcia.addTerms(VseobecneFunkcie.vytvorPoleVzdialenosti(premenneXij, data.getSpoje(), data.getKmVzdialenosti(), 1), premenneXij);
        ucelovaFunkcia.addTerms(GarazFunkcie.vytvorPoleVzdialenostiPreGaraz(premenneUj, data.getSpoje(), data.getKmVzdialenosti(), data.getKonfiguracia().getGaraz(), true, 1), premenneUj);
        ucelovaFunkcia.addTerms(GarazFunkcie.vytvorPoleVzdialenostiPreGaraz(premenneVi, data.getSpoje(), data.getKmVzdialenosti(), data.getKonfiguracia().getGaraz(), false, 1), premenneVi);
        model.setObjective(ucelovaFunkcia, GRB.MINIMIZE);

        GRBLinExpr[] podmienky1 = GarazFunkcie.vytvorPodmienkySucetXijPodlaIaUj(xIJ, uJ, zoznamSpojov);
        model.addConstrs(podmienky1, VseobecneFunkcie.vytvorPoleRovnost(podmienky1.length, GRB.EQUAL),
                VseobecneFunkcie.vytvorPoleJednotiek(podmienky1.length), VseobecneFunkcie.vytvorNazvyPodmienok(podmienky1.length, "1"));

        GRBLinExpr[] podmienky2 = GarazFunkcie.vytvorPodmienkySucetXijPodlaJaVi(xIJ, vI, zoznamSpojov);
        model.addConstrs(podmienky2, VseobecneFunkcie.vytvorPoleRovnost(podmienky2.length, GRB.EQUAL),
                VseobecneFunkcie.vytvorPoleJednotiek(podmienky2.length), VseobecneFunkcie.vytvorNazvyPodmienok(podmienky2.length, "2"));

        GRBLinExpr podmienkaPocetAutobusov = new GRBLinExpr();
        podmienkaPocetAutobusov.addTerms(VseobecneFunkcie.vytvorPoleJednotiek(premenneXij.length), premenneXij);
        model.addConstr(podmienkaPocetAutobusov, GRB.EQUAL, data.getSpoje().size() - pocetAutobusov, "3");

        model.optimize();

        List<String> spoje = new ArrayList<>();
        for (GRBVar var : model.getVars()) {
            if (var.get(GRB.DoubleAttr.X) == 1) {
                String v = var.get(GRB.StringAttr.VarName);
                if (v.charAt(0) == 'x') {
                    spoje.add(v);
                }
            }
        }
        VysledokMinPrejazdyGaraz vysledok = new VysledokMinPrejazdyGaraz((int) model.get(GRB.DoubleAttr.ObjVal), Vypis.vytvorTurnusy(spoje, data.getSpoje()));
        model.dispose();
        env.dispose();
        return vysledok;
    }

    public static class VysledokMinPrejazdyGaraz {

        private final int pocetKilometrov;
        private final List<List<Spoj>> turnusy;

        public VysledokMinPrejazdyGaraz(int pocetKilometrov, List<List<Spoj>> turnusy) {
            this.pocetKilometrov = pocetKilometrov;
            this.turnusy = turnusy;
        }

        public int getPocetKilometrov() {
            return pocetKilometrov;
        }

        public List<List<Spoj>> getTurnusy() {
            return turnusy;
        }

    }
}

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
import gurobiModelFunkcie.VseobecneFunkcie;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Martina Cernekova
 */
public class MinPrazdnePrejazdy {

    public VysledokMinPrejazdy optimalizuj(Data data, int pocetAutobusov) throws GRBException {
        GRBEnv env = new GRBEnv("minPrazdnePrejazdy.log");
        GRBModel model = new GRBModel(env);

        List<Spoj> zoznamSpojov = new ArrayList<>(data.getSpoje().values());

        Map<KlucSpoja, Map<KlucSpoja, GRBVar>> premenne = VseobecneFunkcie.vytvorPremenneXij(model, zoznamSpojov);
        model.update();

        GRBVar[] premenneXij = VseobecneFunkcie.vytvorSucetNasledovnych(premenne);
        GRBLinExpr ucelovaFunkcia = new GRBLinExpr();
        ucelovaFunkcia.addTerms(VseobecneFunkcie.vytvorPoleVzdialenosti(premenneXij, data.getSpoje(), data.getKmVzdialenosti(), 1), premenneXij);
        model.setObjective(ucelovaFunkcia, GRB.MINIMIZE);

        GRBLinExpr[] podmienky1 = VseobecneFunkcie.vytvorPodmienkySucetXijPodlaI(premenne, zoznamSpojov);
        model.addConstrs(podmienky1, VseobecneFunkcie.vytvorPoleRovnost(podmienky1.length, GRB.LESS_EQUAL),
                VseobecneFunkcie.vytvorPoleJednotiek(podmienky1.length), VseobecneFunkcie.vytvorNazvyPodmienok(podmienky1.length, "1"));

        GRBLinExpr[] podmienky2 = VseobecneFunkcie.vytvorPodmienkySucetXijPodlaJ(premenne, zoznamSpojov);
        model.addConstrs(podmienky2, VseobecneFunkcie.vytvorPoleRovnost(podmienky2.length, GRB.LESS_EQUAL),
                VseobecneFunkcie.vytvorPoleJednotiek(podmienky2.length), VseobecneFunkcie.vytvorNazvyPodmienok(podmienky2.length, "2"));

        GRBLinExpr podmienkaPocetAutobusov = new GRBLinExpr();
        podmienkaPocetAutobusov.addTerms(VseobecneFunkcie.vytvorPoleJednotiek(premenneXij.length), premenneXij);
        model.addConstr(podmienkaPocetAutobusov, GRB.EQUAL, data.getSpoje().size() - pocetAutobusov, "3");

        model.optimize();

        List<String> spoje = new ArrayList<>();
        for (GRBVar var : model.getVars()) {
            if (var.get(GRB.DoubleAttr.X) == 1) {
                spoje.add(var.get(GRB.StringAttr.VarName));
            }
        }
        VysledokMinPrejazdy vysledok = new VysledokMinPrejazdy((int) model.get(GRB.DoubleAttr.ObjVal), Vypis.vytvorTurnusy(spoje, data.getSpoje()));
        model.dispose();
        env.dispose();
        return vysledok;
    }

    public static class VysledokMinPrejazdy {

        private final int pocetKilometrov;
        private final List<List<Spoj>> turnusy;

        public VysledokMinPrejazdy(int pocetKilometrov, List<List<Spoj>> turnusy) {
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

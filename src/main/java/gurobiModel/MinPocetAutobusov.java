package gurobiModel;

import gurobiModelVypisy.Vypis;
import dataObjekty.Data;
import dataObjekty.Spoj;
import dataObjekty.Spoj.KlucSpoja;
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
public class MinPocetAutobusov {

    public VysledokMinAutobusy optimalizuj(Data data) throws GRBException {
        GRBEnv env = new GRBEnv("minPocetAutobusov.log");
        GRBModel model = new GRBModel(env);

        List<Spoj> zoznamSpojov = new ArrayList<>(data.getSpoje().values());

        Map<KlucSpoja, Map<KlucSpoja, GRBVar>> premenne = VseobecneFunkcie.vytvorPremenneXij(model, zoznamSpojov);

        GRBVar[] premenneXij = VseobecneFunkcie.vytvorSucetNasledovnych(premenne);
        GRBLinExpr ucelovaFunkcia = new GRBLinExpr();
        ucelovaFunkcia.addTerms(VseobecneFunkcie.vytvorPoleJednotiek(premenneXij.length), premenneXij);
        model.setObjective(ucelovaFunkcia, GRB.MAXIMIZE);

        GRBLinExpr[] podmienky1 = VseobecneFunkcie.vytvorPodmienkySucetXijPodlaI(premenne, zoznamSpojov);
        model.addConstrs(podmienky1, VseobecneFunkcie.vytvorPoleRovnost(podmienky1.length, GRB.LESS_EQUAL),
                VseobecneFunkcie.vytvorPoleJednotiek(podmienky1.length), VseobecneFunkcie.vytvorNazvyPodmienok(podmienky1.length, "1"));

        GRBLinExpr[] podmienky2 = VseobecneFunkcie.vytvorPodmienkySucetXijPodlaJ(premenne, zoznamSpojov);
        model.addConstrs(podmienky2, VseobecneFunkcie.vytvorPoleRovnost(podmienky2.length, GRB.LESS_EQUAL),
                VseobecneFunkcie.vytvorPoleJednotiek(podmienky2.length), VseobecneFunkcie.vytvorNazvyPodmienok(podmienky2.length, "2"));

        model.optimize();

        List<String> spoje = new ArrayList<>();
        for (GRBVar var : model.getVars()) {
            if (var.get(GRB.DoubleAttr.X) == 1) {
                spoje.add(var.get(GRB.StringAttr.VarName));
            }
        }
        VysledokMinAutobusy vysledok = new VysledokMinAutobusy((int) (data.getSpoje().size() - model.get(GRB.DoubleAttr.ObjVal)), Vypis.vytvorTurnusy(spoje, data.getSpoje()));
        model.dispose();
        env.dispose();
        return vysledok;
    }

    public static class VysledokMinAutobusy {

        private final int pocetAutobusov;
        private final List<List<Spoj>> turnusy;

        public VysledokMinAutobusy(int pocetAutobusov, List<List<Spoj>> turnusy) {
            this.pocetAutobusov = pocetAutobusov;
            this.turnusy = turnusy;
        }

        public int getPocetAutobusov() {
            return pocetAutobusov;
        }

        public List<List<Spoj>> getTurnusy() {
            return turnusy;
        }

    }
}

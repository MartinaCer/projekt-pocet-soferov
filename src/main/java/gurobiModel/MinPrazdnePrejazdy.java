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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Martina Cernekova
 */
public class MinPrazdnePrejazdy {

    public void optimalizuj(Data data, int pocetAutobusov) {
        try {
            GRBEnv env = new GRBEnv("minPrazdnePrejazdy.log");
            GRBModel model = new GRBModel(env);
            
            List<Spoj> zoznamSpojov = new ArrayList<>(data.getSpoje().values());

            Map<KlucSpoja, Map<KlucSpoja, GRBVar>> premenne = VseobecneFunkcie.vytvorPremenneXij(model, zoznamSpojov);
            model.update();

            GRBVar[] premenneXij = VseobecneFunkcie.vytvorSucetXij(premenne);
            GRBLinExpr ucelovaFunkcia = new GRBLinExpr();
            ucelovaFunkcia.addTerms(VseobecneFunkcie.vytvorPoleVzdialenosti(premenneXij, data.getSpoje(), data.getKmVzdialenosti(), 1), premenneXij);
            model.setObjective(ucelovaFunkcia, GRB.MINIMIZE);

            GRBLinExpr[] podmienky1 = VseobecneFunkcie.vytvorPodmienkySucetXijPodlaI(premenne, zoznamSpojov);
            model.addConstrs(podmienky1, VseobecneFunkcie.vytvorPoleMensiRovny(podmienky1.length),
                    VseobecneFunkcie.vytvorPoleJednotiek(podmienky1.length), VseobecneFunkcie.vytvorNazvyPodmienok(podmienky1.length, "1"));

            GRBLinExpr[] podmienky2 = VseobecneFunkcie.vytvorPodmienkySucetXijPodlaJ(premenne, zoznamSpojov);
            model.addConstrs(podmienky2, VseobecneFunkcie.vytvorPoleMensiRovny(podmienky2.length),
                    VseobecneFunkcie.vytvorPoleJednotiek(podmienky2.length), VseobecneFunkcie.vytvorNazvyPodmienok(podmienky2.length, "2"));

            GRBLinExpr podmienkaPocetAutobusov = new GRBLinExpr();
            podmienkaPocetAutobusov.addTerms(VseobecneFunkcie.vytvorPoleJednotiek(premenneXij.length), premenneXij);
            model.addConstr(podmienkaPocetAutobusov, GRB.EQUAL, data.getSpoje().size() - pocetAutobusov, "3");

            model.optimize();

            System.out.println("Minimálne prázdne prejazdy: " + model.get(GRB.DoubleAttr.ObjVal) + " kilometrov");
            List<String> spoje = new ArrayList<>();
            for (GRBVar var : model.getVars()) {
                if (var.get(GRB.DoubleAttr.X) == 1) {
                    spoje.add(var.get(GRB.StringAttr.VarName));
                }
            }
            Vypis.vypisTurnusy(Vypis.vytvorTurnusy(spoje, data.getSpoje()));
            model.dispose();
            env.dispose();
        } catch (GRBException ex) {
            Logger.getLogger(MinPrazdnePrejazdy.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

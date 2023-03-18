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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Martina Cernekova
 */
public class MinPrazdnePrejazdyGaraz {

    public void optimalizuj(Data data, int pocetAutobusov) {
        try {
            GRBEnv env = new GRBEnv("minPrazdnePrejazdyGaraz.log");
            GRBModel model = new GRBModel(env);
            
            List<Spoj> zoznamSpojov = new ArrayList<>(data.getSpoje().values());

            Map<KlucSpoja, Map<KlucSpoja, GRBVar>> xIJ = VseobecneFunkcie.vytvorPremenneXij(model, zoznamSpojov);
            Map<KlucSpoja, GRBVar> uJ = GarazFunkcie.vytvorPremenneUjVi(model, zoznamSpojov, "u");
            Map<KlucSpoja, GRBVar> vI = GarazFunkcie.vytvorPremenneUjVi(model, zoznamSpojov, "v");
            model.update();

            GRBVar[] premenneXij = VseobecneFunkcie.vytvorSucetXij(xIJ);
            GRBVar[] premenneUj = GarazFunkcie.vytvorSucetUjVj(uJ);
            GRBVar[] premenneVi = GarazFunkcie.vytvorSucetUjVj(vI);
            GRBLinExpr ucelovaFunkcia = new GRBLinExpr();
            ucelovaFunkcia.addTerms(VseobecneFunkcie.vytvorPoleVzdialenosti(premenneXij, data.getSpoje(), data.getKmVzdialenosti(), 1), premenneXij);
            ucelovaFunkcia.addTerms(GarazFunkcie.vytvorPoleVzdialenostiPreGaraz(premenneUj, data.getSpoje(), data.getKmVzdialenosti(), data.getGaraze().get(0), true, 1), premenneUj);
            ucelovaFunkcia.addTerms(GarazFunkcie.vytvorPoleVzdialenostiPreGaraz(premenneVi, data.getSpoje(), data.getKmVzdialenosti(), data.getGaraze().get(0), false, 1), premenneVi);
            model.setObjective(ucelovaFunkcia, GRB.MINIMIZE);

            GRBLinExpr[] podmienky1 = GarazFunkcie.vytvorPodmienkySucetXijPodlaIaUj(xIJ, uJ, zoznamSpojov);
            model.addConstrs(podmienky1, VseobecneFunkcie.vytvorPoleRovny(podmienky1.length),
                    VseobecneFunkcie.vytvorPoleJednotiek(podmienky1.length), VseobecneFunkcie.vytvorNazvyPodmienok(podmienky1.length, "1"));

            GRBLinExpr[] podmienky2 = GarazFunkcie.vytvorPodmienkySucetXijPodlaJaVi(xIJ, vI, zoznamSpojov);
            model.addConstrs(podmienky2, VseobecneFunkcie.vytvorPoleRovny(podmienky2.length),
                    VseobecneFunkcie.vytvorPoleJednotiek(podmienky2.length), VseobecneFunkcie.vytvorNazvyPodmienok(podmienky2.length, "2"));

            GRBLinExpr podmienkaPocetAutobusov = new GRBLinExpr();
            podmienkaPocetAutobusov.addTerms(VseobecneFunkcie.vytvorPoleJednotiek(premenneXij.length), premenneXij);
            model.addConstr(podmienkaPocetAutobusov, GRB.EQUAL, data.getSpoje().size() - pocetAutobusov, "3");

            model.optimize();

            System.out.println("Minimálne prázdne prejazdy s garážou: " + model.get(GRB.DoubleAttr.ObjVal) + " kilometrov");
            List<String> spoje = new ArrayList<>();
            for (GRBVar var : model.getVars()) {
                if (var.get(GRB.DoubleAttr.X) == 1) {
                    String v = var.get(GRB.StringAttr.VarName);
                    if (v.charAt(0) == 'x') {
                        spoje.add(v);
                    }
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

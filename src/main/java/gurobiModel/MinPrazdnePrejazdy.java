package gurobiModel;

import dto.Data;
import dto.Spoj.KlucSpoja;
import gurobi.GRB;
import gurobi.GRBEnv;
import gurobi.GRBException;
import gurobi.GRBLinExpr;
import gurobi.GRBModel;
import gurobi.GRBVar;
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

            Map<KlucSpoja, Map<KlucSpoja, GRBVar>> premenne = FunkciePreModel.vytvorPremenneXij(model, new ArrayList<>(data.getSpoje().values()));
            model.update();

            GRBVar[] premenneXij = FunkciePreModel.vytvorSucetXij(premenne);
            GRBLinExpr ucelovaFunkcia = new GRBLinExpr();
            ucelovaFunkcia.addTerms(FunkciePreModel.vytvorPoleVzdialenosti(premenneXij, data.getSpoje(), data.getVzdialenosti()), premenneXij);
            model.setObjective(ucelovaFunkcia, GRB.MINIMIZE);

            GRBLinExpr[] podmienky1 = FunkciePreModel.vytvorPodmienkySucetXijPodlaI(premenne, new ArrayList<>(data.getSpoje().values()));
            model.addConstrs(podmienky1, FunkciePreModel.vytvorPoleMensiRovny(podmienky1.length),
                    FunkciePreModel.vytvorPoleJednotiek(podmienky1.length), FunkciePreModel.vytvorNazvyPodmienok(podmienky1.length, "1"));

            GRBLinExpr[] podmienky2 = FunkciePreModel.vytvorPodmienkySucetXijPodlaJ(premenne, new ArrayList<>(data.getSpoje().values()));
            model.addConstrs(podmienky2, FunkciePreModel.vytvorPoleMensiRovny(podmienky2.length),
                    FunkciePreModel.vytvorPoleJednotiek(podmienky2.length), FunkciePreModel.vytvorNazvyPodmienok(podmienky2.length, "2"));

            GRBLinExpr podmienkaPocetAutobusov = new GRBLinExpr();
            podmienkaPocetAutobusov.addTerms(FunkciePreModel.vytvorPoleJednotiek(premenneXij.length), premenneXij);
            model.addConstr(podmienkaPocetAutobusov, GRB.EQUAL, data.getSpoje().size() - pocetAutobusov, "3");

            model.optimize();

            System.out.println("Minimálne prázdne prejazdy: " + model.get(GRB.DoubleAttr.ObjVal) + " sekúnd");
            List<String> spoje = new ArrayList<>();
            for (GRBVar var : model.getVars()) {
                if (var.get(GRB.DoubleAttr.X) == 1) {
                    spoje.add(var.get(GRB.StringAttr.VarName));
                }
            }
            VypisyPreModel.vytvorVypisTurnusy(spoje, data.getSpoje());
            model.dispose();
            env.dispose();
        } catch (GRBException ex) {
            Logger.getLogger(MinPrazdnePrejazdy.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

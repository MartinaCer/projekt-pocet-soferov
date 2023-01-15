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
public class MinPocetAutobusov {

    public void optimalizuj(Data data) {
        try {
            GRBEnv env = new GRBEnv("minPocetAutobusov.log");
            GRBModel model = new GRBModel(env);

            Map<KlucSpoja, Map<KlucSpoja, GRBVar>> premenne = FunkciePreModel.vytvorPremenneXij(model, new ArrayList<>(data.getSpoje().values()));

            GRBVar[] premenneXij = FunkciePreModel.vytvorSucetXij(premenne);
            GRBLinExpr ucelovaFunkcia = new GRBLinExpr();
            ucelovaFunkcia.addTerms(FunkciePreModel.vytvorPoleJednotiek(premenneXij.length), premenneXij);
            model.setObjective(ucelovaFunkcia, GRB.MAXIMIZE);

            GRBLinExpr[] podmienky1 = FunkciePreModel.vytvorPodmienkySucetXijPodlaI(premenne, new ArrayList<>(data.getSpoje().values()));
            model.addConstrs(podmienky1, FunkciePreModel.vytvorPoleMensiRovny(podmienky1.length),
                    FunkciePreModel.vytvorPoleJednotiek(podmienky1.length), FunkciePreModel.vytvorNazvyPodmienok(podmienky1.length, "1"));

            GRBLinExpr[] podmienky2 = FunkciePreModel.vytvorPodmienkySucetXijPodlaJ(premenne, new ArrayList<>(data.getSpoje().values()));
            model.addConstrs(podmienky2, FunkciePreModel.vytvorPoleMensiRovny(podmienky2.length),
                    FunkciePreModel.vytvorPoleJednotiek(podmienky2.length), FunkciePreModel.vytvorNazvyPodmienok(podmienky2.length, "2"));

            model.optimize();

            System.out.println("Minimálny počet autobusov: " + (data.getSpoje().size() - model.get(GRB.DoubleAttr.ObjVal)));
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
            Logger.getLogger(MinPocetAutobusov.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

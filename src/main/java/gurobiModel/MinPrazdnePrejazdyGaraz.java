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
public class MinPrazdnePrejazdyGaraz {

    public void optimalizuj(Data data, int pocetAutobusov) {
        try {
            GRBEnv env = new GRBEnv("minPrazdnePrejazdyGaraz.log");
            GRBModel model = new GRBModel(env);

            Map<KlucSpoja, Map<KlucSpoja, GRBVar>> xIJ = FunkciePreModel.vytvorPremenneXij(model, new ArrayList<>(data.getSpoje().values()));
            Map<KlucSpoja, GRBVar> uJ = FunkciePreModel.vytvorPremenneUjVi(model, new ArrayList<>(data.getSpoje().values()), "u");
            Map<KlucSpoja, GRBVar> vI = FunkciePreModel.vytvorPremenneUjVi(model, new ArrayList<>(data.getSpoje().values()), "v");
            model.update();

            GRBVar[] premenneXij = FunkciePreModel.vytvorSucetXij(xIJ);
            GRBVar[] premenneUj = FunkciePreModel.vytvorSucetUjVj(uJ);
            GRBVar[] premenneVi = FunkciePreModel.vytvorSucetUjVj(vI);
            GRBLinExpr ucelovaFunkcia = new GRBLinExpr();
            ucelovaFunkcia.addTerms(FunkciePreModel.vytvorPoleVzdialenosti(premenneXij, data.getSpoje(), data.getVzdialenosti()), premenneXij);
            ucelovaFunkcia.addTerms(FunkciePreModel.vytvorPoleVzdialenostiGaraz(premenneUj, data.getSpoje(), data.getVzdialenosti(), data.getGaraze().get(0), true), premenneUj);
            ucelovaFunkcia.addTerms(FunkciePreModel.vytvorPoleVzdialenostiGaraz(premenneVi, data.getSpoje(), data.getVzdialenosti(), data.getGaraze().get(0), false), premenneVi);
            model.setObjective(ucelovaFunkcia, GRB.MINIMIZE);

            GRBLinExpr[] podmienky1 = FunkciePreModel.vytvorPodmienkySucetXijPodlaIaUj(xIJ, uJ, new ArrayList<>(data.getSpoje().values()));
            model.addConstrs(podmienky1, FunkciePreModel.vytvorPoleRovny(podmienky1.length),
                    FunkciePreModel.vytvorPoleJednotiek(podmienky1.length), FunkciePreModel.vytvorNazvyPodmienok(podmienky1.length, "1"));

            GRBLinExpr[] podmienky2 = FunkciePreModel.vytvorPodmienkySucetXijPodlaJaVi(xIJ, vI, new ArrayList<>(data.getSpoje().values()));
            model.addConstrs(podmienky2, FunkciePreModel.vytvorPoleRovny(podmienky2.length),
                    FunkciePreModel.vytvorPoleJednotiek(podmienky2.length), FunkciePreModel.vytvorNazvyPodmienok(podmienky2.length, "2"));

            GRBLinExpr podmienkaPocetAutobusov = new GRBLinExpr();
            podmienkaPocetAutobusov.addTerms(FunkciePreModel.vytvorPoleJednotiek(premenneXij.length), premenneXij);
            model.addConstr(podmienkaPocetAutobusov, GRB.EQUAL, data.getSpoje().size() - pocetAutobusov, "3");

            model.optimize();

            System.out.println("Minimálne prázdne prejazdy s garážou: " + model.get(GRB.DoubleAttr.ObjVal) + " sekúnd");
            List<String> spoje = new ArrayList<>();
            List<String> prve = new ArrayList<>();
            List<String> posledne = new ArrayList<>();
            for (GRBVar var : model.getVars()) {
                if (var.get(GRB.DoubleAttr.X) == 1) {
                    String v = var.get(GRB.StringAttr.VarName);
                    switch (v.charAt(0)) {
                        case 'x':
                            spoje.add(v);
                            break;
                        case 'v':
                            prve.add(v);
                            break;
                        default:
                            posledne.add(v);
                            break;
                    }
                }
            }
            VypisyPreModel.vytvorVypisTurnusy(spoje, data.getSpoje());
            VypisyPreModel.vypisSpoje(prve, data.getSpoje(), true);
            VypisyPreModel.vypisSpoje(posledne, data.getSpoje(), false);
            model.dispose();
            env.dispose();
        } catch (GRBException ex) {
            Logger.getLogger(MinPrazdnePrejazdy.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

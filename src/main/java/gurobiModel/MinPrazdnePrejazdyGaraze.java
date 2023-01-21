package gurobiModel;

import dto.Data;
import dto.Spoj;
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
public class MinPrazdnePrejazdyGaraze {

    public void optimalizuj(Data data, int pocetAutobusov) {
        try {
            GRBEnv env = new GRBEnv("minPrazdnePrejazdyGaraze.log");
            GRBModel model = new GRBModel(env);

            Map<KlucSpoja, Map<KlucSpoja, GRBVar>> xIJ = FunkciePreModel.vytvorPremenneXij(model, new ArrayList<>(data.getSpoje().values()));
            Map<KlucSpoja, Map<KlucSpoja, Map<Integer, GRBVar>>> xIJK = FunkciePreModel.vytvorPremenneXijk(model, new ArrayList<>(data.getSpoje().values()), data.getGaraze());
            Map<KlucSpoja, GRBVar> uJ = FunkciePreModel.vytvorPremenneUjVi(model, new ArrayList<>(data.getSpoje().values()), "u");
            Map<KlucSpoja, Map<Integer, GRBVar>> uJK = FunkciePreModel.vytvorPremenneUjkVik(model, new ArrayList<>(data.getSpoje().values()), data.getGaraze(), "u");
            Map<KlucSpoja, GRBVar> vI = FunkciePreModel.vytvorPremenneUjVi(model, new ArrayList<>(data.getSpoje().values()), "v");
            Map<KlucSpoja, Map<Integer, GRBVar>> vIK = FunkciePreModel.vytvorPremenneUjkVik(model, new ArrayList<>(data.getSpoje().values()), data.getGaraze(), "v");
            model.update();

            GRBVar[] premenneXij = FunkciePreModel.vytvorSucetXij(xIJ);
            GRBVar[] premenneUjk = FunkciePreModel.vytvorSucetUjkVjk(uJK);
            GRBVar[] premenneVik = FunkciePreModel.vytvorSucetUjkVjk(vIK);
            GRBLinExpr ucelovaFunkcia = new GRBLinExpr();
            ucelovaFunkcia.addTerms(FunkciePreModel.vytvorPoleVzdialenosti(premenneXij, data.getSpoje(), data.getVzdialenosti()), premenneXij);
            ucelovaFunkcia.addTerms(FunkciePreModel.vytvorPoleVzdialenostiPreGaraze(premenneUjk, data.getSpoje(), data.getVzdialenosti(), true), premenneUjk);
            ucelovaFunkcia.addTerms(FunkciePreModel.vytvorPoleVzdialenostiPreGaraze(premenneVik, data.getSpoje(), data.getVzdialenosti(), false), premenneVik);
            model.setObjective(ucelovaFunkcia, GRB.MINIMIZE);

            GRBLinExpr[] podmienky1 = FunkciePreModel.vytvorPodmienkySucetXijPodlaIaUj(xIJ, uJ, new ArrayList<>(data.getSpoje().values()));
            model.addConstrs(podmienky1, FunkciePreModel.vytvorPoleRovny(podmienky1.length),
                    FunkciePreModel.vytvorPoleJednotiek(podmienky1.length), FunkciePreModel.vytvorNazvyPodmienok(podmienky1.length, "1"));

            GRBLinExpr[] podmienky2 = FunkciePreModel.vytvorPodmienkySucetXijPodlaJaVi(xIJ, vI, new ArrayList<>(data.getSpoje().values()));
            model.addConstrs(podmienky2, FunkciePreModel.vytvorPoleRovny(podmienky2.length),
                    FunkciePreModel.vytvorPoleJednotiek(podmienky2.length), FunkciePreModel.vytvorNazvyPodmienok(podmienky2.length, "2"));

            FunkciePreModel.pridajPodmienkyUjViRovneUjkVik(model, uJ, uJK, "3");
            FunkciePreModel.pridajPodmienkyUjViRovneUjkVik(model, vI, vIK, "4");
            FunkciePreModel.pridajPodmienkyXijRovneXijk(model, xIJ, xIJK, "5");
            FunkciePreModel.pridajPodmienkyUjkXijkRovneVikXijk(model, uJK, vIK, xIJK, new ArrayList<>(data.getSpoje().values()), data.getGaraze(), "6");

            GRBLinExpr podmienkaPocetAutobusov = new GRBLinExpr();
            podmienkaPocetAutobusov.addTerms(FunkciePreModel.vytvorPoleJednotiek(premenneXij.length), premenneXij);
            model.addConstr(podmienkaPocetAutobusov, GRB.EQUAL, data.getSpoje().size() - pocetAutobusov, "7");

            model.optimize();

            System.out.println("Minimálne prázdne prejazdy s garážami: " + model.get(GRB.DoubleAttr.ObjVal) + " sekúnd");
            List<String> spoje = new ArrayList<>();
            List<String> prve = new ArrayList<>();
            List<String> posledne = new ArrayList<>();
            for (GRBVar var : model.getVars()) {
                if (var.get(GRB.DoubleAttr.X) == 1) {
                    String v = var.get(GRB.StringAttr.VarName);
                    switch (v.charAt(0)) {
                        case 'x':
                            if (v.chars().filter(c -> c == '_').count() == 3) {
                                spoje.add(v);
                            }
                            break;
                        case 'v':
                            if (v.chars().filter(c -> c == '_').count() == 2) {
                                prve.add(v);
                            }
                            break;
                        default:
                            if (v.chars().filter(c -> c == '_').count() == 2) {
                                posledne.add(v);
                            }
                            break;
                    }
                }
            }
            VypisyPreModel.vytvorVypisTurnusyGaraze(spoje, data.getSpoje());
            VypisyPreModel.vypisSpojeGaraze(prve, data.getSpoje(), true);
            VypisyPreModel.vypisSpojeGaraze(posledne, data.getSpoje(), false);
            model.dispose();
            env.dispose();
        } catch (GRBException ex) {
            Logger.getLogger(MinPrazdnePrejazdy.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

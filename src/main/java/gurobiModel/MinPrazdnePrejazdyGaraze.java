package gurobiModel;

import gurobiModelFunkcie.GarazeFunkcie;
import com.itextpdf.text.DocumentException;
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
import gurobiModelFunkcie.VypisGaraze;
import gurobiModelFunkcie.VypisGaraze.SpojGaraz;
import importExport.ImportExportDat;
import java.io.FileNotFoundException;
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

            List<Spoj> zoznamSpojov = new ArrayList<>(data.getSpoje().values());

            Map<KlucSpoja, Map<KlucSpoja, GRBVar>> xIJ = VseobecneFunkcie.vytvorPremenneXij(model, zoznamSpojov);
            Map<KlucSpoja, Map<KlucSpoja, Map<Integer, GRBVar>>> xIJK = GarazeFunkcie.vytvorPremenneXijk(model, zoznamSpojov, data.getGaraze());
            Map<KlucSpoja, GRBVar> uJ = GarazFunkcie.vytvorPremenneUjVi(model, zoznamSpojov, "u");
            Map<KlucSpoja, Map<Integer, GRBVar>> uJK = GarazeFunkcie.vytvorPremenneUjkVik(model, zoznamSpojov, data.getGaraze(), "u");
            Map<KlucSpoja, GRBVar> vI = GarazFunkcie.vytvorPremenneUjVi(model, zoznamSpojov, "v");
            Map<KlucSpoja, Map<Integer, GRBVar>> vIK = GarazeFunkcie.vytvorPremenneUjkVik(model, zoznamSpojov, data.getGaraze(), "v");
            model.update();

            GRBVar[] premenneXij = VseobecneFunkcie.vytvorSucetXij(xIJ);
            GRBVar[] premenneUjk = GarazeFunkcie.vytvorSucetUjkVjk(uJK);
            GRBVar[] premenneVik = GarazeFunkcie.vytvorSucetUjkVjk(vIK);
            GRBLinExpr ucelovaFunkcia = new GRBLinExpr();
            ucelovaFunkcia.addTerms(VseobecneFunkcie.vytvorPoleVzdialenosti(premenneXij, data.getSpoje(), data.getKmVzdialenosti(), 1), premenneXij);
            ucelovaFunkcia.addTerms(GarazeFunkcie.vytvorPoleVzdialenostiPreGaraze(premenneUjk, data.getSpoje(), data.getKmVzdialenosti(), true), premenneUjk);
            ucelovaFunkcia.addTerms(GarazeFunkcie.vytvorPoleVzdialenostiPreGaraze(premenneVik, data.getSpoje(), data.getKmVzdialenosti(), false), premenneVik);
            model.setObjective(ucelovaFunkcia, GRB.MINIMIZE);

            GRBLinExpr[] podmienky1 = GarazFunkcie.vytvorPodmienkySucetXijPodlaIaUj(xIJ, uJ, zoznamSpojov);
            model.addConstrs(podmienky1, VseobecneFunkcie.vytvorPoleRovny(podmienky1.length),
                    VseobecneFunkcie.vytvorPoleJednotiek(podmienky1.length), VseobecneFunkcie.vytvorNazvyPodmienok(podmienky1.length, "1"));

            GRBLinExpr[] podmienky2 = GarazFunkcie.vytvorPodmienkySucetXijPodlaJaVi(xIJ, vI, zoznamSpojov);
            model.addConstrs(podmienky2, VseobecneFunkcie.vytvorPoleRovny(podmienky2.length),
                    VseobecneFunkcie.vytvorPoleJednotiek(podmienky2.length), VseobecneFunkcie.vytvorNazvyPodmienok(podmienky2.length, "2"));

            GarazeFunkcie.pridajPodmienkyUjViRovneUjkVik(model, uJ, uJK, "3");
            GarazeFunkcie.pridajPodmienkyUjViRovneUjkVik(model, vI, vIK, "4");
            GarazeFunkcie.pridajPodmienkyXijRovneXijk(model, xIJ, xIJK, "5");
            GarazeFunkcie.pridajPodmienkyUjkXijkRovneVikXijk(model, uJK, vIK, xIJK, zoznamSpojov, data.getGaraze(), "6");

            GRBLinExpr podmienkaPocetAutobusov = new GRBLinExpr();
            podmienkaPocetAutobusov.addTerms(VseobecneFunkcie.vytvorPoleJednotiek(premenneXij.length), premenneXij);
            model.addConstr(podmienkaPocetAutobusov, GRB.EQUAL, data.getSpoje().size() - pocetAutobusov, "7");

            model.optimize();

            System.out.println("Minimálne prázdne prejazdy s garážami: " + model.get(GRB.DoubleAttr.ObjVal) + " kilometrov");
            List<String> spoje = new ArrayList<>();
            for (GRBVar var : model.getVars()) {
                if (var.get(GRB.DoubleAttr.X) == 1) {
                    String v = var.get(GRB.StringAttr.VarName);
                    if (v.charAt(0) == 'x' && v.chars().filter(c -> c == '_').count() == 3) {
                        spoje.add(v);
                    }
                }
            }
            List<List<SpojGaraz>> turnusy = VypisGaraze.vytvorTurnusyGaraze(spoje, data.getSpoje());
            VypisGaraze.vypisTurnusyGaraze(turnusy);
            try {
                ImportExportDat.vypisTurnusyDoPdf(turnusy, data.getZastavky());
            } catch (FileNotFoundException | DocumentException ex) {
                Logger.getLogger(MinPrazdnePrejazdyGaraze.class.getName()).log(Level.SEVERE, null, ex);
            }
            model.dispose();
            env.dispose();
        } catch (GRBException ex) {
            Logger.getLogger(MinPrazdnePrejazdy.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

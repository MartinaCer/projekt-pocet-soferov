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
import gurobiModelFunkcie.GarazFunkcie;
import gurobiModelFunkcie.SoferiFunkcie;
import gurobiModelFunkcie.VseobecneFunkcie;
import gurobiModelFunkcie.VypisSoferi;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import konfiguracia.Konstanty;

/**
 *
 * @author Martina Cernekova
 */
public class MinPocetSoferov {

    public void optimalizuj(Data data, int pocetAutobusov) {
        try {
            GRBEnv env = new GRBEnv("minPocetSoferov.log");
            GRBModel model = new GRBModel(env);

            List<Spoj> zoznamSpojov = new ArrayList<>(data.getSpoje().values());
            int idGaraze = data.getGaraze().get(0);

            Map<Spoj.KlucSpoja, Map<Spoj.KlucSpoja, GRBVar>> xIJ = VseobecneFunkcie.vytvorPremenneXij(model, zoznamSpojov);
            Map<Spoj.KlucSpoja, Map<Spoj.KlucSpoja, GRBVar>> yIJ = SoferiFunkcie.vytvorPremenneYij(model, zoznamSpojov);
            Map<Spoj.KlucSpoja, GRBVar> uJ = GarazFunkcie.vytvorPremenneUjVi(model, zoznamSpojov, "u");
            Map<Spoj.KlucSpoja, GRBVar> vI = GarazFunkcie.vytvorPremenneUjVi(model, zoznamSpojov, "v");
            Map<Spoj.KlucSpoja, GRBVar> tJ = SoferiFunkcie.vytvorPremenneTj(model, zoznamSpojov);
            model.update();

            GRBVar[] premenneXij = VseobecneFunkcie.vytvorSucetXij(xIJ);
            GRBVar[] premenneYij = SoferiFunkcie.vytvorSucetYij(yIJ);
            GRBVar[] premenneUj = GarazFunkcie.vytvorSucetUjVj(uJ);
            GRBVar[] premenneVi = GarazFunkcie.vytvorSucetUjVj(vI);
            GRBLinExpr ucelovaFunkcia = new GRBLinExpr();
            ucelovaFunkcia.addConstant(data.getSpoje().size() * Konstanty.CENA_SOFERA);
            ucelovaFunkcia.addTerms(SoferiFunkcie.vytvorPoleCenySoferov(premenneXij.length), premenneXij);
            ucelovaFunkcia.addTerms(VseobecneFunkcie.vytvorPoleVzdialenosti(premenneXij, data.getSpoje(), data.getKmVzdialenosti(), Konstanty.CENA_KILOMETER), premenneXij);
            ucelovaFunkcia.addTerms(SoferiFunkcie.vytvorPoleVzdialenostiZaDoGaraze(premenneYij, data.getSpoje(), data.getKmVzdialenosti(), idGaraze, Konstanty.CENA_KILOMETER), premenneYij);
            ucelovaFunkcia.addTerms(GarazFunkcie.vytvorPoleVzdialenostiPreGaraz(premenneUj, data.getSpoje(), data.getKmVzdialenosti(), idGaraze, true, Konstanty.CENA_KILOMETER), premenneUj);
            ucelovaFunkcia.addTerms(GarazFunkcie.vytvorPoleVzdialenostiPreGaraz(premenneVi, data.getSpoje(), data.getKmVzdialenosti(), idGaraze, false, Konstanty.CENA_KILOMETER), premenneVi);
            model.setObjective(ucelovaFunkcia, GRB.MINIMIZE);

            GRBLinExpr[] podmienky1 = SoferiFunkcie.vytvorPodmienkySucetXijYijPodlaIaUj(xIJ, yIJ, uJ, zoznamSpojov);
            model.addConstrs(podmienky1, VseobecneFunkcie.vytvorPoleRovny(podmienky1.length),
                    VseobecneFunkcie.vytvorPoleJednotiek(podmienky1.length), VseobecneFunkcie.vytvorNazvyPodmienok(podmienky1.length, "1"));

            GRBLinExpr[] podmienky2 = SoferiFunkcie.vytvorPodmienkySucetXijYijPodlaJaVi(xIJ, yIJ, vI, zoznamSpojov);
            model.addConstrs(podmienky2, VseobecneFunkcie.vytvorPoleRovny(podmienky2.length),
                    VseobecneFunkcie.vytvorPoleJednotiek(podmienky2.length), VseobecneFunkcie.vytvorNazvyPodmienok(podmienky2.length, "2"));

            GRBLinExpr[] podmienky3 = SoferiFunkcie.vytvorPodmienkyTjGaraz(tJ, data.getCasVzdialenosti(), idGaraze, zoznamSpojov);
            model.addConstrs(podmienky3, VseobecneFunkcie.vytvorPoleMensiRovny(podmienky3.length),
                    VseobecneFunkcie.vytvorPoleHodnot(podmienky3.length, Konstanty.MAX_DOBA_JAZDY), VseobecneFunkcie.vytvorNazvyPodmienok(podmienky3.length, "3"));

            SoferiFunkcie.pridajPodmienkyTjPreXij(model, xIJ, tJ, data.getCasVzdialenosti(), zoznamSpojov, "4");
            SoferiFunkcie.pridajPodmienkyTjPreVi(model, vI, tJ, data.getCasVzdialenosti(), idGaraze, zoznamSpojov, "5");

            GRBLinExpr podmienkaPocetAutobusov = new GRBLinExpr();
            podmienkaPocetAutobusov.addTerms(VseobecneFunkcie.vytvorPoleJednotiek(premenneXij.length), premenneXij);
            podmienkaPocetAutobusov.addTerms(VseobecneFunkcie.vytvorPoleJednotiek(premenneYij.length), premenneYij);
            model.addConstr(podmienkaPocetAutobusov, GRB.EQUAL, data.getSpoje().size() - pocetAutobusov, "6");

//            model.computeIIS();
//            model.write("iis.ilp");
//            model.feasRelax(0, true, false, true);
//            model.tune();
            model.set(GRB.IntParam.Method, 0);
            model.set(GRB.IntParam.BranchDir, -1);
            model.set(GRB.IntParam.AggFill, 100);
            model.set(GRB.DoubleParam.TimeLimit, 100);
            model.optimize();
            System.out.println("rie??enie: " + model.get(GRB.IntAttr.SolCount));
//            System.out.println("Minim??lna cena : " + model.get(GRB.DoubleAttr.ObjVal) + " eur");
            List<String> spoje = new ArrayList<>();
            Map<KlucSpoja, Double> tHodnoty = new HashMap<>();
            int pocetX = 0;
            int pocetY = 0;
            for (GRBVar var : model.getVars()) {
                if (var.get(GRB.DoubleAttr.X) == 1) {
                    String v = var.get(GRB.StringAttr.VarName);
                    if (v.charAt(0) == 'x') {
                        spoje.add(v);
                        pocetX++;
                    }
                    if (v.charAt(0) == 'y') {
                        spoje.add(v);
                        pocetY++;
                    }
                }
                if (var.get(GRB.StringAttr.VarName).charAt(0) == 't') {
                    String[] pole = var.get(GRB.StringAttr.VarName).split("_");
                    String[] spoj = pole[1].split(";");
                    tHodnoty.put(new KlucSpoja(Integer.valueOf(spoj[0]), Integer.valueOf(spoj[1])), var.get(GRB.DoubleAttr.X));
                }
            }
            System.out.println("Po??et x: " + pocetX);
            System.out.println("Po??et y: " + pocetY);
            VypisSoferi.vypisTurnusy(VypisSoferi.vytvorTurnusy(spoje, data.getSpoje()), tHodnoty, data.getCasVzdialenosti(), idGaraze);
            model.dispose();
            env.dispose();
        } catch (GRBException ex) {
            Logger.getLogger(MinPocetSoferov.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

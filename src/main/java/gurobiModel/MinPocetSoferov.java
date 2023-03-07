package gurobiModel;

import dto.Data;
import dto.Spoj;
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

            Map<Spoj.KlucSpoja, Map<Spoj.KlucSpoja, GRBVar>> xIJ = VseobecneFunkcie.vytvorPremenneXij(model, new ArrayList<>(data.getSpoje().values()));
            Map<Spoj.KlucSpoja, Map<Spoj.KlucSpoja, GRBVar>> yIJ = SoferiFunkcie.vytvorPremenneYij(model, new ArrayList<>(data.getSpoje().values()));
            Map<Spoj.KlucSpoja, GRBVar> uJ = GarazFunkcie.vytvorPremenneUjVi(model, new ArrayList<>(data.getSpoje().values()), "u");
            Map<Spoj.KlucSpoja, GRBVar> vI = GarazFunkcie.vytvorPremenneUjVi(model, new ArrayList<>(data.getSpoje().values()), "v");
            model.update();

            GRBVar[] premenneXij = VseobecneFunkcie.vytvorSucetXij(xIJ);
            GRBVar[] premenneYij = SoferiFunkcie.vytvorSucetYij(yIJ);
            GRBVar[] premenneUj = GarazFunkcie.vytvorSucetUjVj(uJ);
            GRBVar[] premenneVi = GarazFunkcie.vytvorSucetUjVj(vI);
            GRBLinExpr ucelovaFunkcia = new GRBLinExpr();
            ucelovaFunkcia.addConstant(data.getSpoje().size() * Konstanty.CENA_SOFERA);
            ucelovaFunkcia.addTerms(SoferiFunkcie.vytvorPoleCenySoferov(premenneXij.length), premenneXij);
            ucelovaFunkcia.addTerms(VseobecneFunkcie.vytvorPoleVzdialenosti(premenneXij, data.getSpoje(), data.getKmVzdialenosti(), Konstanty.CENA_KILOMETER), premenneXij);
            ucelovaFunkcia.addTerms(SoferiFunkcie.vytvorPoleVzdialenostiZaDoGaraze(premenneYij, data.getSpoje(), data.getKmVzdialenosti(), data.getGaraze().get(0), Konstanty.CENA_KILOMETER), premenneYij);
            ucelovaFunkcia.addTerms(GarazFunkcie.vytvorPoleVzdialenostiPreGaraz(premenneUj, data.getSpoje(), data.getKmVzdialenosti(), data.getGaraze().get(0), true, Konstanty.CENA_KILOMETER), premenneUj);
            ucelovaFunkcia.addTerms(GarazFunkcie.vytvorPoleVzdialenostiPreGaraz(premenneVi, data.getSpoje(), data.getKmVzdialenosti(), data.getGaraze().get(0), false, Konstanty.CENA_KILOMETER), premenneVi);
            model.setObjective(ucelovaFunkcia, GRB.MINIMIZE);

            GRBLinExpr[] podmienky1 = SoferiFunkcie.vytvorPodmienkySucetXijYijPodlaIaUj(xIJ, yIJ, uJ, new ArrayList<>(data.getSpoje().values()));
            model.addConstrs(podmienky1, VseobecneFunkcie.vytvorPoleRovny(podmienky1.length),
                    VseobecneFunkcie.vytvorPoleJednotiek(podmienky1.length), VseobecneFunkcie.vytvorNazvyPodmienok(podmienky1.length, "1"));

            GRBLinExpr[] podmienky2 = SoferiFunkcie.vytvorPodmienkySucetXijYijPodlaJaVi(xIJ, yIJ, vI, new ArrayList<>(data.getSpoje().values()));
            model.addConstrs(podmienky2, VseobecneFunkcie.vytvorPoleRovny(podmienky2.length),
                    VseobecneFunkcie.vytvorPoleJednotiek(podmienky2.length), VseobecneFunkcie.vytvorNazvyPodmienok(podmienky2.length, "2"));

            GRBLinExpr podmienkaPocetAutobusov = new GRBLinExpr();
            podmienkaPocetAutobusov.addTerms(VseobecneFunkcie.vytvorPoleJednotiek(premenneXij.length), premenneXij);
            podmienkaPocetAutobusov.addTerms(VseobecneFunkcie.vytvorPoleJednotiek(premenneYij.length), premenneYij);
            model.addConstr(podmienkaPocetAutobusov, GRB.EQUAL, data.getSpoje().size() - pocetAutobusov, "3");

            model.optimize();

            System.out.println("Minimálna cena : " + model.get(GRB.DoubleAttr.ObjVal) + " eur");
            List<String> spoje = new ArrayList<>();
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
            }
            System.out.println("Počet x: " + pocetX);
            System.out.println("Počet y: " + pocetY);
            VypisSoferi.vypisTurnusy(VypisSoferi.vytvorTurnusy(spoje, data.getSpoje()));
            model.dispose();
            env.dispose();
        } catch (GRBException ex) {
            Logger.getLogger(MinPocetSoferov.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

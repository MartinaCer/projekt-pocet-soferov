package gurobiModel;

import com.itextpdf.text.DocumentException;
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
import gurobiModelVypisy.SmenaSofera;
import gurobiModelVypisy.SpojSofera;
import gurobiModelVypisy.VypisSoferi;
import gurobiModelVypisy.VypisSoferi.SpojSofer;
import importExport.ImportExportDat;
import java.io.FileNotFoundException;
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

            List<Spoj> zoznamSpojov = new ArrayList<>(data.getSpoje().values());
            int idGaraze = data.getGaraze().get(0);

            Map<Spoj.KlucSpoja, Map<Spoj.KlucSpoja, GRBVar>> xIJ = VseobecneFunkcie.vytvorPremenneXij(model, zoznamSpojov);
            Map<Spoj.KlucSpoja, Map<Spoj.KlucSpoja, GRBVar>> yIJ = SoferiFunkcie.vytvorPremenneYij(model, zoznamSpojov);
            Map<Spoj.KlucSpoja, GRBVar> uJ = GarazFunkcie.vytvorPremenneUjVi(model, zoznamSpojov, "u");
            Map<Spoj.KlucSpoja, GRBVar> vI = GarazFunkcie.vytvorPremenneUjVi(model, zoznamSpojov, "v");
            Map<Spoj.KlucSpoja, GRBVar> sJ = SoferiFunkcie.vytvorPremenneSjTj(model, zoznamSpojov, "s");
            Map<Spoj.KlucSpoja, GRBVar> tJ = SoferiFunkcie.vytvorPremenneSjTj(model, zoznamSpojov, "t");
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

            GRBLinExpr[] podmienky3 = SoferiFunkcie.vytvorPodmienkySjTjGaraz(sJ, data.getCasVzdialenosti(), idGaraze, zoznamSpojov);
            model.addConstrs(podmienky3, VseobecneFunkcie.vytvorPoleMensiRovny(podmienky3.length),
                    VseobecneFunkcie.vytvorPoleHodnot(podmienky3.length, Konstanty.MAX_DOBA_SMENY), VseobecneFunkcie.vytvorNazvyPodmienok(podmienky3.length, "3"));

            GRBLinExpr[] podmienky4 = SoferiFunkcie.vytvorPodmienkySjTjGaraz(tJ, data.getCasVzdialenosti(), idGaraze, zoznamSpojov);
            model.addConstrs(podmienky4, VseobecneFunkcie.vytvorPoleMensiRovny(podmienky4.length),
                    VseobecneFunkcie.vytvorPoleHodnot(podmienky4.length, Konstanty.MAX_DOBA_JAZDY), VseobecneFunkcie.vytvorNazvyPodmienok(podmienky4.length, "4"));

            SoferiFunkcie.pridajPodmienkySjTjPreXij(model, xIJ, sJ, data.getCasVzdialenosti(), zoznamSpojov, "5", true);
            SoferiFunkcie.pridajPodmienkySjTjPreXij(model, xIJ, tJ, data.getCasVzdialenosti(), zoznamSpojov, "6", false);
            SoferiFunkcie.pridajPodmienkySjTjPreYij(model, yIJ, sJ, data.getCasVzdialenosti(), idGaraze, zoznamSpojov, "7");
            SoferiFunkcie.pridajPodmienkySjTjPreYij(model, yIJ, tJ, data.getCasVzdialenosti(), idGaraze, zoznamSpojov, "8");
            SoferiFunkcie.pridajPodmienkySjTjPreUj(model, uJ, sJ, data.getCasVzdialenosti(), idGaraze, zoznamSpojov, "9");
            SoferiFunkcie.pridajPodmienkySjTjPreUj(model, uJ, tJ, data.getCasVzdialenosti(), idGaraze, zoznamSpojov, "10");
            SoferiFunkcie.pridajPodmienkyYijNieJeUj(model, uJ, yIJ, zoznamSpojov, "11");
            SoferiFunkcie.pridajPodmienkyYijNieJeVi(model, vI, yIJ, zoznamSpojov, "12");

            GRBLinExpr podmienkaPocetAutobusov = new GRBLinExpr();
            podmienkaPocetAutobusov.addTerms(VseobecneFunkcie.vytvorPoleJednotiek(premenneXij.length), premenneXij);
            podmienkaPocetAutobusov.addTerms(VseobecneFunkcie.vytvorPoleJednotiek(premenneYij.length), premenneYij);
            model.addConstr(podmienkaPocetAutobusov, GRB.EQUAL, data.getSpoje().size() - pocetAutobusov, "13");

//            model.computeIIS();
//            model.write("iis.ilp");
//            model.feasRelax(0, true, false, true);
//            model.tune();
            model.set(GRB.IntParam.Method, 0);
            model.set(GRB.IntParam.BranchDir, -1);
            model.set(GRB.IntParam.AggFill, 100);
            model.set(GRB.DoubleParam.TimeLimit, 10);
            model.optimize();
            List<String> spoje = new ArrayList<>();
            int pocetX = 0;
            int pocetY = 0;
            List<List<SpojSofer>> turnusy = null;
            List<List<SmenaSofera>> smeny = null;
            boolean porusujePrestavky = true;
            while (porusujePrestavky) {
                List<List<SpojSofera>> zlePrestavky = new ArrayList<>();
                porusujePrestavky = false;
                pocetX = 0;
                pocetY = 0;
                spoje.clear();
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
                turnusy = VypisSoferi.vytvorTurnusy(spoje, data.getSpoje());
                smeny = VypisSoferi.vytvorSmeny(turnusy, data.getCasVzdialenosti(), idGaraze);
                for (List<SmenaSofera> turnus : smeny) {
                    boolean turnusSplna = true;
                    for (SmenaSofera smena : turnus) {
                        List<SpojSofera> chybne = smena.porusujePrestavku();
                        if (!chybne.isEmpty()) {
                            turnusSplna = false;
                            zlePrestavky.add(chybne);
                        }
                    }
                    if (!turnusSplna) {
                        porusujePrestavky = true;
                    }
                }
                if (!zlePrestavky.isEmpty()) {
                    int poradiePodmienky = 1;
                    for (List<SpojSofera> smena : zlePrestavky) {
                        GRBLinExpr podmienka = new GRBLinExpr();
                        for (int i = 0; i < smena.size() - 1; i++) {
                            podmienka.addTerm(1, xIJ.get(smena.get(i).getSpoj().getKluc()).get(smena.get(i + 1).getSpoj().getKluc()));
                        }
                        model.addConstr(podmienka, GRB.LESS_EQUAL, smena.size() - 2, "14_" + poradiePodmienky);
                        poradiePodmienky++;
                    }
                    model.optimize();
                }
            }
            System.out.println("Minimálna cena : " + model.get(GRB.DoubleAttr.ObjVal) + " eur");
            System.out.println("Počet x: " + pocetX);
            System.out.println("Počet y: " + pocetY);
            VypisSoferi.vypisTurnusy(turnusy, data.getCasVzdialenosti(), idGaraze);
            try {
                ImportExportDat.vypisSmenyDoPdf(smeny);
            } catch (FileNotFoundException | DocumentException ex) {
                Logger.getLogger(MinPocetSoferov.class.getName()).log(Level.SEVERE, null, ex);
            }
            model.dispose();
            env.dispose();
        } catch (GRBException ex) {
            Logger.getLogger(MinPocetSoferov.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

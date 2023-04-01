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
import gurobiModelVypisy.SmenaSofera;
import gurobiModelVypisy.SpojSofera;
import gurobiModelVypisy.VypisSoferi;
import gurobiModelVypisy.VypisSoferi.SpojSofer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import konfiguracia.Konstanty;
import static konfiguracia.Konstanty.PRESTAVKY;
import konfiguracia.Konstanty.Prestavka;

/**
 *
 * @author Martina Cernekova
 */
public class MinPocetSoferov {

    public VysledokMinSoferi optimalizuj(Data data, int pocetAutobusov) throws GRBException {
        GRBEnv env = new GRBEnv("minPocetSoferov.log");
        GRBModel model = new GRBModel(env);

        List<Spoj> zoznamSpojov = new ArrayList<>(data.getSpoje().values());
        int idGaraze = data.getKonfiguracia().getGaraz();

        Map<Spoj.KlucSpoja, Map<Spoj.KlucSpoja, GRBVar>> xIJ = VseobecneFunkcie.vytvorPremenneXij(model, zoznamSpojov);
        Map<Spoj.KlucSpoja, Map<Spoj.KlucSpoja, GRBVar>> yIJ = SoferiFunkcie.vytvorPremenneYij(model, zoznamSpojov);
        Map<Spoj.KlucSpoja, GRBVar> uJ = GarazFunkcie.vytvorPremenneVsetkySpoje(model, zoznamSpojov, "u");
        Map<Spoj.KlucSpoja, GRBVar> vI = GarazFunkcie.vytvorPremenneVsetkySpoje(model, zoznamSpojov, "v");
        Map<Spoj.KlucSpoja, GRBVar> sJ = SoferiFunkcie.vytvorPremenneSjTj(model, zoznamSpojov, "s");
        Map<Spoj.KlucSpoja, GRBVar> tJ = SoferiFunkcie.vytvorPremenneSjTj(model, zoznamSpojov, "t");
        model.update();

        GRBVar[] premenneXij = VseobecneFunkcie.vytvorSucetNasledovnych(xIJ);
        GRBVar[] premenneYij = VseobecneFunkcie.vytvorSucetNasledovnych(yIJ);
        GRBVar[] premenneUj = GarazFunkcie.vytvorSucetVsetkySpoje(uJ);
        GRBVar[] premenneVi = GarazFunkcie.vytvorSucetVsetkySpoje(vI);
        GRBLinExpr ucelovaFunkcia = new GRBLinExpr();
        ucelovaFunkcia.addConstant(data.getSpoje().size() * data.getKonfiguracia().getCenaSofera());
        ucelovaFunkcia.addTerms(VseobecneFunkcie.vytvorPoleHodnot(premenneXij.length, -data.getKonfiguracia().getCenaSofera()), premenneXij);
        ucelovaFunkcia.addTerms(VseobecneFunkcie.vytvorPoleVzdialenosti(premenneXij, data.getSpoje(), data.getKmVzdialenosti(), data.getKonfiguracia().getCenaKilometer()), premenneXij);
        ucelovaFunkcia.addTerms(SoferiFunkcie.vytvorPoleVzdialenostiZaDoGaraze(premenneYij, data.getSpoje(), data.getKmVzdialenosti(), idGaraze, data.getKonfiguracia().getCenaKilometer()), premenneYij);
        ucelovaFunkcia.addTerms(GarazFunkcie.vytvorPoleVzdialenostiPreGaraz(premenneUj, data.getSpoje(), data.getKmVzdialenosti(), idGaraze, true, data.getKonfiguracia().getCenaKilometer()), premenneUj);
        ucelovaFunkcia.addTerms(GarazFunkcie.vytvorPoleVzdialenostiPreGaraz(premenneVi, data.getSpoje(), data.getKmVzdialenosti(), idGaraze, false, data.getKonfiguracia().getCenaKilometer()), premenneVi);
        model.setObjective(ucelovaFunkcia, GRB.MINIMIZE);

        GRBLinExpr[] podmienky1 = SoferiFunkcie.vytvorPodmienkySucetXijYijPodlaIaUj(xIJ, yIJ, uJ, zoznamSpojov);
        model.addConstrs(podmienky1, VseobecneFunkcie.vytvorPoleRovnost(podmienky1.length, GRB.EQUAL),
                VseobecneFunkcie.vytvorPoleJednotiek(podmienky1.length), VseobecneFunkcie.vytvorNazvyPodmienok(podmienky1.length, "1"));

        GRBLinExpr[] podmienky2 = SoferiFunkcie.vytvorPodmienkySucetXijYijPodlaJaVi(xIJ, yIJ, vI, zoznamSpojov);
        model.addConstrs(podmienky2, VseobecneFunkcie.vytvorPoleRovnost(podmienky2.length, GRB.EQUAL),
                VseobecneFunkcie.vytvorPoleJednotiek(podmienky2.length), VseobecneFunkcie.vytvorNazvyPodmienok(podmienky2.length, "2"));

        GRBLinExpr[] podmienky3 = SoferiFunkcie.vytvorPodmienkySjTjGaraz(sJ, data.getCasVzdialenosti(), idGaraze, zoznamSpojov);
        model.addConstrs(podmienky3, VseobecneFunkcie.vytvorPoleRovnost(podmienky3.length, GRB.LESS_EQUAL),
                VseobecneFunkcie.vytvorPoleHodnot(podmienky3.length, Konstanty.MAX_DOBA_SMENY), VseobecneFunkcie.vytvorNazvyPodmienok(podmienky3.length, "3"));

        GRBLinExpr[] podmienky4 = SoferiFunkcie.vytvorPodmienkySjTjGaraz(tJ, data.getCasVzdialenosti(), idGaraze, zoznamSpojov);
        model.addConstrs(podmienky4, VseobecneFunkcie.vytvorPoleRovnost(podmienky4.length, GRB.LESS_EQUAL),
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

        model.set(GRB.DoubleParam.Heuristics, 0.001);
        model.set(GRB.DoubleParam.TimeLimit, 100);
        model.optimize();

        List<String> spoje = new ArrayList<>();
        List<List<SpojSofer>> turnusy = null;
        List<List<SmenaSofera>> smeny = null;
        boolean porusujePrestavky = true;
        while (porusujePrestavky) {
            List<List<SpojSofera>> zlePrestavky = new ArrayList<>();
            porusujePrestavky = false;
            spoje.clear();
            for (GRBVar var : model.getVars()) {
                if (var.get(GRB.DoubleAttr.X) == 1) {
                    String v = var.get(GRB.StringAttr.VarName);
                    if (v.charAt(0) == 'x') {
                        spoje.add(v);
                    }
                    if (v.charAt(0) == 'y') {
                        spoje.add(v);
                    }
                }
            }
            turnusy = VypisSoferi.vytvorTurnusy(spoje, data.getSpoje());
            smeny = VypisSoferi.vytvorSmeny(turnusy, data.getCasVzdialenosti(), idGaraze);
            for (List<SmenaSofera> turnus : smeny) {
                boolean turnusSplna = true;
                for (SmenaSofera smena : turnus) {
                    for (Prestavka nastaveniePrestavky : PRESTAVKY) {
                        List<SpojSofera> chybne = smena.porusujePrestavku(nastaveniePrestavky);
                        if (!chybne.isEmpty()) {
                            turnusSplna = false;
                            zlePrestavky.add(chybne);
                        }
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
        VysledokMinSoferi vysledok = new VysledokMinSoferi((int) model.get(GRB.DoubleAttr.ObjVal), 
                smeny.stream().mapToInt(s -> s.size()).sum(), VypisSoferi.vytvorSmeny(turnusy, data.getCasVzdialenosti(), idGaraze));
        model.dispose();
        env.dispose();
        return vysledok;
    }

    public static class VysledokMinSoferi {

        private final int cena;
        private final int pocetSoferov;
        private final List<List<SmenaSofera>> smeny;

        public VysledokMinSoferi(int cena, int pocetSoferov, List<List<SmenaSofera>> smeny) {
            this.cena = cena;
            this.pocetSoferov = pocetSoferov;
            this.smeny = smeny;
        }

        public int getCena() {
            return cena;
        }

        public int getPocetSoferov() {
            return pocetSoferov;
        }

        public List<List<SmenaSofera>> getSmeny() {
            return smeny;
        }

    }
}

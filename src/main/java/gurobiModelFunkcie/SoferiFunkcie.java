package gurobiModelFunkcie;

import dto.Data;
import dto.Spoj;
import dto.Spoj.KlucSpoja;
import gurobi.GRB;
import gurobi.GRBException;
import gurobi.GRBLinExpr;
import gurobi.GRBModel;
import gurobi.GRBVar;
import gurobiModelVypisy.SmenaSofera;
import gurobiModelVypisy.SpojSofera;
import gurobiModelVypisy.VypisSoferi;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import konfiguracia.Konstanty;
import static konfiguracia.Konstanty.PRESTAVKA_V_DOBE_JAZDY;
import static konfiguracia.Konstanty.PRESTAVKY;

/**
 *
 * @author Martina Cernekova
 */
public class SoferiFunkcie {

    private static final int K = (int) (Konstanty.MAX_DOBA_JAZDY > Konstanty.MAX_DOBA_SMENY ? Konstanty.MAX_DOBA_JAZDY : Konstanty.MAX_DOBA_SMENY * 1.2);

    private SoferiFunkcie() {
    }

    public static double[] vytvorPoleVzdialenostiZaDoGaraze(GRBVar[] premenneYij, Map<KlucSpoja, Spoj> spoje,
            Map<Integer, Map<Integer, Integer>> vzdialenosti, int idGaraze, int cena) throws GRBException {
        double[] pole = new double[premenneYij.length];
        for (int i = 0; i < premenneYij.length; i++) {
            String premenna = premenneYij[i].get(GRB.StringAttr.VarName);
            String[] data = premenna.split("_");
            String[] sIspoj = data[1].split(";");
            String[] sJspoj = data[2].split(";");
            Spoj iSpoj = spoje.get(new Spoj.KlucSpoja(Integer.valueOf(sIspoj[0]), Integer.valueOf(sIspoj[1])));
            Spoj jSpoj = spoje.get(new Spoj.KlucSpoja(Integer.valueOf(sJspoj[0]), Integer.valueOf(sJspoj[1])));
            pole[i] = (vzdialenosti.get(iSpoj.getMiestoPrichodu().getId()).get(idGaraze)
                    + vzdialenosti.get(idGaraze).get(jSpoj.getMiestoOdchodu().getId())) * cena;
        }
        return pole;
    }

    public static Map<KlucSpoja, Map<KlucSpoja, GRBVar>> vytvorPremenneYij(GRBModel model, List<Spoj> spoje) throws GRBException {
        Map<KlucSpoja, Map<KlucSpoja, GRBVar>> premenne = new HashMap<>();
        for (Spoj iSpoj : spoje) {
            KlucSpoja iKluc = iSpoj.getKluc();
            Map<KlucSpoja, GRBVar> iMapa = premenne.computeIfAbsent(iKluc, k -> new HashMap<>());
            for (Spoj jSpoj : iSpoj.getMozneNasledovneZmenySofera()) {
                KlucSpoja jKluc = jSpoj.getKluc();
                iMapa.put(jKluc, model.addVar(0, 1, 0, GRB.BINARY, "y_" + iKluc.toString() + "_" + jKluc.toString()));
            }
        }
        return premenne;
    }

    public static Map<KlucSpoja, GRBVar> vytvorPremenneSjTj(GRBModel model, List<Spoj> spoje, String meno) throws GRBException {
        Map<KlucSpoja, GRBVar> premenne = new HashMap<>();
        for (Spoj spoj : spoje) {
            premenne.put(spoj.getKluc(), model.addVar(0, meno.equals("s") ? Konstanty.MAX_DOBA_SMENY : Konstanty.MAX_DOBA_JAZDY, 0, GRB.CONTINUOUS, meno + "_" + spoj.getKluc().toString()));
        }
        return premenne;
    }

    public static GRBLinExpr[] vytvorPodmienkySucetXijYijPodlaJaVi(Map<KlucSpoja, Map<KlucSpoja, GRBVar>> premenneXij,
            Map<KlucSpoja, Map<KlucSpoja, GRBVar>> premenneYij, Map<KlucSpoja, GRBVar> premenneVi, List<Spoj> spoje) {
        List<GRBLinExpr> podmienky = new ArrayList<>();
        for (Spoj iSpoj : spoje) {
            GRBLinExpr podmienka = new GRBLinExpr();
            iSpoj.getMozneNasledovneSpojenia().forEach(jSpoj -> podmienka.addTerm(1, premenneXij.get(iSpoj.getKluc()).get(jSpoj.getKluc())));
            iSpoj.getMozneNasledovneZmenySofera().forEach(jSpoj -> podmienka.addTerm(1, premenneYij.get(iSpoj.getKluc()).get(jSpoj.getKluc())));
            podmienka.addTerm(1, premenneVi.get(iSpoj.getKluc()));
            podmienky.add(podmienka);
        }
        return VseobecneFunkcie.vytvorPolePodmienok(podmienky);
    }

    public static GRBLinExpr[] vytvorPodmienkySucetXijYijPodlaIaUj(Map<KlucSpoja, Map<KlucSpoja, GRBVar>> premenneXij,
            Map<KlucSpoja, Map<KlucSpoja, GRBVar>> premenneYij, Map<KlucSpoja, GRBVar> premenneUj, List<Spoj> spoje) {
        List<GRBLinExpr> podmienky = new ArrayList<>();
        for (Spoj jSpoj : spoje) {
            GRBLinExpr podmienka = new GRBLinExpr();
            jSpoj.getMoznePredosleSpojenia().forEach(iSpoj -> podmienka.addTerm(1, premenneXij.get(iSpoj.getKluc()).get(jSpoj.getKluc())));
            jSpoj.getMoznePredosleZmenySofera().forEach(iSpoj -> podmienka.addTerm(1, premenneYij.get(iSpoj.getKluc()).get(jSpoj.getKluc())));
            podmienka.addTerm(1, premenneUj.get(jSpoj.getKluc()));
            podmienky.add(podmienka);
        }
        return VseobecneFunkcie.vytvorPolePodmienok(podmienky);
    }

    public static GRBLinExpr[] vytvorPodmienkySjTjGaraz(Map<KlucSpoja, GRBVar> premenne,
            Map<Integer, Map<Integer, Integer>> vzdialenosti, int idGaraze, List<Spoj> spoje) throws GRBException {
        List<GRBLinExpr> podmienky = new ArrayList<>();
        for (Spoj spoj : spoje) {
            GRBLinExpr podmienka = new GRBLinExpr();
            podmienka.addTerm(1, premenne.get(spoj.getKluc()));
            podmienka.addConstant(vzdialenosti.get(spoj.getMiestoPrichodu().getId()).get(idGaraze));
            podmienky.add(podmienka);
        }
        return VseobecneFunkcie.vytvorPolePodmienok(podmienky);
    }

    public static void pridajPodmienkySjTjPreXij(GRBModel model, Map<KlucSpoja, Map<KlucSpoja, GRBVar>> premenneXij,
            Map<KlucSpoja, GRBVar> premenne, Map<Integer, Map<Integer, Integer>> vzdialenosti,
            List<Spoj> spoje, String cisloPodmienky, boolean sJ) throws GRBException {
        int poradiePodmienky = 1;
        for (Spoj jSpoj : spoje) {
            int casSpojaJ = jSpoj.getCasPrichodu().toSecondOfDay() - jSpoj.getCasOdchodu().toSecondOfDay();
            for (Spoj iSpoj : jSpoj.getMoznePredosleSpojenia()) {
                GRBVar xIJ = premenneXij.get(iSpoj.getKluc()).get(jSpoj.getKluc());
                GRBLinExpr podmienka = new GRBLinExpr();
                podmienka.addTerm(1, premenne.get(iSpoj.getKluc()));
                if (sJ) {
                    podmienka.addTerm(jSpoj.getCasOdchodu().toSecondOfDay() - iSpoj.getCasPrichodu().toSecondOfDay(), xIJ);
                } else {
                    int vzdialenost = vzdialenosti.get(iSpoj.getMiestoPrichodu().getId()).get(jSpoj.getMiestoOdchodu().getId());
                    int prestavka = jSpoj.getCasOdchodu().toSecondOfDay() - iSpoj.getCasOdchodu().toSecondOfDay() - vzdialenost;
                    podmienka.addTerm(prestavka <= PRESTAVKA_V_DOBE_JAZDY ? vzdialenost + prestavka : vzdialenost, xIJ);
                }
                podmienka.addTerm(K, xIJ);
                podmienka.addConstant(casSpojaJ - K);
                model.addConstr(podmienka, GRB.LESS_EQUAL, premenne.get(jSpoj.getKluc()), cisloPodmienky + "_" + poradiePodmienky);
                poradiePodmienky++;
            }
        }
    }

    public static void pridajPodmienkySjTjPreYij(GRBModel model, Map<KlucSpoja, Map<KlucSpoja, GRBVar>> premenneYij,
            Map<KlucSpoja, GRBVar> premenne, Map<Integer, Map<Integer, Integer>> vzdialenosti,
            int idGaraze, List<Spoj> spoje, String cisloPodmienky) throws GRBException {
        int poradiePodmienky = 1;
        Map<Integer, Integer> vzdialenostiGaraz = vzdialenosti.get(idGaraze);
        for (Spoj jSpoj : spoje) {
            for (Spoj iSpoj : jSpoj.getMoznePredosleZmenySofera()) {
                GRBVar yIJ = premenneYij.get(iSpoj.getKluc()).get(jSpoj.getKluc());
                GRBLinExpr podmienka = new GRBLinExpr();
                podmienka.addTerm(K, yIJ);
                podmienka.addConstant(vzdialenostiGaraz.get(jSpoj.getMiestoOdchodu().getId()) + jSpoj.getCasPrichodu().toSecondOfDay() - jSpoj.getCasOdchodu().toSecondOfDay() - K);
                model.addConstr(podmienka, GRB.LESS_EQUAL, premenne.get(jSpoj.getKluc()), cisloPodmienky + "_" + poradiePodmienky);
                poradiePodmienky++;
            }
        }
    }

    public static void pridajPodmienkySjTjPreUj(GRBModel model, Map<KlucSpoja, GRBVar> premenneUj,
            Map<KlucSpoja, GRBVar> premenne, Map<Integer, Map<Integer, Integer>> vzdialenosti,
            int idGaraze, List<Spoj> spoje, String cisloPodmienky) throws GRBException {
        int poradiePodmienky = 1;
        Map<Integer, Integer> vzdialenostiGaraz = vzdialenosti.get(idGaraze);
        for (Spoj spoj : spoje) {
            GRBVar uJ = premenneUj.get(spoj.getKluc());
            GRBLinExpr podmienka = new GRBLinExpr();
            podmienka.addTerm(K, uJ);
            podmienka.addConstant(vzdialenostiGaraz.get(spoj.getMiestoOdchodu().getId()) + spoj.getCasPrichodu().toSecondOfDay() - spoj.getCasOdchodu().toSecondOfDay() - K);
            model.addConstr(podmienka, GRB.LESS_EQUAL, premenne.get(spoj.getKluc()), cisloPodmienky + "_" + poradiePodmienky);
            poradiePodmienky++;
        }
    }

    public static void pridajPodmienkyYijNieJeUj(GRBModel model, Map<KlucSpoja, GRBVar> premenneUj,
            Map<KlucSpoja, Map<KlucSpoja, GRBVar>> premenneYij, List<Spoj> spoje, String cisloPodmienky) throws GRBException {
        int poradiePodmienky = 1;
        for (Spoj spoj : spoje) {
            GRBLinExpr podmienka = new GRBLinExpr();
            podmienka.addConstant(Integer.MAX_VALUE);
            podmienka.addTerm(-Integer.MAX_VALUE, premenneUj.get(spoj.getKluc()));
            GRBLinExpr sucet = new GRBLinExpr();
            premenneYij.get(spoj.getKluc()).values().forEach(spoj2 -> sucet.addTerm(1, spoj2));
            model.addConstr(podmienka, GRB.GREATER_EQUAL, sucet, cisloPodmienky + "_" + poradiePodmienky);
            poradiePodmienky++;
        }
    }

    public static void pridajPodmienkyYijNieJeVi(GRBModel model, Map<KlucSpoja, GRBVar> premenneVi,
            Map<KlucSpoja, Map<KlucSpoja, GRBVar>> premenneYij, List<Spoj> spoje, String cisloPodmienky) throws GRBException {
        int poradiePodmienky = 1;
        for (Spoj spoj : spoje) {
            GRBLinExpr podmienka = new GRBLinExpr();
            podmienka.addConstant(Integer.MAX_VALUE);
            podmienka.addTerm(-Integer.MAX_VALUE, premenneVi.get(spoj.getKluc()));
            GRBLinExpr sucet = new GRBLinExpr();
            for (Map.Entry<KlucSpoja, Map<KlucSpoja, GRBVar>> yIJ : premenneYij.entrySet()) {
                GRBVar premenna = yIJ.getValue().get(spoj.getKluc());
                if (premenna != null) {
                    sucet.addTerm(1, premenna);
                }
            }
            model.addConstr(podmienka, GRB.GREATER_EQUAL, sucet, cisloPodmienky + "_" + poradiePodmienky);
            poradiePodmienky++;
        }
    }

    public static List<List<SmenaSofera>> skontrolujPrestavky(GRBModel model, Map<KlucSpoja, Map<KlucSpoja, GRBVar>> premenneXij, Data data, int idGaraze) throws GRBException {
        List<String> spoje = new ArrayList<>();
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
            smeny = VypisSoferi.vytvorSmeny(VypisSoferi.vytvorTurnusy(spoje, data.getSpoje()), data.getCasVzdialenosti(), idGaraze);
            for (List<SmenaSofera> turnus : smeny) {
                boolean turnusSplna = true;
                for (SmenaSofera smena : turnus) {
                    for (Konstanty.Prestavka nastaveniePrestavky : PRESTAVKY) {
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
                        podmienka.addTerm(1, premenneXij.get(smena.get(i).getSpoj().getKluc()).get(smena.get(i + 1).getSpoj().getKluc()));
                    }
                    model.addConstr(podmienka, GRB.LESS_EQUAL, smena.size() - 2, "14_" + poradiePodmienky);
                    poradiePodmienky++;
                }
                model.optimize();
            }
        }
        return smeny;
    }
}

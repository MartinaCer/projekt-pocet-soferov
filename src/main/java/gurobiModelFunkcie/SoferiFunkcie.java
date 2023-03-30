package gurobiModelFunkcie;

import dto.Spoj;
import gurobi.GRB;
import gurobi.GRBException;
import gurobi.GRBLinExpr;
import gurobi.GRBModel;
import gurobi.GRBVar;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import konfiguracia.Konstanty;
import static konfiguracia.Konstanty.PRESTAVKA_V_DOBE_JAZDY;

/**
 *
 * @author Martina Cernekova
 */
public class SoferiFunkcie {

    private static final int K = (int) (Konstanty.MAX_DOBA_JAZDY > Konstanty.MAX_DOBA_SMENY ? Konstanty.MAX_DOBA_JAZDY : Konstanty.MAX_DOBA_SMENY * 1.2);

    private SoferiFunkcie() {
    }

    public static double[] vytvorPoleVzdialenostiZaDoGaraze(GRBVar[] premenneYij, Map<Spoj.KlucSpoja, Spoj> spoje,
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

    public static Map<Spoj.KlucSpoja, Map<Spoj.KlucSpoja, GRBVar>> vytvorPremenneYij(GRBModel model, List<Spoj> spoje) throws GRBException {
        Map<Spoj.KlucSpoja, Map<Spoj.KlucSpoja, GRBVar>> premenne = new HashMap<>();
        for (Spoj iSpoj : spoje) {
            Spoj.KlucSpoja iKluc = iSpoj.getKluc();
            Map<Spoj.KlucSpoja, GRBVar> iMapa = premenne.computeIfAbsent(iKluc, k -> new HashMap<>());
            for (Spoj jSpoj : iSpoj.getMozneNasledovneZmenySofera()) {
                Spoj.KlucSpoja jKluc = jSpoj.getKluc();
                iMapa.put(jKluc, model.addVar(0, 1, 0, GRB.BINARY, "y_" + iKluc.toString() + "_" + jKluc.toString()));
            }
        }
        return premenne;
    }

    public static Map<Spoj.KlucSpoja, GRBVar> vytvorPremenneSjTj(GRBModel model, List<Spoj> spoje, String meno) throws GRBException {
        Map<Spoj.KlucSpoja, GRBVar> premenne = new HashMap<>();
        for (Spoj spoj : spoje) {
            premenne.put(spoj.getKluc(), model.addVar(0, meno.equals("s") ? Konstanty.MAX_DOBA_SMENY : Konstanty.MAX_DOBA_JAZDY, 0, GRB.CONTINUOUS, meno + "_" + spoj.getKluc().toString()));
        }
        return premenne;
    }

    public static GRBLinExpr[] vytvorPodmienkySucetXijYijPodlaJaVi(Map<Spoj.KlucSpoja, Map<Spoj.KlucSpoja, GRBVar>> premenneXij,
            Map<Spoj.KlucSpoja, Map<Spoj.KlucSpoja, GRBVar>> premenneYij, Map<Spoj.KlucSpoja, GRBVar> premenneVi, List<Spoj> spoje) {
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

    public static GRBLinExpr[] vytvorPodmienkySucetXijYijPodlaIaUj(Map<Spoj.KlucSpoja, Map<Spoj.KlucSpoja, GRBVar>> premenneXij,
            Map<Spoj.KlucSpoja, Map<Spoj.KlucSpoja, GRBVar>> premenneYij, Map<Spoj.KlucSpoja, GRBVar> premenneUj, List<Spoj> spoje) {
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

    public static GRBLinExpr[] vytvorPodmienkySjTjGaraz(Map<Spoj.KlucSpoja, GRBVar> premenne,
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

    public static void pridajPodmienkySjTjPreXij(GRBModel model, Map<Spoj.KlucSpoja, Map<Spoj.KlucSpoja, GRBVar>> premenneXij,
            Map<Spoj.KlucSpoja, GRBVar> premenne, Map<Integer, Map<Integer, Integer>> vzdialenosti,
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

    public static void pridajPodmienkySjTjPreYij(GRBModel model, Map<Spoj.KlucSpoja, Map<Spoj.KlucSpoja, GRBVar>> premenneYij,
            Map<Spoj.KlucSpoja, GRBVar> premenne, Map<Integer, Map<Integer, Integer>> vzdialenosti,
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

    public static void pridajPodmienkySjTjPreUj(GRBModel model, Map<Spoj.KlucSpoja, GRBVar> premenneUj,
            Map<Spoj.KlucSpoja, GRBVar> premenne, Map<Integer, Map<Integer, Integer>> vzdialenosti,
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

    public static void pridajPodmienkyYijNieJeUj(GRBModel model, Map<Spoj.KlucSpoja, GRBVar> premenneUj,
            Map<Spoj.KlucSpoja, Map<Spoj.KlucSpoja, GRBVar>> premenneYij, List<Spoj> spoje, String cisloPodmienky) throws GRBException {
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

    public static void pridajPodmienkyYijNieJeVi(GRBModel model, Map<Spoj.KlucSpoja, GRBVar> premenneVi,
            Map<Spoj.KlucSpoja, Map<Spoj.KlucSpoja, GRBVar>> premenneYij, List<Spoj> spoje, String cisloPodmienky) throws GRBException {
        int poradiePodmienky = 1;
        for (Spoj spoj : spoje) {
            GRBLinExpr podmienka = new GRBLinExpr();
            podmienka.addConstant(Integer.MAX_VALUE);
            podmienka.addTerm(-Integer.MAX_VALUE, premenneVi.get(spoj.getKluc()));
            GRBLinExpr sucet = new GRBLinExpr();
            for (Map.Entry<Spoj.KlucSpoja, Map<Spoj.KlucSpoja, GRBVar>> yIJ : premenneYij.entrySet()) {
                GRBVar premenna = yIJ.getValue().get(spoj.getKluc());
                if (premenna != null) {
                    sucet.addTerm(1, premenna);
                }
            }
            model.addConstr(podmienka, GRB.GREATER_EQUAL, sucet, cisloPodmienky + "_" + poradiePodmienky);
            poradiePodmienky++;
        }
    }

}

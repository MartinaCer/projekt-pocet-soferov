
import algoritmus.Priority;
import dto.Data;
import dto.Spoj;
import dto.Zastavka;
import gurobi.GRBException;
import gurobiModel.MinNeobsluzeneSpoje;
import gurobiModel.MinPocetAutobusov;
import gurobiModel.MinPocetSoferov;
import gurobiModel.MinPrazdnePrejazdy;
import gurobiModel.MinPrazdnePrejazdyGaraz;
import importExport.ImportExportDat;
import java.io.IOException;
import java.util.Map;
import konfiguracia.Konfiguracia;
import konfiguracia.Konstanty;

/**
 *
 * @author Martina Cernekova
 */
public class MainTrieda {

    public static void main(String[] args) throws IOException, GRBException {
        Map<Integer, Zastavka> zastavky = ImportExportDat.nacitajZastavky();
        Konfiguracia konfiguracia = new Konfiguracia();
        Data data = new Data(zastavky, ImportExportDat.nacitajUseky(zastavky), ImportExportDat.nacitajSpoje(zastavky), konfiguracia);
        Priority.nastavPriority(data.getSpoje(), Priority.Strategia.PRVY_POSLEDNY_KAZDY_DRUHY, null);
//        MinPocetAutobusov model = new MinPocetAutobusov();
//        model.optimalizuj(data);
//        MinPrazdnePrejazdy model = new MinPrazdnePrejazdy();
//        MinPrazdnePrejazdyGaraz model = new MinPrazdnePrejazdyGaraz();
//        MinPocetSoferov model = new MinPocetSoferov();
//        //model.optimalizuj(data, 106);
//        model.optimalizuj(data, 10);
        MinNeobsluzeneSpoje model = new MinNeobsluzeneSpoje();
        model.optimalizuj(data, 6, 8);
    }
}

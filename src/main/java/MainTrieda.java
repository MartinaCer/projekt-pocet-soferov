
import dto.Data;
import dto.Spoj;
import dto.Zastavka;
import gurobiModel.MinPocetAutobusov;
import gurobiModel.MinPocetSoferov;
import gurobiModel.MinPrazdnePrejazdy;
import gurobiModel.MinPrazdnePrejazdyGaraz;
import gurobiModel.MinPrazdnePrejazdyGaraze;
import importExport.ImportExportDat;
import java.io.IOException;
import java.util.Map;
import konfiguracia.Konstanty;

/**
 *
 * @author Martina Cernekova
 */
public class MainTrieda {

    public static void main(String[] args) throws IOException {
        Map<Integer, Zastavka> zastavky = ImportExportDat.nacitajZastavky();
        Data data = new Data(zastavky, ImportExportDat.nacitajUseky(zastavky), ImportExportDat.nacitajSpoje(zastavky), Konstanty.GARAZE);
//        MinPocetAutobusov model = new MinPocetAutobusov();
//        model.optimalizuj(data);
        //MinPrazdnePrejazdy model = new MinPrazdnePrejazdy();
        //MinPrazdnePrejazdyGaraz model = new MinPrazdnePrejazdyGaraz();
        //MinPrazdnePrejazdyGaraze model = new MinPrazdnePrejazdyGaraze();
        MinPocetSoferov model = new MinPocetSoferov();
//        model.optimalizuj(data, 106);
        model.optimalizuj(data, 10);
    }
}

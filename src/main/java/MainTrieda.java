
import dto.Data;
import dto.Zastavka;
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
        System.out.println("");
    }
}

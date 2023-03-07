package importExport;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import dto.Spoj;
import dto.Spoj.KlucSpoja;
import dto.Usek;
import dto.Zastavka;
import gurobiModelFunkcie.VypisGaraze.SpojGaraz;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Martina Cernekova
 */
public final class ImportExportDat {

    private static final String ZASTAVKY = "/zastavky.csv";
    private static final String USEKY = "/useky.csv";
    private static final String SPOJE = "/spoje.csv";
    private static final String TURNUSY = "turnusy.pdf";

    private ImportExportDat() {
    }

    public static Map<Integer, Zastavka> nacitajZastavky() throws IOException {
        Map<Integer, Zastavka> zastavky = new HashMap<>();
        InputStream is = ImportExportDat.class.getResourceAsStream(ZASTAVKY);
        BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        String riadok;
        while ((riadok = br.readLine()) != null) {
            String[] zastavka = riadok.split(";");
            int idZastavky = Integer.valueOf(zastavka[0]);
            String nazovZastavky = zastavka[1];
            zastavky.put(idZastavky, new Zastavka(idZastavky, nazovZastavky));
        }
        br.close();
        is.close();
        return zastavky;
    }

    public static List<Usek> nacitajUseky(Map<Integer, Zastavka> zastavky) throws IOException {
        List<Usek> useky = new ArrayList<>();
        InputStream is = ImportExportDat.class.getResourceAsStream(USEKY);
        BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        String riadok;
        while ((riadok = br.readLine()) != null) {
            String[] usek = riadok.split(";");
            int idZaciatku = Integer.valueOf(usek[0]);
            int idKonca = Integer.valueOf(usek[2]);
            int sekundy = Integer.valueOf(usek[4]);
            double kilometre = Double.valueOf(usek[5].replace(",", "."));
            useky.add(new Usek(zastavky.get(idZaciatku), zastavky.get(idKonca), sekundy, kilometre));
        }
        br.close();
        is.close();
        return useky;
    }

    public static Map<KlucSpoja, Spoj> nacitajSpoje(Map<Integer, Zastavka> zastavky) throws IOException {
        Map<KlucSpoja, Spoj> spoje = new HashMap<>();
        InputStream is = ImportExportDat.class.getResourceAsStream(SPOJE);
        BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        String riadok;
        while ((riadok = br.readLine()) != null) {
            String[] spoj = riadok.split(";");
            int idLinky = Integer.valueOf(spoj[0]);
            int idSpoja = Integer.valueOf(spoj[1]);
            int idZaciatku = Integer.valueOf(spoj[2]);
            LocalTime odchod = LocalTime.parse(spoj[4].length() == 8 ? spoj[4] : "0".concat(spoj[4]));
            int idKonca = Integer.valueOf(spoj[5]);
            LocalTime prichod = LocalTime.parse(spoj[7].length() == 8 ? spoj[7] : "0".concat(spoj[7]));
            int kilometre = Integer.valueOf(spoj[8]);
            Spoj vytvorenySpoj = new Spoj(idSpoja, idLinky, zastavky.get(idZaciatku), zastavky.get(idKonca), odchod, prichod, kilometre);
            spoje.put(vytvorenySpoj.getKluc(), vytvorenySpoj);
        }
        br.close();
        is.close();
        return spoje;
    }

    public static void vypisTurnusyDoPdf(List<List<SpojGaraz>> turnusy, Map<Integer, Zastavka> zastavky) throws FileNotFoundException, DocumentException {
        Document document = new Document(PageSize.A4.rotate());
        PdfWriter.getInstance(document, new FileOutputStream(new File(TURNUSY)));
        document.open();
        document.addTitle("Turnusy");
        int poradieTurnusu = 1;
        for (List<SpojGaraz> turnus : turnusy) {
            document.add(new Paragraph("Turnus " + poradieTurnusu + " - z garáže " + zastavky.get(turnus.get(0).getGaraz()).getNazov()));
            PdfPTable tabulka = new PdfPTable(6);
            tabulka.setWidthPercentage(100);
            tabulka.setSpacingBefore(5f);
            tabulka.setSpacingAfter(5f);
            PdfPCell hlavicka = new PdfPCell(new Phrase("Spoj"));
            hlavicka.setHorizontalAlignment(Element.ALIGN_CENTER);
            tabulka.addCell(hlavicka);
            hlavicka = new PdfPCell(new Phrase("Linka"));
            hlavicka.setHorizontalAlignment(Element.ALIGN_CENTER);
            tabulka.addCell(hlavicka);
            hlavicka = new PdfPCell(new Phrase("Miesto odchodu"));
            hlavicka.setHorizontalAlignment(Element.ALIGN_CENTER);
            tabulka.addCell(hlavicka);
            hlavicka = new PdfPCell(new Phrase("Odchod"));
            hlavicka.setHorizontalAlignment(Element.ALIGN_CENTER);
            tabulka.addCell(hlavicka);
            hlavicka = new PdfPCell(new Phrase("Miesto príchodu"));
            hlavicka.setHorizontalAlignment(Element.ALIGN_CENTER);
            tabulka.addCell(hlavicka);
            hlavicka = new PdfPCell(new Phrase("Príchod"));
            hlavicka.setHorizontalAlignment(Element.ALIGN_CENTER);
            tabulka.addCell(hlavicka);
            tabulka.setHeaderRows(1);
            for (SpojGaraz spojGaraz : turnus) {
                Spoj spoj = spojGaraz.getSpoj();
                tabulka.addCell(String.valueOf(spoj.getKluc().getId()));
                tabulka.addCell(String.valueOf(spoj.getKluc().getLinka()));
                Zastavka odchod = spoj.getMiestoOdchodu();
                tabulka.addCell(String.valueOf(odchod.getId()) + " - " + odchod.getNazov());
                tabulka.addCell(spoj.getCasOdchodu().toString());
                Zastavka prichod = spoj.getMiestoPrichodu();
                tabulka.addCell(String.valueOf(prichod.getId()) + " - " + prichod.getNazov());
                tabulka.addCell(spoj.getCasPrichodu().toString());
            }
            document.add(tabulka);
            poradieTurnusu++;
        }
        document.close();
    }
}

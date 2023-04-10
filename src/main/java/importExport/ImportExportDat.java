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
import dataObjekty.Spoj;
import dataObjekty.Spoj.KlucSpoja;
import dataObjekty.Usek;
import dataObjekty.Zastavka;
import gurobiModelVypisy.SmenaSofera;
import gurobiModelVypisy.SpojSofera;
import gurobiModelVypisy.SpojeLinky;
import gurobiModelVypisy.SpojeLinky.SpojLinky;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Martina Cernekova
 */
public final class ImportExportDat {

    private static final String ZASTAVKY_DEMO = "/zastavky.csv";
    private static final String USEKY_DEMO = "/useky.csv";
    private static final String SPOJE_DEMO = "/spoje.csv";
    private static final String PRIORITY_DEMO = "/priority.csv";

    private ImportExportDat() {
    }

    public static Map<Integer, Zastavka> nacitajZastavky(File subor) throws IOException {
        Map<Integer, Zastavka> zastavky = new HashMap<>();
        InputStream is = subor == null
                ? ImportExportDat.class.getResourceAsStream(ZASTAVKY_DEMO)
                : new FileInputStream(subor);
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

    public static List<Usek> nacitajUseky(File subor, Map<Integer, Zastavka> zastavky) throws IOException {
        List<Usek> useky = new ArrayList<>();
        InputStream is = subor == null
                ? ImportExportDat.class.getResourceAsStream(USEKY_DEMO)
                : new FileInputStream(subor);
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

    public static Map<KlucSpoja, Spoj> nacitajSpoje(File subor, Map<Integer, Zastavka> zastavky) throws IOException {
        Map<KlucSpoja, Spoj> spoje = new HashMap<>();
        InputStream is = subor == null
                ? ImportExportDat.class.getResourceAsStream(SPOJE_DEMO)
                : new FileInputStream(subor);
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
            vytvorenySpoj.setPriorita(1);
            spoje.put(vytvorenySpoj.getKluc(), vytvorenySpoj);
        }
        br.close();
        is.close();
        return spoje;
    }

    public static PriorityImport naciatajPriority(File subor) throws IOException {
        Map<KlucSpoja, Integer> priority = new HashMap<>();
        List<KlucSpoja> musiObsluzit = new ArrayList<>();
        InputStream is = subor == null
                ? ImportExportDat.class.getResourceAsStream(PRIORITY_DEMO)
                : new FileInputStream(subor);
        BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        String riadok;
        while ((riadok = br.readLine()) != null) {
            String[] spoj = riadok.split(";");
            int idLinky = Integer.valueOf(spoj[0]);
            int idSpoja = Integer.valueOf(spoj[1]);
            KlucSpoja kluc = new KlucSpoja(idSpoja, idLinky);
            if (!spoj[2].isEmpty()) {
                int priorita = Integer.valueOf(spoj[2]);
                priority.put(kluc, priorita);
            }
            if (spoj.length == 4) {
                int musi = Integer.valueOf(spoj[3]);
                if (musi == 1) {
                    musiObsluzit.add(kluc);
                }
            }
        }
        br.close();
        is.close();
        return new PriorityImport(priority, musiObsluzit);
    }

    public static void vypisTurnusyDoPdf(List<List<Spoj>> turnusy, String nazov) throws FileNotFoundException, DocumentException {
        Document document = new Document(PageSize.A4.rotate());
        PdfWriter.getInstance(document, new FileOutputStream(new File(nazov + ".pdf")));
        document.open();
        document.addTitle("Turnusy");
        int poradieTurnusu = 1;
        for (List<Spoj> turnus : turnusy) {
            document.add(new Paragraph("Turnus " + poradieTurnusu));
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
            for (Spoj spoj : turnus) {
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

    public static void vypisSmenyDoPdf(List<List<SmenaSofera>> turnusy, String nazov) throws FileNotFoundException, DocumentException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        Document document = new Document(PageSize.A4.rotate());
        PdfWriter.getInstance(document, new FileOutputStream(new File(nazov + ".pdf")));
        document.open();
        document.addTitle("Smeny");
        int poradieTurnusu = 1;
        for (List<SmenaSofera> turnus : turnusy) {
            document.add(new Paragraph("Turnus " + poradieTurnusu + " - celkové trvanie "
                    + LocalTime.ofSecondOfDay(turnus.get(turnus.size() - 1).koniecSmeny() - turnus.get(0).zaciatokSmeny()).format(formatter)));
            int poradieSmeny = 1;
            for (SmenaSofera smena : turnus) {
                document.add(new Paragraph("Smena " + poradieSmeny + " - trvanie smeny " + LocalTime.ofSecondOfDay(smena.trvanieSmeny()).format(formatter)
                        + " - trvanie jazdy " + LocalTime.ofSecondOfDay(smena.trvanieJazdy()).format(formatter)));
                PdfPTable tabulka = new PdfPTable(7);
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
                hlavicka = new PdfPCell(new Phrase("Prestávka"));
                hlavicka.setHorizontalAlignment(Element.ALIGN_CENTER);
                tabulka.addCell(hlavicka);
                tabulka.setHeaderRows(1);
                Spoj odchod = smena.getSpoje().get(0).getSpoj();
                Spoj prichod = smena.getSpoje().get(smena.getSpoje().size() - 1).getSpoj();
                tabulka.addCell("");
                tabulka.addCell("");
                tabulka.addCell("Garáž");
                tabulka.addCell(odchod.getCasOdchodu().minusSeconds(smena.getCestaZgaraze()).format(formatter));
                Zastavka odchodZ = odchod.getMiestoOdchodu();
                tabulka.addCell(String.valueOf(odchodZ.getId()) + " - " + odchodZ.getNazov());
                tabulka.addCell(odchod.getCasOdchodu().format(formatter));
                tabulka.addCell("");
                for (SpojSofera spojSofera : smena.getSpoje()) {
                    Spoj spoj = spojSofera.getSpoj();
                    tabulka.addCell(String.valueOf(spoj.getKluc().getId()));
                    tabulka.addCell(String.valueOf(spoj.getKluc().getLinka()));
                    Zastavka odch = spoj.getMiestoOdchodu();
                    tabulka.addCell(String.valueOf(odch.getId()) + " - " + odch.getNazov());
                    tabulka.addCell(spoj.getCasOdchodu().format(formatter));
                    Zastavka prich = spoj.getMiestoPrichodu();
                    tabulka.addCell(String.valueOf(prich.getId()) + " - " + prich.getNazov());
                    tabulka.addCell(spoj.getCasPrichodu().format(formatter));
                    tabulka.addCell(LocalTime.ofSecondOfDay(spojSofera.getPrestavkaPoSpoji()).format(formatter));
                }
                tabulka.addCell("");
                tabulka.addCell("");
                Zastavka prichodZ = prichod.getMiestoPrichodu();
                tabulka.addCell(String.valueOf(prichodZ.getId()) + " - " + prichodZ.getNazov());
                tabulka.addCell(prichod.getCasPrichodu().format(formatter));
                tabulka.addCell("Garáž");
                tabulka.addCell(prichod.getCasPrichodu().plusSeconds(smena.getCestaDoGaraze()).format(formatter));
                tabulka.addCell("");
                document.add(tabulka);
                poradieSmeny++;
            }
            poradieTurnusu++;
        }
        document.close();
    }

    public static void vypisLinkyDoPdf(List<SpojeLinky> linky, String nazov) throws FileNotFoundException, DocumentException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        Document document = new Document(PageSize.A4.rotate());
        PdfWriter.getInstance(document, new FileOutputStream(new File(nazov + ".pdf")));
        document.open();
        document.addTitle("Linky");
        for (SpojeLinky spojeLinky : linky) {
            document.add(new Paragraph("Linka " + spojeLinky.getLinka()));
            int poradieSmeru = 1;
            for (List<SpojLinky> spojLinky : spojeLinky.getSpoje().values()) {
                Collections.sort(spojLinky, Comparator.comparing(sp -> sp.getSpoj().getCasOdchodu()));
                document.add(new Paragraph("Smer " + poradieSmeru + " - obslúžených " + spojLinky.stream().filter(s -> s.isObsluzeny()).count()
                        + " z " + spojLinky.size() + " spojov"));
                PdfPTable tabulka = new PdfPTable(6);
                tabulka.setWidthPercentage(100);
                tabulka.setSpacingBefore(5f);
                tabulka.setSpacingAfter(5f);
                PdfPCell hlavicka = new PdfPCell(new Phrase("Spoj"));
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
                hlavicka = new PdfPCell(new Phrase("Obslúžený"));
                hlavicka.setHorizontalAlignment(Element.ALIGN_CENTER);
                tabulka.addCell(hlavicka);
                for (SpojLinky spoj : spojLinky) {
                    tabulka.addCell(String.valueOf(spoj.getSpoj().getKluc().getId()));
                    Zastavka odch = spoj.getSpoj().getMiestoOdchodu();
                    tabulka.addCell(String.valueOf(odch.getId()) + " - " + odch.getNazov());
                    tabulka.addCell(spoj.getSpoj().getCasOdchodu().format(formatter));
                    Zastavka prich = spoj.getSpoj().getMiestoPrichodu();
                    tabulka.addCell(String.valueOf(prich.getId()) + " - " + prich.getNazov());
                    tabulka.addCell(spoj.getSpoj().getCasPrichodu().format(formatter));
                    tabulka.addCell(spoj.isObsluzeny() ? "Áno" : "Nie");
                }
                document.add(tabulka);
                poradieSmeru++;
            }
        }
        document.close();
    }

    public static class PriorityImport {

        private final Map<KlucSpoja, Integer> priority;
        private final List<KlucSpoja> musiObsluzit;

        public PriorityImport(Map<KlucSpoja, Integer> priority, List<KlucSpoja> musiObsluzit) {
            this.priority = priority;
            this.musiObsluzit = musiObsluzit;
        }

        public Map<KlucSpoja, Integer> getPriority() {
            return priority;
        }

        public List<KlucSpoja> getMusiObsluzit() {
            return musiObsluzit;
        }

    }
}

package konfiguracia;

import java.util.List;
import static konfiguracia.Konstanty.CENA_KILOMETER;
import static konfiguracia.Konstanty.CENA_SOFERA;
import static konfiguracia.Konstanty.GARAZ;
import static konfiguracia.Konstanty.MAX_DOBA_JAZDY;
import static konfiguracia.Konstanty.MAX_DOBA_SMENY;
import static konfiguracia.Konstanty.PRESTAVKA_V_DOBE_JAZDY;
import static konfiguracia.Konstanty.PRESTAVKY;
import konfiguracia.Konstanty.Prestavka;
import static konfiguracia.Konstanty.REZERVA;
import static konfiguracia.Konstanty.REZERVA_GARAZ;

/**
 *
 * @author Martina Cernekova
 */
public class Konfiguracia {

    private Integer garaz;
    private Integer rezerva;
    private Integer rezervaGaraz;
    private Integer maxDobaJazdy;
    private Integer maxDobaSmeny;
    private Integer prestavkaVdobeJazdy;
    private Integer cenaSofera;
    private Integer cenaKilometer;
    private List<Prestavka> prestavky;

    public void vynuluj() {
        garaz = null;
        rezerva = null;
        rezervaGaraz = null;
        maxDobaJazdy = null;
        maxDobaSmeny = null;
        prestavkaVdobeJazdy = null;
        cenaSofera = null;
        cenaKilometer = null;
        prestavky = null;
    }

    public Integer getGaraz() {
        return garaz != null ? garaz : GARAZ;
    }

    public void setGaraz(Integer garaz) {
        this.garaz = garaz;
    }

    public Integer getRezerva() {
        return rezerva != null ? rezerva : REZERVA;
    }

    public void setRezerva(Integer rezerva) {
        this.rezerva = rezerva;
    }

    public Integer getRezervaGaraz() {
        return rezervaGaraz != null ? rezervaGaraz : REZERVA_GARAZ;
    }

    public void setRezervaGaraz(Integer rezervaGaraz) {
        this.rezervaGaraz = rezervaGaraz;
    }

    public Integer getMaxDobaJazdy() {
        return maxDobaJazdy != null ? maxDobaJazdy : MAX_DOBA_JAZDY;
    }

    public void setMaxDobaJazdy(Integer maxDobaJazdy) {
        this.maxDobaJazdy = maxDobaJazdy;
    }

    public Integer getMaxDobaSmeny() {
        return maxDobaSmeny != null ? maxDobaJazdy : MAX_DOBA_SMENY;
    }

    public void setMaxDobaSmeny(Integer maxDobaSmeny) {
        this.maxDobaSmeny = maxDobaSmeny;
    }

    public Integer getPrestavkaVdobeJazdy() {
        return prestavkaVdobeJazdy != null ? prestavkaVdobeJazdy : PRESTAVKA_V_DOBE_JAZDY;
    }

    public void setPrestavkaVdobeJazdy(Integer prestavkaVdobeJazdy) {
        this.prestavkaVdobeJazdy = prestavkaVdobeJazdy;
    }

    public Integer getCenaSofera() {
        return cenaSofera != null ? cenaSofera : CENA_SOFERA;
    }

    public void setCenaSofera(Integer cenaSofera) {
        this.cenaSofera = cenaSofera;
    }

    public Integer getCenaKilometer() {
        return cenaKilometer != null ? cenaKilometer : CENA_KILOMETER;
    }

    public void setCenaKilometer(Integer cenaKilometer) {
        this.cenaKilometer = cenaKilometer;
    }

    public List<Prestavka> getPrestavky() {
        return prestavky != null ? prestavky : PRESTAVKY;
    }

    public void setPrestavky(List<Prestavka> prestavky) {
        this.prestavky = prestavky;
    }

}

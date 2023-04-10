package dataObjekty;

/**
 *
 * @author Martina Cernekova
 */
public class Zastavka {

    private final int id;
    private final String nazov;

    public Zastavka(int id, String nazov) {
        this.id = id;
        this.nazov = nazov;
    }

    public int getId() {
        return id;
    }

    public String getNazov() {
        return nazov;
    }

}

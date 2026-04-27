import java.util.*;

// --- ENUMERÁCIE ---
enum UrovenSpoluprace {
    ZLA(1), PRIEMERNA(2), DOBRA(3);

    private final int hodnota;
    UrovenSpoluprace(int hodnota) { this.hodnota = hodnota; }
    public int getHodnota() { return hodnota; }
}

// --- ABSTRAKTNÁ TRIEDA (Spĺňa požiadavku na abstraktnú triedu/rozhranie) ---
abstract class Zamestnanec {
    private static int idCounter = 1; // Automatické prideľovanie ID
    
    protected int id;
    protected String meno;
    protected String priezvisko;
    protected int rokNarodenia;
    // Dynamická dátová štruktúra pre evidenciu spolupracovníkov (ID kolegu -> Úroveň)
    protected Map<Integer, UrovenSpoluprace> spolupracovnici;

    public Zamestnanec(String meno, String priezvisko, int rokNarodenia) {
        this.id = idCounter++;
        this.meno = meno;
        this.priezvisko = priezvisko;
        this.rokNarodenia = rokNarodenia;
        this.spolupracovnici = new HashMap<>();
    }

    public int getId() { return id; }
    public String getPriezvisko() { return priezvisko; }
    public String getMeno() { return meno; }
    public Map<Integer, UrovenSpoluprace> getSpolupracovnici() { return spolupracovnici; }

    public void pridejSpolupracovnika(int idKolegu, UrovenSpoluprace uroven) {
        spolupracovnici.put(idKolegu, uroven);
    }

    public void odstranSpolupracovnika(int idKolegu) {
        spolupracovnici.remove(idKolegu);
    }

    public void vypisInformacie() {
        System.out.println("ID: " + id + " | " + meno + " " + priezvisko + " (" + rokNarodenia + ") | Skupina: " + getNazovSkupiny());
        System.out.println("  Počet evidovaných spoluprác: " + spolupracovnici.size());
    }

    // Abstraktné metódy, ktoré musia implementovať potomkovia
    public abstract String getNazovSkupiny();
    public abstract void spustZrucnost(Map<Integer, Zamestnanec> databaza);
}

// --- TRIEDY SKUPÍN (Dedičnosť a Polymorfizmus) ---
class DatovyAnalytik extends Zamestnanec {
    
    public DatovyAnalytik(String meno, String priezvisko, int rokNarodenia) {
        super(meno, priezvisko, rokNarodenia);
    }

    @Override
    public String getNazovSkupiny() { return "Dátový analytik"; }

    @Override
    public void spustZrucnost(Map<Integer, Zamestnanec> databaza) {
        System.out.println("--> Spúšťam analýzu spoločných spolupracovníkov pre ID " + this.id);
        if (spolupracovnici.isEmpty()) {
            System.out.println("  Zamestnanec nemá žiadnych spolupracovníkov.");
            return;
        }

        int najlepsiKolegaId = -1;
        int maxSpolocnych = -1;

        for (int idKolegu : spolupracovnici.keySet()) {
            Zamestnanec kolega = databaza.get(idKolegu);
            if (kolega == null) continue;

            int spolocni = 0;
            // Prienik mojich spolupracovníkov a spolupracovníkov kolegu
            for (int idKoleguOdKolegu : kolega.getSpolupracovnici().keySet()) {
                if (this.spolupracovnici.containsKey(idKoleguOdKolegu)) {
                    spolocni++;
                }
            }

            if (spolocni > maxSpolocnych) {
                maxSpolocnych = spolocni;
                najlepsiKolegaId = idKolegu;
            }
        }

        if (najlepsiKolegaId != -1) {
            Zamestnanec top = databaza.get(najlepsiKolegaId);
            System.out.println("  Najviac spoločných väzieb (" + maxSpolocnych + ") máte s: " 
                + top.getMeno() + " " + top.getPriezvisko() + " (ID: " + top.getId() + ")");
        }
    }
}

class BezpecnostnySpecialista extends Zamestnanec {
    
    public BezpecnostnySpecialista(String meno, String priezvisko, int rokNarodenia) {
        super(meno, priezvisko, rokNarodenia);
    }

    @Override
    public String getNazovSkupiny() { return "Bezpečnostný špecialista"; }

    @Override
    public void spustZrucnost(Map<Integer, Zamestnanec> databaza) {
        System.out.println("--> Vypočítavam rizikové skóre pre ID " + this.id);
        if (spolupracovnici.isEmpty()) {
            System.out.println("  Rizikové skóre: 0.0 (Žiadne spolupráce)");
            return;
        }

        int sucetUrovni = 0;
        for (UrovenSpoluprace uroven : spolupracovnici.values()) {
            sucetUrovni += uroven.getHodnota();
        }

        double priemernaKvalita = (double) sucetUrovni / spolupracovnici.size();
        
        // Vlastný algoritmus: Viac ľudí = väčšia plocha útoku. 
        // Horšia priemerná kvalita (bližšie k 1) = vyššie riziko.
        // Vzorec: počet_spolupracovníkov * (4.0 - priemerná_kvalita)
        double rizikoveSkore = spolupracovnici.size() * (4.0 - priemernáKvalita);
        
        System.out.printf("  Rizikové skóre spolupráce je: %.2f (Priemerná kvalita: %.2f)\n", rizikoveSkore, priemernaKvalita);
    }
}

// --- HLAVNÁ TRIEDA A LOGIKA APLIKÁCIE ---
public class SpravaZamestnancov {
    private Map<Integer, Zamestnanec> databaza = new HashMap<>();
    private Scanner scanner = new Scanner(System.stdin);

    public static void main(String[] args) {
        SpravaZamestnancov app = new SpravaZamestnancov();
        app.spust();
    }

    public void spust() {
        while (true) {
            System.out.println("\n=== DATABÁZA ZAMESTNANCOV ===");
            System.out.println("1. Pridať zamestnanca");
            System.out.println("2. Pridať spoluprácu");
            System.out.println("3. Odobrať zamestnanca");
            System.out.println("4. Vyhľadať zamestnanca podľa ID");
            System.out.println("5. Spustiť zručnosť zamestnanca");
            System.out.println("6. Abecedný výpis zamestnancov (podľa skupín)");
            System.out.println("0. Koniec");
            System.out.print("Vyberte akciu: ");
            
            String volba = scanner.nextLine();
            
            switch (volba) {
                case "1": pridatZamestnanca(); break;
                case "2": pridatSpolupracu(); break;
                case "3": odobratZamestnanca(); break;
                case "4": vyhladatZamestnanca(); break;
                case "5": spustitZrucnost(); break;
                case "6": vypisAbecedne(); break;
                case "0": System.out.println("Ukončujem program..."); return;
                default: System.out.println("Neplatná voľba, skúste to znova.");
            }
        }
    }

    // a) Pridanie zamestnanca
    private void pridatZamestnanca() {
        System.out.print("Skupina (1 = Analytik, 2 = Špecialista): ");
        String skupina = scanner.nextLine();
        System.out.print("Meno: ");
        String meno = scanner.nextLine();
        System.out.print("Priezvisko: ");
        String priezvisko = scanner.nextLine();
        System.out.print("Rok narodenia: ");
        int rok = Integer.parseInt(scanner.nextLine());

        Zamestnanec z;
        if (skupina.equals("1")) {
            z = new DatovyAnalytik(meno, priezvisko, rok);
        } else if (skupina.equals("2")) {
            z = new BezpecnostnySpecialista(meno, priezvisko, rok);
        } else {
            System.out.println("Neplatná skupina.");
            return;
        }

        databaza.put(z.getId(), z);
        System.out.println("Zamestnanec pridaný s ID: " + z.getId());
    }

    // b) Pridanie spolupráce
    private void pridatSpolupracu() {
        System.out.print("ID zamestnanca: ");
        int id1 = Integer.parseInt(scanner.nextLine());
        System.out.print("ID kolegu: ");
        int id2 = Integer.parseInt(scanner.nextLine());
        System.out.print("Úroveň spolupráce (1 = ZLÁ, 2 = PRIEMERNÁ, 3 = DOBRÁ): ");
        int urovenVolba = Integer.parseInt(scanner.nextLine());

        if (!databaza.containsKey(id1) || !databaza.containsKey(id2)) {
            System.out.println("Jedno alebo obe ID neexistujú v databáze.");
            return;
        }

        UrovenSpoluprace uroven = switch (urovenVolba) {
            case 1 -> UrovenSpoluprace.ZLA;
            case 2 -> UrovenSpoluprace.PRIEMERNA;
            case 3 -> UrovenSpoluprace.DOBRA;
            default -> null;
        };

        if (uroven == null) {
            System.out.println("Neplatná úroveň.");
            return;
        }

        databaza.get(id1).pridejSpolupracovnika(id2, uroven);
        System.out.println("Spolupráca úspešne pridaná.");
    }

    // c) Odobranie zamestnanca
    private void odobratZamestnanca() {
        System.out.print("Zadajte ID zamestnanca na odstránenie: ");
        int id = Integer.parseInt(scanner.nextLine());

        if (databaza.remove(id) != null) {
            // Prejsť všetkých zostávajúcich zamestnancov a odstrániť väzby na zmazaného
            for (Zamestnanec z : databaza.values()) {
                z.odstranSpolupracovnika(id);
            }
            System.out.println("Zamestnanec a všetky jeho väzby boli odstránené.");
        } else {
            System.out.println("Zamestnanec s týmto ID nebol nájdený.");
        }
    }

    // d) Vyhľadanie zamestnanca podľa ID
    private void vyhladatZamestnanca() {
        System.out.print("Zadajte ID zamestnanca: ");
        int id = Integer.parseInt(scanner.nextLine());

        Zamestnanec z = databaza.get(id);
        if (z != null) {
            z.vypisInformacie();
        } else {
            System.out.println("Zamestnanec s týmto ID neexistuje.");
        }
    }

    // e) Spustenie zručnosti
    private void spustitZrucnost() {
        System.out.print("Zadajte ID zamestnanca: ");
        int id = Integer.parseInt(scanner.nextLine());

        Zamestnanec z = databaza.get(id);
        if (z != null) {
            z.spustZrucnost(databaza);
        } else {
            System.out.println("Zamestnanec s týmto ID neexistuje.");
        }
    }

    // f) Abecedný výpis v skupinách
    private void vypisAbecedne() {
        List<Zamestnanec> analytici = new ArrayList<>();
        List<Zamestnanec> specialisti = new ArrayList<>();

        // Rozdelenie do skupín
        for (Zamestnanec z : databaza.values()) {
            if (z instanceof DatovyAnalytik) analytici.add(z);
            else if (z instanceof BezpecnostnySpecialista) specialisti.add(z);
        }

        // Komparátor pre radenie podľa priezviska
        Comparator<Zamestnanec> podlaPriezviska = Comparator.comparing(Zamestnanec::getPriezvisko);
        analytici.sort(podlaPriezviska);
        specialisti.sort(podlaPriezviska);

        System.out.println("\n--- DÁTOVÍ ANALYTICI ---");
        for (Zamestnanec z : analytici) {
            System.out.println(z.getPriezvisko() + " " + z.getMeno() + " (ID: " + z.getId() + ")");
        }

        System.out.println("\n--- BEZPEČNOSTNÍ ŠPECIALISTI ---");
        for (Zamestnanec z : specialisti) {
            System.out.println(z.getPriezvisko() + " " + z.getMeno() + " (ID: " + z.getId() + ")");
        }
    }
}
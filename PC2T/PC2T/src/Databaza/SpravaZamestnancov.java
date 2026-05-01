package Databaza;
import java.util.*;
import java.sql.*;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.File;

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
    
    public Zamestnanec(int id, String meno, String priezvisko, int rokNarodenia) {
    	this.id = id;
    	this.meno = meno;
        this.priezvisko = priezvisko;
        this.rokNarodenia = rokNarodenia;
        this.spolupracovnici = new HashMap<>();
        
        if (id>= idCounter) {
        	idCounter = id+1;
        }
    }

    public int getId() { return id; }
    public String getPriezvisko() { return priezvisko; }
    public String getMeno() { return meno; }
    public Map<Integer, UrovenSpoluprace> getSpolupracovnici() { return spolupracovnici; }
    public int getRok() { return rokNarodenia; }

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
    
    public DatovyAnalytik(int id, String meno, String priezvisko, int rokNarodenia) {
    	super(id, meno, priezvisko, rokNarodenia);
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
    
    public BezpecnostnySpecialista(int id, String meno, String priezvisko, int rokNarodenia) {
    	super(id, meno, priezvisko, rokNarodenia);
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
        double rizikoveSkore = spolupracovnici.size() * (4.0 - priemernaKvalita);
        
        System.out.printf("  Rizikové skóre spolupráce je: %.2f (Priemerná kvalita: %.2f)\n", rizikoveSkore, priemernaKvalita);
    }
}

// --- HLAVNÁ TRIEDA A LOGIKA APLIKÁCIE ---
public class SpravaZamestnancov {
    private Map<Integer, Zamestnanec> databaza = new HashMap<>();
    private Scanner scanner = new Scanner(System.in);
    
    private final String cesta = "jdbc:sqlite:zamestnanci.db";
    
    public SpravaZamestnancov() {
    	inicializace();
    	
    }
    private void inicializace() {
    	try (Connection conn = DriverManager.getConnection(cesta)){
    		Statement stmt = conn.createStatement();
    		stmt.execute("PRAGMA foreign_keys = ON");  		
    		stmt.execute("CREATE TABLE IF NOT EXISTS zamestnanci (id INTEGER PRIMARY KEY, meno TEXT, priezvisko TEXT, rok INTEGER, skupina TEXT)");
    		stmt.execute("CREATE TABLE IF NOT EXISTS relace (id1 INTEGER, id2 INTEGER, uroven TEXT, PRIMARY KEY (id1, id2), FOREIGN KEY(id1) REFERENCES zamestnanci(id) ON DELETE CASCADE, FOREIGN KEY(id2) REFERENCES zamestnanci(id) ON DELETE CASCADE)");
    		
    		ResultSet a = stmt.executeQuery("SELECT * FROM zamestnanci");
    		while (a.next()) {
    			int id = a.getInt("id");
    			String meno = a.getString("meno");
    			String priezvisko = a.getString("priezvisko");
    			int rok = a.getInt("rok");
    			String skupina = a.getString("skupina");
    			
    			Zamestnanec z = null;
    			
    			if (skupina.equals("Dátový analytik")) {
    				z = new DatovyAnalytik(id, meno, priezvisko, rok);
    			}
    			else {
    				z = new BezpecnostnySpecialista(id, meno, priezvisko, rok);
    			}
    			databaza.put(z.getId(), z);
    		}
    		
    		ResultSet b = stmt.executeQuery("SELECT * FROM relace");
        	while (b.next()) {
        		int id1 = b.getInt("id1");
        		int id2 = b.getInt("id2");
        		String uroven = b.getString("uroven");
        		
        		UrovenSpoluprace uroven_ = null;
    			uroven_ = UrovenSpoluprace.valueOf(uroven);
    			databaza.get(id1).pridejSpolupracovnika(id2, uroven_);
        	}
    	}
    	catch(SQLException e ) {
    		e.printStackTrace();
    	}
    	
    }
    
    public void ulozeniDoSQL(Zamestnanec z) {
    	String sql = "INSERT OR REPLACE INTO zamestnanci (id, meno, priezvisko, rok, skupina) VALUES (?, ?, ?, ?, ?)";
    	try (Connection conn = DriverManager.getConnection(cesta);
    			PreparedStatement pstmt = conn.prepareStatement(sql)){
    		
    		pstmt.setInt(1, z.getId());
    		pstmt.setString(2,  z.getMeno());
    		pstmt.setString(3,  z.getPriezvisko());
    		pstmt.setInt(4,  z.getRok());
    		pstmt.setString(5,  z.getNazovSkupiny());
    		
    		pstmt.executeUpdate();
    		
    	}
    	catch (SQLException e) {
    		e.printStackTrace();
    	}
    }
    
    public void ulozeniRelaceDoSQL(int id1, int id2, UrovenSpoluprace uroven) {
    	String sql = "INSERT OR REPLACE INTO relace (id1, id2, uroven) VALUES (?, ?, ?)";
    	try (Connection conn = DriverManager.getConnection(cesta);
    			PreparedStatement pstmt = conn.prepareStatement(sql)){
    		pstmt.setInt(1, id1);
            pstmt.setInt(2, id2);
            pstmt.setString(3, uroven.name());
            pstmt.executeUpdate();
    	}
    	catch (SQLException e) {
    		e.printStackTrace();
    	}
    }
    
    public void smazaniZSQL(int id) {
    	String sql = "DELETE FROM zamestnanci WHERE id = ?";
    	try (Connection conn = DriverManager.getConnection(cesta)){
    		try (Statement stmt = conn.createStatement()) {
                stmt.execute("PRAGMA foreign_keys = ON");
            }
    		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, id);
                pstmt.executeUpdate();
    		}
    		databaza.remove(id);
    	}
    	catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
    	
        SpravaZamestnancov app = new SpravaZamestnancov();
        app.spust();
    }

    public void spust() {
        while (true) {
            System.out.println("\n=== DATABÁZA ZAMESTNANCOV ===");
            System.out.println("1. Pridať zamestnanca");
            System.out.println("2. Pridať zamastnanca ze súboru");
            System.out.println("3. Uložiť zamestnanca do súboru");
            System.out.println("4. Odobrať zamestnanca");
            System.out.println("5. Pridať spoluprácu");
            System.out.println("6. Vyhľadať zamestnanca podľa ID");
            System.out.println("7. Spustiť zručnosť zamestnanca");
            System.out.println("8. Abecedný výpis zamestnancov (podľa skupín)");
            System.out.println("9. Statistiky");
            System.out.println("0. Koniec");
            System.out.print("Vyberte akciu: ");
            
            String volba = scanner.nextLine();
            
            switch (volba) {
                case "1": pridatZamestnanca(); break;
                case "2": pridatZeSouboru(); break;
                case "3": ulozitDoSouboru(); break;
                case "4": odobratZamestnanca(); break;
                case "5": pridatSpolupracu(); break;
                case "6": vyhladatZamestnanca(); break;
                case "7": spustitZrucnost(); break;
                case "8": vypisAbecedne(); break;
                case "9":statistiky();break;
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
        ulozeniDoSQL(z);
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

        UrovenSpoluprace uroven = null;
        switch (urovenVolba) {
            case 1: 
                uroven = UrovenSpoluprace.ZLA; 
                break;
            case 2: 
                uroven = UrovenSpoluprace.PRIEMERNA; 
                break;
            case 3: 
                uroven = UrovenSpoluprace.DOBRA; 
                break;
            default: 
                uroven = null; 
                break;
        }

        if (uroven == null) {
            System.out.println("Neplatná úroveň.");
            return;
        }

        databaza.get(id1).pridejSpolupracovnika(id2, uroven);
        ulozeniRelaceDoSQL(id1, id2, uroven);
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
                smazaniZSQL(id);
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

        System.out.println("\n--- DÁTOVÍ ANALYTICI (" + analytici.size() + ") ---");
        for (Zamestnanec z : analytici) {
            System.out.println(z.getPriezvisko() + " " + z.getMeno() + " (ID: " + z.getId() + ")");
        }

        System.out.println("\n--- BEZPEČNOSTNÍ ŠPECIALISTI (" + specialisti.size() + ") ---");
        for (Zamestnanec z : specialisti) {
            System.out.println(z.getPriezvisko() + " " + z.getMeno() + " (ID: " + z.getId() + ")");
        }
    }
    
    private void statistiky() {
    	//algoritmus na spočítání průměru spolupráce + základní hodnoty k získání nejvíce vazeb
    	int soucet = 0;
    	int pocet = 0;
    	int nejvice_vazeb = 0;
    	for (Zamestnanec z : databaza.values()) {
    		int pocet_vazeb = 0;
            for (UrovenSpoluprace uroven : z.getSpolupracovnici().values()) {
                soucet += uroven.getHodnota();
                pocet++;
                pocet_vazeb++;
            	}
            if (pocet_vazeb > nejvice_vazeb) {
            	nejvice_vazeb = pocet_vazeb;
            	}
    		}
    	double vysledek = (double) soucet / pocet;

    	System.out.println("Průměrná hodnota spolupráce je " + vysledek + " bodů " + preklad(vysledek));
    	System.out.println("------------------------------------------------------");
    	System.out.println("Zaměstnanci s nejvíce (" + nejvice_vazeb +") vazbami:");
    	
    	// Algoritmus na získání a vypsání nejvíce vazeb
    	for (Zamestnanec z: databaza.values()) {
    		int pocet_vazeb = 0;
    		for (UrovenSpoluprace uroven : z.getSpolupracovnici().values()) {
    			pocet_vazeb++;
    		}
    		if (pocet_vazeb == nejvice_vazeb) {
    			System.out.println("id:" + z.getId());
    			System.out.println("Jméno:" + z.getMeno());
    			System.out.println("Příjmení:" + z.getPriezvisko());
    			System.out.println("------------------------------------------------------");
    		}
    	}
    		
    	}
    private String preklad(double vysledek) {
    	if (vysledek >= 2.5) return "(Dobrá)";
    	if (vysledek < 1.5) return "(Špatná)";
    	return "(Průměrná)";
    }
    
    private void ulozitDoSouboru() {
    	System.out.print("ID zamestnanca: ");
        int id = Integer.parseInt(scanner.nextLine());
        
        Zamestnanec z = databaza.get(id);
        if (z == null) {
        	System.out.println("Žádný zaměstnanec pod daným Id neexistuje");
        	return;
        }
        
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(z.getMeno() + "_" + z.getPriezvisko()))){
        	writer.println(z.getMeno() + "_" + z.getPriezvisko());
        	writer.println(z.getRok());
        	writer.println(z.getNazovSkupiny());
        	writer.println(z.getSpolupracovnici().size());
        	for (Integer ID : z.getSpolupracovnici().keySet()) {
        		writer.println(databaza.get(ID).getMeno() + "_" + databaza.get(ID).getPriezvisko() + "_" + databaza.get(ID).getNazovSkupiny() + "_" + z.getSpolupracovnici().get(ID));
        	}
        }
        catch (IOException e) {
        	
        }
    }
    
    private void pridatZeSouboru() {
    	System.out.print("Prosím nahrajte soubor do stejné složky jako kód");
    	System.out.print("Napište prosím název souboru (i speciální znaky) :");
    	String nazev = scanner.nextLine();
    	File soubor = new File(nazev);
    	if(!soubor.exists()) {
    		System.out.println("Špatně zadaný název nebo soubor neexistuje");
    	}

    		
    	try (BufferedReader reader = new BufferedReader(new FileReader(nazev))){
    		String radek = reader.readLine();
    		String[] rozdeleni = radek.split("_");
    		String jmeno = rozdeleni[0];
    		String priezvisko = rozdeleni[1];
    		int rok = Integer.parseInt(reader.readLine());
    		String NazovSkupiny = reader.readLine();
    		String skupina = "";
    		if ("Dátový analytik".equals(NazovSkupiny)) {
    			skupina = "1";
    		}
    		else {
    			skupina = "2";
    		}
    		for (Zamestnanec z : databaza.values()) {
    			if (z.getMeno().equalsIgnoreCase(jmeno) && z.getPriezvisko().equalsIgnoreCase(priezvisko) && z.getNazovSkupiny().equalsIgnoreCase(NazovSkupiny)) {
    				System.out.println("zaměstnanec již existuje pod id: " + z.getId());
    				return;
    			}}
    			Zamestnanec zz;
    			if (skupina.equals("1")) {
    				zz = new DatovyAnalytik(jmeno,priezvisko, rok);
    			}
    			else {
    				zz = new BezpecnostnySpecialista(jmeno,priezvisko, rok);
    			}
    			databaza.put(zz.getId(), zz);
    			int Id = zz.getId();
    			System.out.println("Zaměstnanec by vytvořen po id: " + Id);
    			ulozeniDoSQL(zz);
    		int pocet = Integer.parseInt(reader.readLine());

    		int nacteno = 0;
    		while ( nacteno < pocet ) {
    			String spolupraca = reader.readLine();
    			String[] rozdel = spolupraca.split("_");
    			String jmenoSpolupracovnika = rozdel[0];
    			String priezviskoSpolupracovnika = rozdel[1];
    			String skupinaSpolupracovnika = rozdel[2];
    			String urovenSpoluprace = rozdel[3];
    			boolean nalezeno = false;
    			for (Zamestnanec z : databaza.values()) {
    	    		if (z.getMeno().equals(jmenoSpolupracovnika) && z.getPriezvisko().equals(priezviskoSpolupracovnika) && z.getNazovSkupiny().equals(skupinaSpolupracovnika)) {
    	    			UrovenSpoluprace uroven = null;
    	    			uroven = UrovenSpoluprace.valueOf(urovenSpoluprace);
    	    			databaza.get(Id).pridejSpolupracovnika(z.getId(), uroven);
    	    			nalezeno = true;
    	    			ulozeniRelaceDoSQL(Id, z.getId(), uroven);
    	    			break;
    	    			}
    				}
    			if (!nalezeno) {
    				System.out.println("Spolupracovník " + jmenoSpolupracovnika + " nebyl nalezen");
    			}
    			nacteno++;
    			}
    		}
    	catch(IOException e) {
    		
    	}
    	}
    	}
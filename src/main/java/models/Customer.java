package models;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A ügyfelek és ügykezelőket (is_admin = true esetén) leképező model.
 * Az ugyenezen nevű Customer tábla kezelésére szolgál.
 * ~~~~~~~~~~~~~~~~~~ PÉLDÁK ~~~~~~~~~~~~~~~~~~
 * Új rekord létrehozása
 * ---------------------
 * Az új rekord létrehozásához a következő SQL lekérdezést kell létrehozni:
 * INSERT INTO Customer (name, private_id, ...stb...) VALUES ('Trab Antal', '239874JA', ...stb...);
 * Ezt a BaseModel automatikusan megteszi mivel ez az osztály származik belőle.
 * <pre><code>
 * Customer valaki = new Customer("Trab Antal", "239874JA", "Szeged", "Valami út 5.", 9000, false, "titkos", "titkos");
 * if (valaki.save()) { // lefut validate() függvényt, hogy ellenőrizze a megadott adatokat
 *   // siker, menthető
 * } else {
 *  // hiba, valami gond van
 * }
 * </code></pre>
 * 
 * Meglévő rekord lekérdezése
 * ---------------------
 * Egy adott id-jű ügyfél keresése:
 * Customer valaki = Customer.findById(1000);
 * 
 * Meglévő rekord módosítása
 * ---------------------
 *  <pre><code>
 *  Customer valaki = Customer.findById(1000); // id = 1000 létezik
 *  valaki.isChanged() == false // nem változotattunk még rajta
 *  valaki.setName("Más Valaki");
 *  valaki.isChanged() == true  // menteni kéne
 *  valaki.save();              // frissítés
 *  </code></pre>
 * 
 * @author akos
 */
public class Customer extends BaseModel {
    /**
     * A kód átláthatósága miatt minden oszlopnak megadjuk, hogy milyen helyen 
     * található pontosan egy konstans intben.
    */
    private static final int COLUMN_NAME        = 2;
    private static final int COLUMN_PRIVATE_ID  = 3;
    private static final int COLUMN_CITY        = 4;
    private static final int COLUMN_ADDRESS     = 5;
    private static final int COLUMN_POSTAL_CODE = 6;
    private static final int COLUMN_CREATED_AT  = 7;
    private static final int COLUMN_IS_ADMIN    = 8;
    private static final int COLUMN_PASSWORD    = 9;
    private static final int COLUMN_OFFICE_ID   = 10;
    
    /**
     * A BaseModel-nek szüksége van arra, hogy ismerje ezen tábla mezőit a helyes
     * sorrendben (id nélkül).
     */
    private static final String COLUMN_NAMES[] =
    {
      "name", "private_id", "city", "address", "postal_code",
      "created_at", "is_admin", "password", "office_id"
    };
    
    private String          name        = null;
    private String          private_id  = null; /* NEM KÜLSŐ KULCS */
    private String          city        = null;
    private String          address     = null;
    private int             postal_code = 0;
    private Timestamp       created_at  = null;
    private boolean         is_admin    = false;
    private String          password    = null;
    private int             office_id   = 0;
    private Office          office      = null; // csak akkor keressük meg ha szükséges
    
    private static ArrayList<Customer> setupCustomers(ResultSet rs) throws SQLException {
        ArrayList<Customer> all = new ArrayList<>();
                
        while (rs.next()) {
            Customer c = new Customer();
            c.setValues(rs);
            all.add(c);
        }
        return all;
    }
    
    /* ========~~~~--+ PUBLIKUS OSZTÁLYMETÓDUSOK (KERESÉS/LEKÉRÉS) +--~~~~======== */

    /**
     * Az összes ügyfél lekérése egy ArrayList-be.
     * 
     * @param limit maximális ügyfélszám
     * @return ügyfeleket tartalmazó ArrayList
     * @throws SQLException
     */
    
    public static List<Customer> getAll(int limit) throws SQLException {
        /* A find segédfüggvény automatikusan létrehozza és lefuttatja a 
           megfelelő SELECT-et
        */
        ResultSet rs = find(Customer.tableName(), null, null, limit);
        return setupCustomers(rs);
    }
    
    /**
     * Az összes ügyfél lekérése.
     * @return
     * @throws SQLException
     */
    public static List<Customer> getAll() throws SQLException {
        return getAll(0);
    }
    
    /**
     * Ügyfél lekérése azonosító alapján.
     * Mivel az id egyedi és elsődleges kulcs, csak egy Customer-t adhat vissza,
     * de az lehet null amennyiben nincs ilyen azonosítójú ügyfél.
     * Példa:
     * <pre><code>
     * Customer c = Customer.findById(1000);
     * </code></pre>
     * @param id ügyfélazonosító
     * @return ügyfél vagy null ha nem létezik
     * @throws SQLException
     */
    public static Customer findById(int id) throws SQLException {
        Customer c = null;
        ResultSet rs = findSingle(Customer.tableName(), "id", Integer.toString(id));
        if (rs.next()) {
            c = new Customer();
            /* model betöltése rekordból */
            c.setValues(rs);
        }
        return c;
    }
    
    /* TODO */

    /**
     * Ügyfelek lekérése fiók szerint.
     * @param off fiük
     * @param limit maximális elemszám
     * @return off fiókban létrehozott ügyfeleket tartalmazó lista
     * @throws SQLException
     */
    
    public static List<Customer> findByOffice(Office off, int limit) throws SQLException {
        /* Az Office id-ját sajnos String-gé kell alakítani */
        ResultSet rs = findSingle(Customer.tableName(), "office_id", Integer.toString(off.getId()), limit);
        return setupCustomers(rs);
    }
    
    public static List<Customer> findByOffice(Office off) throws SQLException {
        return findByOffice(off, 0);
    }
    
    /**
     * Ügyfél azonosító/jelszó ellenőrzése a belépéshez.
     * @param id azonsító
     * @param passw jelszó
     * @return ügyfél ha jó az azonosító+jelszó, null minden más esetben
     */
    public static Customer login(String id, String passw) {
        Customer c = null;
        try {
            c = Customer.findById(Integer.parseInt(id));
            if (c == null || !c.password.equals(passw)) {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
        return c;
    }
    
    /**
     * A getTableName()-el megegyező függvény (csak statikus). 
     * A tábla nevét adja vissza.
     * @return Az aktuális tábla neve
     */
    public static String tableName() { return Customer.class.getSimpleName(); }
    
    
    /* ========~~~~--+ PUBLIKUS PÉLDÁNYMETÓDUSOK +--~~~~======== */

    /**
     * Üres ügyfél létrehozása.
     * Később ebbe lehet betölteni az adatbázisból a setValues() segítségével, 
     * vagy új rekordot létrehozni a többi set*() függvénnyel.
     */
    public Customer() {
        this(null, null, null, null, 0, false, null);
    }
    
    /**
     * Üres objektum létrehozása
     * @param name név
     * @param privid személyi/cégazonosító
     * @param city város
     * @param address cím
     * @param pcode irányítószám
     * @param is_admin true ha ügyintéző
     * @param password jelszó
     */
    public Customer(String name, String privid, String city, String address, int pcode, boolean is_admin,
            String password) {
        /* Az oszlopnevek megadása a BaseModel-nek */
        super(COLUMN_NAMES);

        this.name        = name;
        this.private_id  = privid;
        this.city        = city;
        this.address     = address;
        this.postal_code = pcode;
        this.is_admin    = is_admin;
        this.password    = password;
        this.created_at  = null;
        this.is_changed     = true;
        this.is_new         = true;
    }
    
    /**
     * Új számla ehhez az ügyfélhez.
     * @return új számla
     */
    public Account newAccount() {
        Account acc = new Account();
        acc.setCustomer(this);
        return acc;
    }
    
    /**
     * Az ügyfél adatainak ellenőrzése. Csak akkor lehet az adatbázisba menteni
     * ha nincs benne hiba.
     * @return 
     */
    @Override
    public boolean validate() {
        String svals[] = new String[] {
            name, private_id, city, address, password
        };
        String scols[] = new String[] {
            "name", "private_id", "city", "address", "password"
        };
        String se_errors[] = new String[] {
            "A név nem lehet üres", "A személyi/cég azonosító nem lehet üres", 
            "A város nem lehet üres", "A cím nem lehet üres", "A jelszó nem lehet üres"
        };
        failIfStringsEmpty(svals, scols, se_errors);
        failForIf(postal_code == 0, "postal_code", "Az irányítószám nem lehet üres");
        failForIf(postal_code < 0, "postal_code", "Az irányítószám nem lehet negatív");
        failForIf(office_id == 0, "office_id", "A fiókot meg kell adni");
        failForIf(password != null && password.length() < 6, "password", "A jelszónak hosszabbnak kell lennie 6 karakternél");
        /* Ha nem lett új hiba hozzáadva a rekord jó */
        return !hasErrors();
    }
    
    /* ========~~~~--+ PUBLIKUS LEKÉRDEZŐ METÓDUSOK +--~~~~======== */
    
    public String getName()         { return this.name; }
    public String getPrivateId()    { return this.private_id; }
    public String getCity()         { return this.city; }
    public String getAddress()      { return this.address; }
    public int    getPostalCode()   { return this.postal_code; }
    public boolean isAdmin()        { return this.is_admin; }
    public Timestamp getCreatedAt() { return this.created_at; }
    
    /**
     * A fiókobjektumot adja vissza.
     * Csak akkor kell az adatbázisban a pontos fiókot lekérdezni, ha 
     * explicite meghívjuk ezt a függvényt. A this.office emiatt általában null.
     * @return
     * @throws SQLException
     */
    public Office getOffice() throws SQLException{
        if (this.office == null) {
            this.office = Office.findById(this.office_id);
        }
        return this.office;
    }
    
    /**
     * Az ügyfél számláit adja vissza.
     * @return
     */
    public List<Account> getAccounts() throws SQLException {
        return Account.findByCustomer(this);
    }
    
    public int getAmount() {
        int amount = 0;
        try {

            List<Account> accs = getAccounts();
            for (Account acc : accs) {
                amount += acc.getAmount();
            }
        } catch (SQLException ex) {
            Logger.getLogger(Customer.class.getName()).log(Level.SEVERE, null, ex);
        }
        return amount;
    }
    
    /* ========~~~~--+ PUBLIKUS BEÁLLÍTÓ METÓDUSOK +--~~~~======== */
    /* Mivel minden set*() metódus Customer-el tér vissza így azok láncolhatóak
     * Példa:
     * Customer c = new Customer();
     * c.setName("Trab Antal").setPrivateId("492384JA").setCity("Szeged"); // ...
    */
    /**
     * A nevet állítja be. Ha az változott a rekordot frissíteni kell.
     * @param name név
     * @return ezen objektum láncoláshoz
     */
    public Customer setName(String name) {
        checkForChange(COLUMN_NAME, this.name, name);
        this.name = name;
        return this;
    }
    
    public Customer setPrivateId(String privid) {
        checkForChange(COLUMN_PRIVATE_ID, this.private_id, privid);
        this.private_id = privid;
        return this;
    }
    
    public Customer setOffice(Office off) {
        setOffice(off.getId());
        this.office = off;
        return this;
    }
    
    public Customer setOffice(int office_id) {
        checkForChange(COLUMN_OFFICE_ID, this.office_id, office_id);
        this.office_id = office_id;
        return this; 
    }
    
    public Customer setCity(String city) {
        checkForChange(COLUMN_CITY, this.city, city);
        this.city = city;
        return this;
    }
    
    public Customer setAddress(String address) {
        checkForChange(COLUMN_ADDRESS, this.address, address);
        this.address = address;
        return this;
    }
    
    public Customer setAdmin(boolean admin) {
        checkForChange(COLUMN_IS_ADMIN, this.is_admin, admin);
        this.is_admin = admin;
        return this;   
    }
    
    public Customer setPostalCode(int pcode) {
        checkForChange(COLUMN_POSTAL_CODE, this.postal_code, pcode);
        this.postal_code = pcode;
        return this;
    }
    
    /**
     * Új ügyfél esetén ezt kell hívni, hogy a jelszó megerősítés is ellenőrizhető legyen.
     * @param pass jelszó
     * @param pass_confirm jelszó megerősítése
     * @return
     */
    public Customer setPassword(String pass, String pass_confirm) {
        failForIf(pass_confirm == null, "password_confirm", "A jelszómegerősítést ki kell tölteni");
        failForIf(pass != null && pass_confirm != null && !pass.equals(pass_confirm)
                , "password_confirm", "A jelszavak nem egyeznek meg");
        
        checkForChange(COLUMN_PASSWORD, this.password, pass);
        this.password = pass;
        return this;
    }
    
    public Customer setPassword(String pass) {
        return setPassword(pass, pass);
    }
     
    /**
     * Létrehozás beállítása (seedeléshez).
     * @param t létrehozási idő
     * @return ügyfél
     */
    public Customer setCreatedAt(Timestamp t) {
        checkForChange(COLUMN_CREATED_AT, this.created_at, t);
        this.created_at = t;
        return this;
    }
    
    /* ========~~~~--+ MEZŐLEKÉRDEZŐ/BEÁLLÍTÓ FÜGGVÉNYEK +--~~~~======== */
    
    /**
     * A jelenlegi értékek visszaadása String-ként a BaseModel-nek.
     * @return 
     */
    @Override
    public String[] getValues() {
        return new String[] {
            name, private_id, city, address, Integer.toString(postal_code), TM_FORMAT.format(created_at), 
            Boolean.toString(is_admin), password, Integer.toString(office_id)
        };
    }

    /**
     * Táblából való feltöltés esetén.
     * @param rs
     * @throws SQLException 
     */
    @Override
    public void setValues(ResultSet rs) throws SQLException {
        is_new = false;
        id          = rs.getInt("id");
        /* Az oszlopok nevei már benne vannak a COLUMN_NAMES-ben */
        name        = rs.getString(COLUMN_NAMES[0]);
        private_id  = rs.getString(COLUMN_NAMES[1]);
        city        = rs.getString(COLUMN_NAMES[2]);
        address     = rs.getString(COLUMN_NAMES[3]);
        postal_code = rs.getInt(COLUMN_NAMES[4]);
        created_at  = rs.getTimestamp(COLUMN_NAMES[5]);
        is_admin    = rs.getBoolean(COLUMN_NAMES[6]);
        password    = rs.getString(COLUMN_NAMES[7]);
        office_id   = rs.getInt(COLUMN_NAMES[8]);
    }

    /**
     * Létrehozás idejének frissítése.
     */
    @Override
    public void updateCreatedAt() {
        if (this.created_at == null) {
            this.created_at = Timestamp.valueOf(LocalDateTime.now());
        }
    }
}

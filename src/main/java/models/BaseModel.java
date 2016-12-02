package models;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import static java.sql.Statement.RETURN_GENERATED_KEYS;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * Minden adatbázis modell szülőosztálya, a fő SQL-vudu és mágia helye.
 * Feladata az adatbázisban található táblák Java osztályokra való leképezésének megkönnyítése.
 * A BaseModel hozza létre a legtöbbször használt SQL lekéréseket, pl: (INSERT/UPDATE/DELETE/SELECT)
 * az adott modellhez.
 * 
 * Ahhoz, hogy ez sikerüljön a következőknek kell teljesülnie:
 * 
 *  1, A tábla első oszlopa az 'id' nevű elsődleges kulcs, ami automatikusan növekszik
 *  2, A modellosztály nevének egyeznie kell a tábla nevével
 *  3, A modellosztálynak származnia kell a BaseModel-ből
 *  4, A származtatott osztálynak meg kell hívnia a BaseModel konstruktorát a táblájában
 *      található oszlopok neveivel, (tömbben felsorolva)
 *  5, Az így kapott osztálynak meg kell valósítania a BaseModel abstract metódusait
 * 
 * Kötelezően meg kell valósítani:
 *  o getValues() metódust, ami egy String tömbben adja vissza az osztály jelenlegi adattagjait.
 *    Ehhez nyilván mindent megfelőlen String-gé kell alakítani
 * 
 *  o setValues(ResultSet) metódust, amivel az osztály a Java ResultSet objektumából tölti fel magát
 *    az adatbázisban található értékekkel
 * 
 *  o validate() függvényt, ami igazat ad vissza ha a mezők nem tartalmaznak hibát, tehát
 *    ha a rekordot le lehet menteni
 * 
 * @author akos
 */
public abstract class BaseModel {
    protected int     id;             // Az id mező
    protected boolean is_new;         // Igaz, ha az adott objektum még nincs rekordként mentve
    protected boolean has_created_at  = true;
    protected Map<String, ArrayList<String>> errors          = null; // hibalista
    protected ArrayList<Integer>             columns_changed = null; // megváltozott mezők
    protected boolean                        is_changed;
    protected static final SimpleDateFormat  TM_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    protected static final SimpleDateFormat  DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    
    private final String[]    column_names;
    private static Connection db_connection;
    private static boolean    is_sql_debugged = true;

    /**
     * Csatlakozás az adatbázishoz
     * @param dbPath az adatbázis elérési útja
     * @param schemaPath az adatbázis sémája
     * @return igaz ha a csatlakozás sikeres
     */
    public static boolean dbConnect(String dbPath, String schemaPath) {
        return dbConnect(dbPath, schemaPath, "SA", "");
    }
    
    /**
     * Csatlakozás az adatbázishoz a megadott felhasználónévvel és jelszóval
     * @param dbPath az adatbázis elérési útja
     * @param schemaPath az adatbázis sémája
     * @param username felhasználónév
     * @param pass jelszó
     * @return igaz ha a csatlakozás sikeres
     */
    public static boolean dbConnect(String dbPath, String schemaPath, String username, String pass) {
        try {
            Class.forName("org.hsqldb.jdbc.JDBCDriver");
        } catch (ClassNotFoundException ex) {
            /* TODO: err */
            Logger.getLogger(BaseModel.class.getName()).log(Level.SEVERE, null, ex);

            return false;
        }
        try {
            db_connection = DriverManager.getConnection(
                    "jdbc:hsqldb:" + dbPath + ";ifexist=true", username, pass);
        } catch (SQLException ex) {
            Logger.getLogger(BaseModel.class.getName()).log(Level.SEVERE, null, ex);

            return false;
        }
        
        return true;  
    }
    
    public static boolean dbClose() {
        if (db_connection != null) {
            try {
                db_connection.commit();
                db_connection.close();
            } catch (SQLException ex) {
                Logger.getLogger(BaseModel.class.getName()).log(Level.SEVERE, null, ex);
                return false;
            }
        }
        return true;
    }
    
    public static Connection getConnection() { return db_connection; }
    public static boolean setSchema(String schema_file) {
        FileReader fr = null;
        try {
            fr = new FileReader(schema_file);
            BufferedReader br = new BufferedReader(fr);
            
            String schema = new String(Files.readAllBytes(Paths.get(schema_file)), StandardCharsets.UTF_8);
            System.out.println(schema);

            if (db_connection == null) {
                throw new Exception();
            }
            Statement stmt = db_connection.createStatement();
            stmt.execute(schema);
            return true;
        } catch (Exception ex) {
            Logger.getLogger(BaseModel.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                fr.close();
            } catch (IOException ex) {
                Logger.getLogger(BaseModel.class.getName()).log(Level.SEVERE, null, ex);
            }
            return false;
        }
    }
    /* ========~~~~--+ PUBLIKUS PÉLDÁNYMETÓDUSOK +--~~~~======== */

    /**
     * A BaseModel konstruktorát kötelező meghívni az adott modell oszlopneveit
     * tartalmazó tömbjével.
     * @param col_names oszlopok nevei
     */
    public BaseModel(String[] col_names) {
        this.column_names = col_names;
        this.has_created_at = Arrays.asList(col_names).contains("created_at");  
    }
        
    /**
     * Az oszlopok neveit adja vissza az adott táblához
     * @return sztring tömb
     */
    public String[] getColumnNames() {
        return column_names;
    }
    
    /**
     * Létrehozza az oszlopnevekhez tartozó SQL utasításrészt.
     * Például:
     * Ha getColumnNames() == { "name", "city", "address" }; akkor a
     * getColumnString() == "(name, city, address)"
     * 
     * [Belső használatra]
     * @return SQL lekérdezés részlet String-ként
     */
    public String getColumnString() {
        String cs = new String();
        cs += "(";
        String cols[] = getColumnNames();
        for (int i = 0; i < cols.length; i++) {
            cs += cols[i] + ((i+1 == cols.length) ? ")" : ", ");
        }
        return cs;
    }
    
    /**
     * A PreparedStatement számára állít elő kérdőjeleket.
     * A biztonságos lekérdezéshez PreparedStatement-et kell használni, ami a behelyettesítést
     * csak a paraméterek ellenőrzése után teszi meg. A paraméterek helyére viszont kérdőjeleket
     * kell rakni. Mivel a BaseModel ismeri az oszlopok számát, így ezt elő tudja állítani.
     * [Belső használatra]
     * Példa:
     * Ha getColumnNames() == { "name", "city", "address" }; akkor a
     * getPrepareString() == "(?, ?, ?)" lesz
     * @param n az oszlopok száma
     * @return
     */
    protected String getPrepareString(int n) {
        String vs = new String();
        vs += "(";
        for (int i = 0; i < n; i++) {
            vs += "?" + ((i+1 == n) ? ")" : ", ");
        }
        return vs;
    }
    
    /**
     * Kötelezően túlterhelendő lekérdező metódus.
     * A BaseModel innen fogja tudni, hogy az osztály jelenleg pontosan milyen
     * adatokat tartalmaz az adott mező helyén. A save() ezt fogja használni mentéskor.
     * A Java nyelv nem túl okos, így sajnos mindent előre String-gé kell konvertálni.
     * Az értékeket mindig az "id" nélkül a BaseModel számára előre átadott oszlopnév
     * tömbbel megegyező sorrendben kell visszadni. Tehát, az értékeknek az adott 
     * táblával megeggyező mezőhelyén kell állniuk, de az id oszlop nélkül.
     * Például
     * <pre><code>
     * Office off = new Office("Budapest", "Váradi út 11", 1111);
     * String[] vals = off.getValues();
     * vals == { "Budapest", "Váradi út 11", "1111" };
     * </pre></code>
     * A BaseModel így fogja tudni lementeni a rekordot.
     * @return a jelenlegi mezőket tartalmazó sztring tömb
     */
    public abstract String[] getValues();

    /**
     * Kötelezően túlterhelendő feltőltő metódus.
     * Amikor a táblából kell feltölteni az adott Java objektumot, a BaseModel-nek
     * szüksége van arra, hogy a modelek saját maguk képesek legyenek beállítani
     * az adattagjaikat. Az adatbázisból a rekordokat a Java ResultSet objektumban adja vissza.
     * Ennek metódusaival lehet lekérdezni a rekord jelenlegi adatait és
     * beállítani a modell adattagjait. Logikusan a setValues() végén az is_new  és is_changed
     * változót false-ra kell állítani, hiszen a rekord így már biztosan szerepel a táblázatban.
     * @param rs a rekordot tartalmazó objektum
     * @throws SQLException
     */
    public abstract void setValues(ResultSet rs) throws SQLException;
    
    /**
     * Hiba adása az adott mezőre.
     * Mentés előtt szükség van arra, hogy az adott model-t ellenőrizzük. Ehhez
     * túl kell terhelni a validate() függvényt. A validate()-nek a felhasználó
     * számára használható hibaüzeneteket kell létrehoznia a failFor() függvény
     * segítségével. A hiba "lista" tartalmazza, hogy milyen mezőkhöz, milyen hibák tartoznak.
     * @param column a hibás mező
     * @param error_msg maga a hiba
     */
    protected void failFor(String column, String error_msg) {
        if (this.errors == null) {
            this.errors = new HashMap<>();
        }
        ArrayList<String> cerrs;
        if (errors.containsKey(column)) {
            cerrs = this.errors.get(column);
        } else {
            cerrs = new ArrayList<>();
            this.errors.put(column, cerrs);
        }
        cerrs.add(error_msg);
    }
        
    /**
     * A failForIf() ellentettje.
     * @param expr kifejezés vagy boolean változó
     * @param column hibás mező
     * @param error_msg hiba
     */
    protected void failForIfNot(boolean expr, String column, String error_msg) {
        failForIf(!expr, column, error_msg);
    }
    
    /**
     * A hibát csak feltételesen (ha az első paraméter igaz) hozza létre.
     * Az első paraméter általában egy rövid kifejezés, például:
     * <pre><code>
     * failForIf(password.length < 6, "password", "A jelszónak hosszabbnak kell lennie 6-nál");
     * </code></pre>
     * @param expr
     * @param column
     * @param error_msg
     */
    protected void failForIf(boolean expr, String column, String error_msg) {
        if (expr) {
            failFor(column, error_msg);
        }
    }
    
    /**
     * hibák létrehozása üres mezőknél.
     * A legtöbbszőr előforduló hiba a mező üressége.
     * Emiatt String-eknél érdemes több mezőt egyszerre ellenőrizni. 
     * Ezeket egy tömbben adjuk meg. Úgyanígy teszünk a mezők neveivel és azok pontos
     * hibaszövegével.
     * @param values azok a String-gek amiknek nem kellene üresnek lenniük
     * @param columns ezen String-gek mezőinek nevei
     * @param msgs a hibák pontos leírása
     */
    protected void failIfStringsEmpty(String[] values, String[] columns, String[] msgs) {
        int i;
        for (i = 0; i < columns.length; i++) {
            failForIf(values[i] == null || values[i].isEmpty(), columns[i], msgs[i]);
        }
    }

    /**
     * Igazat ad ha az objektumban hibák vannak. Tehát ha a rekord nem menthető
     * azaz a validáció hibát ad (validate() == false).
     * @return
     */
    public boolean hasErrors() {
        return errors != null && !errors.isEmpty();
    }
    
    /**
     * Az adott mezők hibáinak lekérése
     * @param name Mezőnév
     * @return null ha nem hibás, String lista egyébként
     */
    public ArrayList<String> errorsFor(String name) {
        if (this.errors != null) {
            return this.errors.get(name);
        }
        return null;
    }
    
    /**
     * Hibás-e az adott mező?
     * @param name Mezőnév
     * @return
     */
    public boolean hasErrorsFor(String name) {
        return errorsFor(name) != null;
    }
    
    /**
     * Hibalista lekérése.
     * Egy mezőhöz több hiba is tartozhat.
     * @return Hash a mezőkhöz rendelt hibákkal.
     */
    public Map<String, ArrayList<String>> getErrors() { return this.errors; }
    
    public String getTableName() {
        return this.getClass().getSimpleName();
    }
    
    /**
     * A mezők jelnlegi értékeinek felsroloása
     * @return Kimeneti String
     */
    @Override
    public String toString() {
        String res = new String();
        String cols[] = getColumnNames();
        String vals[] = getValues();
        for (int i = 0; i < cols.length; i++) {
            res += cols[i] + " = '" + vals[i] + "'\n";
        }
        return res;
    }
    
    /**
     * A rekord mentése.
     * Amennyiben új rekordot hozunk létre, a save() INSERT-et készít, viszont
     * ha meglévő rekordot változtattunk UPDATE SQL lekérdezést futtatt le.
     * Ahhoz, hogy ezt tudja, a változtattot mezőket az changed_columns tömb 
     * sorolja fel.
     * @return igaz, ha a rekordot mentetésre került
     */
    public boolean save() {
        String sqlStr = new String();
        if (!validate()) {
            return false;
        }
        
        if (isNew()) {
            // INSERT INTO Tábla (...) VALUES (...); előállítása
            updateCreationTime();
            sqlStr += "INSERT INTO " + getTableName() + " ";
            sqlStr += getColumnString();
            sqlStr += " VALUES ";
            sqlStr += getPrepareString(getColumnNames().length) + ";";
        } else if (!isNew() && isChanged()) {
            /* TODO */
        }
        try {
            debugSql(sqlStr);
            PreparedStatement stmt = db_connection.prepareStatement(sqlStr, RETURN_GENERATED_KEYS);
            String cols[] = getValues();
            for (int i = 0; i < cols.length; i++) {
                stmt.setString(i+1, cols[i]);
            }
            int affec = stmt.executeUpdate();
            if (affec == 0) {
                return false;
            } else if (affec == 1) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    this.id = rs.getInt(1);
                }
            }

            is_new     = false;
            is_changed = false;
        } catch (SQLException ex) {
            Logger.getLogger(BaseModel.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        
        if (isChanged()) {
            return true;
        }
        return true;
    }
    
    /**
     * A rekordok ellenőrzéséért felel, definiálása kötelező.
     * A modellek validate() függvényében kell leellenőrízni az aktuális mezők
     * helyességét. A hibák létrehozásához az failFor(), failForIf(), failForIfNot()
     * és a failIfStringsEmpty() függvények használhatóak.
     * Ha a validate() false-ot ad vissza a save() nem fogja menteni a rekordot.
     * @return
     */
    public abstract boolean validate();
    
    /**
     * A jelen azonosító lekérdezése. Minden táblának tartalmaznia kell id mezőt.
     * @return
     */
    public int getId() {
        return this.id;
    }
    
    /**
     * Igazat ad ha új rekord lett létrehozva.
     * Például:
     * <pre><code>
     * Office off = new Office("Budapest", "Váradi út 11", 1111);
     * off.isNew() == true
     * </code></pre>
     * @return
     */
    public boolean isNew() {
        return is_new;
    }
    
    /**
     * Igazat ad ha új rekord meg lett változtatva.
     * Például:
     * <pre><code>
     * Office off = Office.findById(1); // id = 1 rekord
     * off.isNew() == false     // az adatbázisból vettük, biztosan nem új
     * off.isChanged() == false // még nem változtattunk rajta
     * off.setCity("Budapest");
     * off.isChanged() == true  // átírtuk a várost, frissíteni kell a táblát
     * off.save() == true       // UPDATE fog lefutni
     * </code></pre>
     * @return
     */
    public boolean  isChanged() {
        return columns_changed != null && !columns_changed.isEmpty(); 
    }
    
    /**
     * Minden set*() metódusból meg kell hívni.
     * A BaseModel így fogja ellenőrizni, hogy a rekord változott-e.
     * Meg kell neki adni az oszlop sorszámát, az ediggi értéket és az újat.
     * Ha nincs változás, tehát a kettő megegyezik, vagy a régi adattag eddig null volt
     * az isChanged() false-ot ad vissza.
     * @param col az oszlop sorszáma
     * @param oobj a régi adattag
     * @param nobj a kapott új érték
     * @return igaz ha volt változás
     */
    protected boolean checkForChange(int col, Object oobj, Object nobj) {
        boolean changed = (oobj == null) || (oobj != null && !oobj.equals(nobj));
        if (changed) {
            if (this.columns_changed == null) {
                this.columns_changed = new ArrayList<>();
            }
            columns_changed.add(col);
        }
        return changed;
    }
    
    /**
     * Ha a modell rendelkezik created_at mezővel, azt beillesztéskor
     * a jelenlegi idővel kell frissíteni.
     * Ehhez meg kell hívni az updateCreatedAt() függvényt, amit a created_at-al
     * rendelkező modelleknek túl kell terhelnie.
     */
    protected void updateCreationTime() {
        if (has_created_at) {
            updateCreatedAt();
        }
    }
    
    /**
     * Új azonosító adása
     * @param nid az új azonosító
     * @return
     */
    public BaseModel setId(int nid) {
        checkForChange(1, this.id, nid);
        this.id = nid;
        return this;
    }
    
    /**
     * SQL hibakereső dump függvény. Minden SQL hívást kiír a konzolra ha az 
     * SQL debuggolás be van kapcsolva.
     * @param sql 
     */
    private static void debugSql(String sql) {
        if (is_sql_debugged) {
            System.out.println("SQL exec: '" + sql + "'");
        }
    }
    
    /**
     * SQL debuggolás ki/bekapcsolása
     * @param in_debug
     */
    public static void setSqlDebug(boolean in_debug) { 
        is_sql_debugged = in_debug;
    }
    
    /**
     * Van-e created_at mező
     * @return igaz ha létezik a created_at
     */
    public boolean hasCreatedAt() { return has_created_at; }

    /**
     * Akkor kell túlterhelni ha a modell tartalmaz created_at-ot.
     */
    public void updateCreatedAt() { /* OVERLOADED */ };

    /**
     * Segédfüggvény a findBy*() statikus metódusok számára.
     * Mivel a Java egy primitív, bunkó nyelv ezért nem tartalmaz statikus polimorfizmust.
     * A BaseModel nem tud rájönni pontosan milyen modellből hívták a findBy*() függvényét.
     * Emiatt minden modellneknek magának kell a saját findById(), findByStb() keresőfüggvényeit
     * definiálnia. Hogy ez könyebb legyen a modellek haszálhatják ezt a segédfüggvényt, az előbbi
     * metódusok megvalósítására.
     * A tábla neve megadása után az oszlopok neveinek tömbje, majd a az ezekben keresett
     * értékek tömbjét kell megadni. A visszatérési érték a kapott rekord lista ResultSet-ben.
     * Példa:
     * <pre><code>
     * String cols[] = new String[] { "city", "postal_code" };
     * String vals[] = new String[] { "Szeged", "9000" };
     * ResultSet rs = find("Office", cols, vals);
     * // Létrejön és lefut a következő SQL utasítás:
     * // SELECT * FROM Office WHERE city = 'Szeged' OR postal_code = '9000';
     * if (rs.next()) {
     *  Office off = new Office();
     *  // az első rekord betöltés a táblából a modellbe
     *  off.setValues(rs);
     * }
     * </code></pre>
     * @param table_name a tábla neve
     * @param colnames az oszlopok ami alapján keresünk
     * @param vals az értékek amiket keresünk
     * @param limit maximális rekordszám
     * @return az rekordotkat tartalmazó ResultSet
     */
    protected static ResultSet find(String table_name, String colnames[], String vals[], int limit) {
        ResultSet rs = null;
        String sqlStr = new String();
        /* Minden kell a táblából */
        sqlStr += "SELECT * FROM " + table_name;
        /* Ha nincsenek megadva oszlopok és értékek, akkor lassan itt a végem
           mert feltétel nélkül kell az összes (ha limit = 0) */
        if (colnames != null && vals != null) {
            sqlStr += " WHERE ";
            for (int i = 0; i < colnames.length; i++) {
                /* Kérdőjelek a feltételeknek (SQL injection miatt) */
                sqlStr +=  colnames[i] + " = ?";
                /* Csak több feltétel esetén kell OR */
                if (i+1 != colnames.length) {
                    sqlStr += " OR ";
                }
            }
        }
        /* Ha van limit írjuk a végére */
        if (limit > 0) {
            sqlStr += " LIMIT " + limit;
        }
        sqlStr += ";";
        try {
            /* lekérdezés előkészítése */
            debugSql(sqlStr);
            PreparedStatement stmt = db_connection.prepareStatement(sqlStr);
            if (vals != null) {
                for (int i = 0; i < vals.length; i++) {
                    /* Az értékek hozzárendelése a kérdőjeles helyekre */
                    stmt.setString(i+1, vals[i]);
                }
            }
            rs = stmt.executeQuery();
        } catch (SQLException ex) {
            Logger.getLogger(BaseModel.class.getName()).log(Level.SEVERE, null, ex);
        }
        return rs;
    }
    
    /**
     * Ugyanaz mint a másik find() csak a limit=0, tehát nincs
     * @param table_name a tábla neve
     * @param cols az oszlopok ami alapján keresünk
     * @param vals az értékek amiket keresünk
     * @return az rekordotkat tartalmazó ResultSet
     */
    protected static ResultSet find(String table_name, String cols[], String vals[]) {
        return find(table_name, cols, vals, 0);
    }
    
    /**
     * Segédfüggvény a findBy*() statikus metódusok számára.
     * Mivel a Java egy primitív, bunkó nyelv ezért nem tartalmaz statikus polimorfizmust.
     * A BaseModel nem tud rájönni pontosan milyen modellből hívták a findBy*() függvényét.
     * Emiatt minden modellneknek magának kell a saját findById(), findByStb() keresőfüggvényeit
     * definiálnia. Hogy ez könyebb legyen a modellek haszálhatják ezt a segédfüggvényt, az előbbi
     * metódusok megvalósítására.
     * A tábla neve megadása után az oszlopok neveinek tömbje, majd a az ezekben keresett
     * értékek tömbjét kell megadni. A visszatérési érték a kapott rekord lista ResultSet-ben.
     * A find()-al ellentétben ennek elég csak egy oszlopot és értéket megadni, nem kell tömb.
     * Példa:
     * <pre><code>
     * ResultSet rs = find("Office", "city", "Szeged");
     * // Létrejön és lefut a következő SQL utasítás:
     * // SELECT * FROM Office WHERE city = 'Szeged';
     * if (rs.next()) {
     *  Office off = new Office();
     *  // az első rekord betöltés a táblából a modellbe
     *  off.setValues(rs);
     * }
     * </code></pre>
     * @param table_name a tábla neve
     * @param colname az oszlop ami alapján keresünk
     * @param val az érték amit keresünk
     * @param limit maximális rekordszám
     * @return az rekordotkat tartalmazó ResultSet
     */   
    protected static ResultSet findSingle(String table_name, String colname, String val, int limit) {
        return find(table_name, new String[] { colname }, new String[] { val }, limit);
    }
    
    /**
     * Ugyanaz mint az előző findSinge() csak a limit=0.
     * @param table_name a tábla neve
     * @param colname az oszlop ami alapján keresünk
     * @param val az érték amit keresünk
     * @return a rekordokat tartalmazó ResultSet
     */
    protected static ResultSet findSingle(String table_name, String colname, String val) {
        return findSingle(table_name, colname, val, 0);
    }
}

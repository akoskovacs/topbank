import controllers.AccountController;
import controllers.BaseController;
import controllers.CustomerController;
import controllers.OfficeController;
import controllers.SessionController;
import controllers.TransactionController;
import static spark.Spark.*;
import spark.template.freemarker.FreeMarkerEngine;
import freemarker.template.*;
import java.io.*;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import models.BaseModel;
import models.Customer;
import models.Account;
import models.Office;
import models.Card;
import models.Transaction;
import spark.ModelAndView;

public class Main {

    private static final String DB_FILE = "file:db/bank.db";
    private static final String DB_SCHEMA = "db/bank_schema.sql";
    private static Configuration viewConfig;
    private static FreeMarkerEngine viewEngine;

    private static Timestamp randomizedTime() {
        long offset = Timestamp.valueOf("2010-01-01 00:00:00").getTime();
        long end = Timestamp.valueOf("2016-11-01 00:00:00").getTime();
        long diff = end - offset + 1;
        Timestamp rand = new Timestamp(offset + (long)(Math.random() * diff));
        return rand;
    }
    
    public static void seed() {
        Office off[] = new Office[10];
        off[0] = new Office("Budapest", "Szépvölgyi út 4/b.", 1025);
        off[1] = new Office("Budapest", "Móricz Zsigmond körtér 18.", 1117);
        off[2] = new Office("Győr", "Bartók Béla út 53/b.", 9024);
        off[3] = new Office("Szeged", "Aradi vértanúk tere 3.", 6720);
        off[4] = new Office("Szeged", "Vértói út 1.", 6724);
        off[5] = new Office("Debrecen", "Piac utca 45-47.", 4025);
        off[6] = new Office("Debrecen", "Hatvan utca 2.", 4025);
        off[7] = new Office("Pécs", "Rákóczi út 1.", 7621);
        off[8] = new Office("Nyíregyháza", "Sóstói utca 31/b.", 4400);
        off[9] = new Office("Miskolc", "Árpád utca 2.", 3534);
        for (Office o : off) {
            o.save();
        }
        
        Customer cust[] = new Customer[20];
        final String PASS = "valami";
        cust[0] = new Customer("Nagyné Szepesi Ilonka", "93284JA", "Budapest", "Sasadi utca 10", 9000, true, PASS);
        cust[1] = new Customer("Szabó István", "44452AA", "Budapest", "Török utca 1", 8840, false, PASS);
        cust[2] = new Customer("Szabó István", "44452AA", "Budapest", "Török utca 1", 8840, false, PASS);
        cust[3] = new Customer("Erdei Tamás", "44424JA", "Szeged", "Bécsi körút 31", 6722, false, PASS);
        cust[4] = new Customer("Nagy Csaba", "11276BA", "Szeged", "Sólyom utca 22", 6726, false, PASS);
        cust[5] = new Customer("Kiss László", "11556BA", "Szeged", "Szilléri sugárút 44", 6723, true, PASS);
        cust[6] = new Customer("Eötvös Erzsébet", "22356BB", "Budapest", "Debreceni út 11", 9112, false, PASS);
        cust[7] = new Customer("Kalmár József", "44223BA", "Budapest", "Eötvös utca 2", 1044, false, PASS);
        cust[8] = new Customer("Zente Ferenc", "13356BA", "Pécs", "Nagytétényi út 22", 7621, false, PASS);
        cust[9] = new Customer("Kiss Imre", "99556JA", "Szeged", "Retek utca 8", 6723, false, PASS);
        cust[10] = new Customer("Kovács Dóra", "10056DD", "Szeged", "Szilléri sugárút 2", 6723, false, PASS);
        cust[11] = new Customer("Sánta Imre", "923872AA", "Győr", "Istenes út 12", 6723, false, PASS);
        cust[12] = new Customer("Szepesi Ferenc", "822342DA", "Miskolc", "Benedek utca 91", 3534, false, PASS);
        cust[13] = new Customer("Nemzeti Adó és Vámhivatal", "NAV/19121", "Budapest", "Rákóczi út 11.", 1111, false, PASS);
        Random r = new Random();
        for (int ci = 0; ci < 14; ci++) {
            Customer c = cust[ci];
            c.setOffice(off[r.nextInt(off.length-1)]);
            c.setCreatedAt(randomizedTime());
            c.save();
            
            for (int i = 0; i < r.nextInt(2)+1; i++) {
               Account acc = c.newAccount();
               acc.setCreditInterest(r.nextFloat());
               acc.setCreditLoan(r.nextInt(100000));
               acc.setFee(r.nextInt(1000));
               acc.setCreatedAt(randomizedTime());
               acc.save();
               for (int j = 0; j < r.nextInt(3); j++) {
                   Card cr = acc.newCard();
                   cr.setCardType(r.nextInt(Card.CARD_TYPE_NAMES.length));
                   cr.setCcv2(r.nextInt(1000));
                   cr.setFee(r.nextInt(1000));
                   cr.setPin(r.nextInt(10000));
                   cr.setExpiresAt(Timestamp.valueOf("2020-10-10 12:00:00"));
                   cr.setCreatedAt(randomizedTime());
                   cr.save();
               }
           }   
        }
        
        Timestamp times[] = new Timestamp[10];
        for (int i = 1; i < times.length; i++) {
            String fmt = String.format("2016-%02d-%02d 13:34:11", i, r.nextInt(27)+1);
            times[i-1] = Timestamp.valueOf(fmt);
        }
        /* Pénzosztás */
        List<Account> accs = null;
        try {
            accs = Account.getAll(0);
        } catch (SQLException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        for (Account acc : accs) {
            int money = r.nextInt((int)2e6); // 2 millió max
            
            Transaction mt = new Transaction(money, "Befizetés", "Banki befizetés", null, acc);
            mt.setCreatedAt(acc.getCreatedAt());
            mt.save();
            /* Szétosztjuk a pénzt szegény árva számlák között */
            for (int i = 0; i < r.nextInt(15) && money >= 0; i++) {
                int sm = r.nextInt(money/4);
                int payee = r.nextInt(accs.size());
                money -= sm;
                Transaction t = acc.newTransaction();
                t.setAmount(sm);
                t.setPayeeAccount(accs.get(payee));
                t.setCreatedAt(times[i]);
                t.save();
            }
        }
    }

    public static void main(String[] args) {
        if (args.length == 1) {
            if (args[0].equals("--seed")) {
                initDb();
                seed();
                BaseModel.dbClose();
            } else if (args[0].equals("--create-db")) {
                initDb();
                BaseModel.setSchema(DB_SCHEMA);
                BaseModel.dbClose();
            } else if (args[0].equals("--help")) {
                System.out.println("OnlineBank használat: OnlineBank [opciók]\n");
                System.out.println("opciók:");
                System.out.println("\t--seed\tAdatbázis feltöltése adatokkal");
                System.out.println("\t--create-db\tAdatbázis létrehozása");
                System.out.println("\t--help\tEz a segédüzenet");
            } else {
                System.out.println("Ismeretlen operandus");           
            }
            return;
        }
        
        /* Statikus fájlok helye (css, js, statikus html) */
        staticFiles.location("/public");
        /* Nézetkönyvtár beállítása */
        initViews("views");

        /* Kivétel érkezésekor a hívott függvények kiíródnak a konzolra */
        exception(Exception.class, (e, req, res) -> e.printStackTrace());

        /* Adatbázis inicializálása */
        initDb();

        /* HTTP vezérlők forgalomírányítása */
        initRouter();
    }

    private static void initViews(String viewDirName) {
        viewConfig = new Configuration();
        try {
            viewConfig.setDirectoryForTemplateLoading(new File(viewDirName));
            /* Ne legyen lokális a számformátum (rendes megjelenítés */
            viewConfig.setSetting("number_format", "computer");
        } catch (Exception e) {
            System.err.println("A '" + viewDirName + "' nézetkönyvtár nem található!");
            e.printStackTrace();
        }
        viewEngine = new FreeMarkerEngine(viewConfig);
    }

    private static void initDb() {
        BaseModel.dbConnect(DB_FILE, DB_SCHEMA);
        //seed();
    }

    /* A forgalomírányítás beállítása:
     A get(), post(), stb... függvények az azonos nevű HTTP lekérésnek felelnek meg.
     Az első paraméterük az elérési út (URI)
     A második egy lambda függvény ami egy Request-tet és egy Response-t vár. Ezekben vannak a
     lekérdezés és a válasz adatai.
     A harmadik opcionális. Ide egy sablon feldolgozó adható meg.
     */
    private static void initRouter() {
        /* A felhasználók kezeléséhez szükség van arra, hogy az aktuális kérés és
         válasz objektumokat letároljuk. Ez a filter minden lekérés előtt lefut és
         frissíti ezen objektumokat.
         */
        before((req, res) -> {
            BaseController.setRequestAndResponse(req, res);
        });
        
        after((req, res) -> {
            BaseController.saveFlash();
        });

        get("/", SessionController.index, viewEngine);
        /* login űrlap megjelenítése */
        get(SessionController.LOGIN_PAGE, SessionController.login, viewEngine);

        /* A login űrlaptól kapott (POST) adatok ellenőrzése és beléptetés */
        post(SessionController.LOGIN_PAGE, SessionController.authenticate);
        get(SessionController.LOGOUT_PAGE, SessionController.logout);
        /* TODO */

        get("/customer/all", CustomerController.all, viewEngine);
        get("/customer/new", CustomerController.newCustomer, viewEngine);
        post("/customer/new", CustomerController.saveNewCustomer, viewEngine);
        get("/profile", CustomerController.profile, viewEngine);
        get("/customer/:cid", CustomerController.show, viewEngine);
        
        get("/account/:aid", AccountController.show, viewEngine);
        
        get("/customer/:cid/transaction/new", TransactionController.newTransactionFor, viewEngine);
        post("/customer/:cid/transaction/new", TransactionController.save, viewEngine);
        get("/transaction/new", TransactionController.newTransaction, viewEngine);
        
        get(OfficeController.LIST_PAGE, OfficeController.all, viewEngine);
        get(OfficeController.SHOW_PAGE, OfficeController.show, viewEngine);
    }
}

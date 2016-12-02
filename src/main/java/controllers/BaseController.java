package controllers;

import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.net.URLEncoder;
import java.net.URLDecoder;
import java.sql.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import models.Customer;
import spark.ModelAndView;
import spark.Request;
import spark.Response;

/**
 * Minden vezérlő absztrakt szűlőosztálya. A controllerek közös segédmetódusait
 * tartalmazza.
 * A vezérlők felelnek az "üzleti logika" megvalósításáért, tehát azért hogy a különböző
 * aloladalak között kapcsolatot létesítsenek (pl.: átirányítás), adatokat kérdezzenek 
 * le az adatbázisból (a modellek segítségével) és ezeket a megfelelő nézetsablonban
 * jelenítsék meg.
 * Az, hogy milyen vezérlő milyen név (URL) alatt érhető el, a Main.initRouter()-ben
 * található Spark (get(), post(), stb...) függvények határozzák meg.
 * A vezérlőket nem kell példányosítani minden adattagjuk/függvényük statikus.
 * 
 * Ez az osztály tartalmazza a felhasználókezelést is.
 * 
 * @author akos
 */
public abstract class BaseController {
    public static final String    BANK_NAME    = "TOP Bank";
    protected static final String CUSTOMER_SESSION_ID = "customer_id"; // Ezzel tároljuk felhasználót a sessionben
    protected static final String PREV_PAGE    = "prev_page";   // A sessionben ilyen néven tároljuk az előző lapot
    private static final String FLASH_NOTICE   = "secondary";
    private static final String FLASH_WARNING  = "warning";
    private static final String FLASH_ERROR    = "danger";
    protected static Customer current_customer = null; // A jelenleg bejelentkezett felhasználó
    protected static Request  current_request  = null; // Az aktuális lekérés
    protected static Response current_response = null; // Az aktuális válasz
    protected static HashMap<String, List<String>> messages = null; // üzenetek
    protected static List<String[]> flash = null;    // aktuális üzenetek
    
    /* A teljes sablonnév előállítása */

    /**
     * Nézetsablon megjelenítése, csak az alapváltozókkal (pl: customer, is_logged_in, is_admin).
     * @param viewName
     * @return
     */
    public static ModelAndView templateFor(String viewName) {
        HashMap<String, Object> attrs = new HashMap<>();
	return templateFor(viewName, attrs);
    }

    /**
     * Adott nézetsablon megjelenítése az alap- és a az attrs-ben található 
     * sablonváltozókkal.
     * @param viewName nézet neve
     * @param attrs sablonváltozók
     * @return a megfelelő nézet
     */
    public static ModelAndView templateFor(String viewName, Map<String, Object> attrs) {
        /* A jelen felhasználóra mindig szükség van */
        attrs.put("current_customer", getCurrentCustomer());
        attrs.put("is_logged_in", isLoggedIn());
        attrs.put("is_admin", isAdmin());
        attrs.put("view_name", viewName);
        attrs.put("flash", flash); /* üzenetek megjelenítésére */
        return emptyTemplateFor(viewName, attrs);
    }
    
    public static ModelAndView emptyTemplateFor(String viewName, Map<String, Object> attrs) {
        removeFlash();
        if (viewName == null) {
            return emptyTemplate();
        }
        return new ModelAndView(attrs, "/" + viewName + ".ftl.html");
    }
    
    public static ModelAndView emptyTemplate() {
        return new ModelAndView(null, null);
    }
    
    /* Alcím beállítása a <head>-ben */
    public static String titleFor(String subTitle, Map<String, Object> attrs) {
        String fullTitle = BANK_NAME;
        if (subTitle != null) {
            fullTitle += " | " + subTitle;
        }
        if (attrs != null) {
            attrs.put("title", fullTitle);
        }
        return fullTitle;
    }
    
    /* ========~~~~--+ FELHASZNÁLÓKEZELÉS +--~~~~======== */
    /*
     * Az alábbi metódusoknak logikailag a CustomerControllerben lenne a helye, 
     * de mivel a legtöbb vezérlőnek le kell kérdeznie az aktuális felhasználót
     * jobb ezeket idehelyezni.
    */

    /**
     * Igazat ad ha valaki be van jelentkezve
     * @return
     */
    protected static boolean isLoggedIn() {
        return getCurrentCustomer() != null;
    }
    
    /**
     * Igazat ad ha a jelenleg bejelentkezett felhasználó ügyintéző.
     * @return 
     */
    protected static boolean isAdmin() {
        Customer c = getCurrentCustomer();
        if (c != null) {
            return c.isAdmin();
        }
        return false;
    }
    
    /**
     * Az adott ügyfél bejelentkeztetése.
     * Hogy a belépés teljes legyen az ügyfél azonosítóját a sessionben el kell menteni.
     * Ezután a Customer objektum a getCurrentCustomer()-el ez mindig lekérdezhető lesz
     * amíg a felhasználó ki nem lép.
     * @param c A bejelentkeztetni kívánt ügyfél
     * @return
     */
    protected static Customer loginCustomer(Customer c) {
        current_customer = c;
        if (current_request != null) {
            current_request.session().attribute(CUSTOMER_SESSION_ID
                    , Integer.toString(c.getId()));
        }
        return c;
    }
    
    /**
     * Az ügyfél kiléptetése.
     * Kilépéskor az ügyfél azonosítóját törölni kell a sessionből.
     * @return Az eddigi ügyfél
     */
    protected static Customer logoutCustomer() {
        Customer c = current_customer;
        current_customer = null;
        if (current_request != null) {
            spark.Session sess = current_request.session();
            if (sess != null) {
                sess.removeAttribute(CUSTOMER_SESSION_ID);
                sess.removeAttribute(PREV_PAGE);
            }
        }
        return c;
    }
    
    /**
     * A jelenleg bejelentkezett ügyfél lekérése.
     * Ha a current_customer adattag null, a jelenlegi felhasználót a sessionből
     * kell lekérdeznünk. Ha ott sincs a felhasználó nincs bejelentkezve.
     * @return Az aktuális bejelentkezett ügyfél (null ha nincs ilyen)
     */
    protected static Customer getCurrentCustomer() {
        Customer c = null;
        if (current_customer == null) {
            if (current_request != null) {
                String sid = current_request.session().attribute(CUSTOMER_SESSION_ID);
                if (sid == null) {
                    return null;
                }
                try {
                    c = Customer.findById(Integer.parseInt(sid));
                    current_customer = c;
                } catch (SQLException ex) {
                    return null;
                }
            }
        } else {
            c = current_customer;
        }
        return c;
    }
    
    /* ========~~~~--+ LAPKEZELÉS/VISSZAIRÁNYÍTÁS +--~~~~======== */
    /* Ahhoz, hogy a klienst visszalehessen irányítani az előző lapra
     * a main.js-nek be kell állítania a PREV_PAGE cookie-t az aktuális oldalra.
     * A getPreviousPage() ezen cookie értékét adja vissza. A redirectBack()
     * is ezt használja a visszadobáshoz. Ha nincs ilyen süti, a kezdőoldalra
     * jutunk vissza.
    */
    /**
     * Az előző oldalra való visszatérés.
     * @return
     */
    protected static String getPreviousPage() {
        if (current_request != null) {
            return current_request.cookie(PREV_PAGE);
        }
        return null;
    }
    
    /**
     * Visszaírányítás az előző oldalra.
     */
    protected static ModelAndView redirectBack() {
        String ppage = getPreviousPage();
        if (current_response != null) {
            current_response.redirect((ppage != null) ? ppage : "/");
        }
        return emptyTemplateFor(null, null);
    }
    
    /** 
     * Csak a következő lapon jelenik meg az üzenet.
     * Akkor használatos, ha az üzenetet, csak átirányítás 
     * után akarjuk megjelenítettni.
    */
    private static void addMessage(String type, String msg) {
        List<String> msgs = null;
        if (messages == null) {
            messages = new HashMap<>();
        }
        if (messages.containsKey(type)) {
            msgs = messages.get(type);
        } else {
            msgs = new ArrayList<>();
        }

        msgs.add(msg);
        messages.put(type, msgs);
    }
    
    /**
     * Az üzenet rögtön felvillan a nézetben.
     * @param type
     * @param msg 
     */
    private static void addFlash(String type, String msg) {
        if (flash == null) {
            flash = new ArrayList<>();
        }
        
        String fsh[] = new String[2];
        fsh[0] = type;
        fsh[1] = msg;
        flash.add(fsh);
    }
    
    /**
     * Üzenet megjelenítése. 
     * @param type FLASH_ERROR, FLASH_WARNING vagy FLASH_NOTICE
     * @param msg megjelenítendő üzenet
     * @param now igaz, ha a jelenlegi nézetben kívánjuk, false, ha átirányítás után
     */
    private static void showMessage(String type, String msg, boolean now) {
        if (now) {
            addFlash(type, msg);
        } else {
            addMessage(type, msg);
        }
    }
    
    /**
     * Sikert jelző üzenet.
     * @param msg üzenet
     * @param now Igaz ha a jelenlegi nézetben akarjuk megjeleníteni.
     */
    protected static void showSuccess(String msg, boolean now) {
        showMessage(FLASH_NOTICE, msg, now);
    }
      
    /**
     * Sikert jelző üzenet. Csak átirányítás után jelenik meg.
     * @param msg üzenet
     */  
    protected static void showSuccess(String msg) {
        showSuccess(msg, false);
    }
    
    /**
     * Figyelmeztető üzenet.
     * @param msg üzenet
     * @param now Igaz ha a jelenlegi nézetben akarjuk megjeleníteni.
     */
    protected static void showWarning(String msg, boolean now) {
        showMessage(FLASH_WARNING, msg, now);
    }
        
    /**
     * Figyelmeztető üzenet. Csak átirányítás után jelenik meg.
     * @param msg üzenet
     */
    protected  static void showWarning(String msg) {
        showWarning(msg, false);
    }
    
    /**
     * Hibát jelző üzenet.
     * @param msg üzenet
     * @param now Igaz ha a jelenlegi nézetben akarjuk megjeleníteni.
     */
    protected static void showError(String msg, boolean now) {
        showMessage(FLASH_ERROR, msg, now);
    }
    
    /**
     * Hibát jelző üzenet. Csak átirányítás után jelenik meg.
     * @param msg üzenet
     */
    protected static void showError(String msg) {
        showError(msg, false);
    }
    
    /**
     * Azokat a lapokat védi, amiket csak ügyintéző nyithat meg.
     * Ha az aktuális ügyfél nincs bejelentkezve, vagy nem ügyintéző
     * hibával visszadobja az előző lapra.
     * 
     * A nevet nem lehetett kihagyni, bocs.
     */
    protected static boolean checkYourPrivilege() {
        boolean isadm = isAdmin();
        if (!isadm) {
            showError("Hozzáférés megtagadva!");
            redirectBack();
        }
        return isadm;
    }
    
    protected static boolean checkLogin() {
        if (!isLoggedIn()) {
            showError("Ehhez a laphoz be kell jelentkeznie!");
            if (current_response != null) {
                current_response.redirect("/login");
            }
            return false;
        }
        return true;
    }
    
    /**
     * Üzenetek beolvasása a flash-be.
     */
    private static void setupFlash() {
        String flashes[] = new String[3];
        String fnames[] = { FLASH_NOTICE, FLASH_WARNING, FLASH_ERROR };
        flash = new ArrayList<>();
        messages = null;
        if (current_request != null) {
            for (int i = 0; i < fnames.length; i++) {
                try {
                    String cookie = current_request.session().attribute(fnames[i]);
                    if (cookie != null) {
                        /* A süti nem ismeri kódolt, UTF-8 szerint kell dekódolni. */
                        flashes[i] = URLDecoder.decode(cookie, "UTF-8");              
                        for (String fl : flashes[i].split("\t")) {
                            addFlash(fnames[i], fl);
                        }
                    } else {
                        flashes[i] = null;
                    }
                } catch (UnsupportedEncodingException ex) {
                    Logger.getLogger(BaseController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
    
    /**
     * Üzenetek lementése a sessionbe.
     * @param type
     * @param al 
     */
    private static void saveFlash(String type, List<String> al) {
        try {
            /* Az üzenetek UTF-8-ban vannak, ezeket viszont nem lehet sütibe
             * menteni. Előtte az azonos típusúak közé tabulátort teszünk
             * (\t) majd, kódoljuk őket az URLEncoder-rel.
            */
            String cookie = String.join("\t", al.toArray(new String[0]));
            if (cookie != null) {
                current_request.session().attribute(type, URLEncoder.encode(cookie, "UTF-8"));
            }
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(BaseController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Az összes üzenet Session-be mentése.
     */
    public static void saveFlash() {
        if (current_request != null) {
            /* Nem volt törölve (megjelenítve) a flash, ezért továbbadjuk */
            if (flash != null) {
                for (String[] f : flash) {
                    addMessage(f[0], f[1]);
                }
            }
            if (messages != null) {
                Set<String> keys = messages.keySet();
                for (String fl : keys) {
                    List<String> al = messages.get(fl);
                    saveFlash(fl, al);
                }
                messages = null;
                flash    = null;
            }
            
        }
    }
    
    /**
     * Üzenetek törlése. (megjelenítés után)
     */
    private static void removeFlash() {
        if (current_response != null) {
            current_request.session().removeAttribute(FLASH_ERROR);
            current_request.session().removeAttribute(FLASH_NOTICE);
            current_request.session().removeAttribute(FLASH_WARNING);
            flash = null;
        }
    }
    
    
    public static void setRequestAndResponse(Request req, Response res) {
        current_request  = req;
        current_response = res;
        current_customer = null;
        setupFlash();
    }
}

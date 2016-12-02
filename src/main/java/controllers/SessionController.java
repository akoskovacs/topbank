package controllers;
import models.Customer;
import spark.*;

/**
 * A munkamenet (bejelentkezés/kijelentkezés) kezelésére szolgáló osztály.
 * További infó: BaseController és SparkJava dokumentáció
 * @author akos
 */
public class SessionController extends BaseController {
    public static final String LOGIN_PAGE   = "/login";
    public static final String LOGOUT_PAGE  = "/logout";
    public static final String PROFILE_PAGE = "/profile";

    /**
     * A főoldalt jeleníti meg.
     */
    public static TemplateViewRoute index = (Request req, Response res) -> {
        return templateFor("index");
    };
    
    /**
     * A /login oldalt jeleníti meg. (GET)
     */
    public static TemplateViewRoute login = (Request req, Response res) -> {
        return templateFor("session/login");
    };
    
    /**
     * A /logout oldallal jeletkeztet ki.
     */
    public static Route logout = (Request req, Response res) -> {
        /* A jelenlegi felhasználó törlése a sessionből */
        logoutCustomer();
        showSuccess("Sikeresen kijelentkezett");
        res.redirect("/");
        return "";
    };
    
    /**
     * A /login oldalról küldött felhasználói adatok (azonosító, jelszó)
     * ellenőrzése, bejelentkeztetés.
     */
    public static Route authenticate = (Request req, Response res) -> {
        /* A login form-ban elküldött (POST) azonosító, jelszó */
        String cid = req.queryParams("customer_id");
        String cpass = req.queryParams("customer_password");
        /* A megadott felhasználó lekérése, jelszavának ellenőrzése. */
        Customer c = Customer.login(cid, cpass);
        /* Van ilyen felhasználó, ezzel a jelszóval */
        if (c != null) {
            /* id mentése session-be */
            loginCustomer(c);
            showSuccess("Sikeres bejelentkezés! Üdvözlöm, " + c.getName() + "!");
            /* Átirányítás az ügyfélprofilhoz, vagy a vezérlőpulthoz (ügyintézőknél) */
            if (c.isAdmin()) {
                res.redirect("/");
            } else {
                res.redirect(PROFILE_PAGE);
            }
        } else {
            showError("Sikertelen bejelentkezés :(");
            /* TODO: HIBA */
            /* Valami nem jó, vissza a login laphoz */
            res.redirect(LOGIN_PAGE);
        }
        return "";
    };
}

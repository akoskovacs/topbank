/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import models.Account;
import models.Card;
import models.Customer;
import models.Office;
import models.Transaction;
import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.TemplateViewRoute;

/**
 * Ügyfelek kezelésére szolgáló vezérlő.
 * @author akos
 */
public class CustomerController extends BaseController {
    public static TemplateViewRoute all = (Request req, Response res) -> {
        HashMap<String, Object> attrs;
        /* Nincs jogosultsága ehhez a laphoz vissza a bejelentkezéshez */
        if (!checkYourPrivilege()) {
            return emptyTemplate();
        }
        attrs = new HashMap<>();
        try {
            List<Customer> all = Customer.getAll(0);
            attrs.put("customers", all);
        } catch (SQLException e) {
            showError("Nem lehet megjeleníteni az ügyfeleket!");
            res.redirect("/");
        }
        return templateFor("customer/all", attrs);
    };
    
    private static HashMap<String, Object> setOfficeCombobox(HashMap<String, Object> attrs) throws SQLException {
        Map<Integer, String> offices = Office.getAllAsStrings();
        /* FreeMarker workaround :( :( :( */
        attrs.put("office_ids", offices.keySet());
        attrs.put("office_addrs", offices.values());
        return attrs;
    }
    
    /**
     * Új ügyfél létrehozása.
     * 
     */
    public static TemplateViewRoute newCustomer = (Request req, Response res) -> {
        HashMap<String, Object> attrs;
        
        /* Csak ügyintéző hozhat létre új ügyfelet. */
        if (!checkYourPrivilege()) {
            return emptyTemplate();
        }
        
        attrs = new HashMap<>();
        setOfficeCombobox(attrs);
        attrs.put("customer", null); /* még nincs létrehozva, de a nézet hivatkozik rá */
        /* Minden ok view/customer/new.ftl.html megjelenítése */
        return templateFor("customer/new", attrs);
    };
    
    public static TemplateViewRoute saveNewCustomer = (Request req, Response res) -> {
        HashMap<String, Object> attrs;
        
        /* Csak ügyintéző hozhat létre új ügyfelet. */
        if (!checkYourPrivilege()) {
            return emptyTemplate();
        }
        
        Customer c = new Customer();
        try {
            c.setName(req.queryParams("name"));
            c.setPrivateId(req.queryParams("private_id"));
            c.setCity(req.queryParams("city"));
            c.setAddress(req.queryParams("address"));
            String pcode = req.queryParams("postal_code");
            if (pcode != null && !pcode.isEmpty()) {
                c.setPostalCode(Integer.parseInt(pcode));
            }
            c.setPassword(req.queryParams("password"), req.queryParams("password_confirm"));
            String offid = req.queryParams("office_id");
            if (offid != null && !offid.isEmpty()) {
                c.setOffice(Integer.parseInt(offid));
            }
            String isadm = req.queryParams("is_admin");
            if (isadm != null) {
                c.setAdmin(isadm.equals("true"));
            }
        } catch (Exception e) {
        }

        if (c.save()) {
            /* Sikeres mentés */
            showSuccess("Ügyfél mentve!");
            res.redirect("/customer/all");
        } else {
            /* Sikertelen, hiba */
            showError("Nem lehet menteni az ügyfelet!", true);
            attrs = new HashMap<>();
            setOfficeCombobox(attrs).put("customer", c);
            return templateFor("customer/new", attrs);
        }

        return emptyTemplateFor(null, null);
    };
    
    public static ModelAndView profileFor(Customer c) {
        HashMap<String, Object> attrs;
        List<Account> accs = null;
        List<Card> cards   = null;
        List<Transaction> trans = null;
        if (c == null) {
            showError("Nincs ilyen ügyfél!");
            redirectBack();
        } else {
            try {
                accs  = c.getAccounts();
                //cards = accs.get(1).getCards();
                
            } catch (SQLException ex) {
                Logger.getLogger(CustomerController.class.getName()).log(Level.SEVERE, null, ex);
            }
            System.out.println(accs);
        }
        attrs = new HashMap<>();
        attrs.put("customer", c);
        attrs.put("accounts", accs);
        return templateFor("customer/profile", attrs);
    }
    
    /**
     * A jelenlegi felhasználó profiljának megjelenítése.
     */
    public static TemplateViewRoute profile = (Request req, Response res) -> {

        /* TODO: privillégium ellenőrzés */
        /* Ha nincs bejeletkezve, nem mutatunk neki semmit */
        if (!isLoggedIn()) {
            showError("Nincs bejelentkezve!");
            res.redirect(SessionController.LOGIN_PAGE);
        }
        Customer c = getCurrentCustomer();
        return profileFor(c);
    };
    
    public static TemplateViewRoute show = (Request req, Response res) -> {
        HashMap<String, Object> attrs;
        Customer c = null;
        
        /* Az ügyfélazonosító az URI-ben van */
        String scid = req.params(":cid");
        if (scid == null) {
            /* hiba */
            showError("Hibás adatok!");
            redirectBack();
        }
        int cid = 0;
        try {
            cid = Integer.parseInt(scid);
        } catch (Exception e) {
            showError("Hibás ügyfélszám!");
            redirectBack();
        }
        Customer curr = getCurrentCustomer();
        /* Nem saját profilt néz és nem is admin tehát nincs jogosultsága */
        if (curr == null || curr.getId() != cid && !curr.isAdmin()) {
            showError("Hozzáférés megtagadva!");
            redirectBack();
        }
        return profileFor(Customer.findById(cid));
        
    };
}

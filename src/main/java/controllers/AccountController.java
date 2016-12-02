package controllers;

import java.util.HashMap;
import java.util.List;
import models.Account;
import models.Card;
import models.Customer;
import models.Transaction;
import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.TemplateViewRoute;

public class AccountController extends BaseController {
    private static ModelAndView noAccountError() {
        showError("Nincs ilyen számla");
        redirectBack();
        return emptyTemplate();
    }
    
    public static TemplateViewRoute show = (Request req, Response res) -> {
        String sacid = req.params(":aid");
        HashMap<String, Object> attrs;
        if (!checkLogin()) {
            return emptyTemplate();
        }
        
        if (sacid == null || sacid.isEmpty()) {
            return noAccountError();
        }
        attrs = new HashMap<>();
        int acid = 0;
        Account acc      = null;
        Customer owner   = null;
        List<Card> cards = null;
        List<Transaction> trs = null;
                    acid = Integer.parseInt(sacid);
            
            acc  = Account.findById(acid);
            if (acc != null) {
                owner = acc.getCustomer();
                if ((owner.getId() != current_customer.getId()) && !isAdmin()) {
                    showError("Hozzáférés megtagadva!");
                    redirectBack();
                }
                cards = acc.getCards();
                trs    = acc.getTransactions();
            }
        try {

        } catch (Exception e) {
            return noAccountError();
        }
        attrs.put("account", acc);
        attrs.put("customer", owner);
        attrs.put("cards", cards);
        attrs.put("transactions", trs);
        return templateFor("account/show", attrs);
    };
}

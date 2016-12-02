package controllers;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import models.Account;
import models.Customer;
import models.Transaction;
import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.TemplateViewRoute;

/**
 * Tranzakcióvezérlő.
 *
 * @author akos
 */
public class TransactionController extends BaseController {

    private static ModelAndView newTransactionFor(Customer c) {
        if (c == null) {
            showError("Nincs ilyen ügyfél!");
            return redirectBack();
        }
        HashMap<String, Object> attrs = new HashMap<>();

        List<Account> accs = null;
        try {
            accs = current_customer.getAccounts();
        } catch (SQLException ex) {
            Logger.getLogger(TransactionController.class.getName()).log(Level.SEVERE, null, ex);
        }
        attrs.put("customer", c);
        attrs.put("accounts", accs);
        return templateFor("transaction/new", attrs);
    }

    public static TemplateViewRoute newTransaction = (Request req, Response res) -> {
        checkLogin();
        return newTransactionFor(current_customer);
    };

    public static TemplateViewRoute newTransactionFor = (Request req, Response res) -> {
        checkYourPrivilege();
        String scid = req.params(":cid");
        Customer c = null;
        if (scid == null || scid.isEmpty()) {
            try {
                int cid = Integer.parseInt(scid);
                c = Customer.findById(cid);
            } catch (Exception e) {
                showError("Hibás adatok");
                redirectBack();
                return emptyTemplate();
            }
        }
        return newTransactionFor(c);
    };

    private static int parseOrZero(String s) {
        try {
            return Integer.parseInt(s);
        } catch (Exception e) {
            return 0;
        }
    }
    
    public static TemplateViewRoute save = (Request req, Response res) -> {
        HashMap<String, Object> attrs = new HashMap<>();
        String sacid = req.params(":cid");
        Customer c = null;
        c = Customer.findById(Integer.parseInt(sacid));
        List<Account> accs = c.getAccounts();
        Account payer = null;
        Transaction t = new Transaction();
        try {
            String r = req.queryParams("amount"); 
            t.setAmount(parseOrZero(r));
            r = req.queryParams("payer_account_id");
            int payer_id = parseOrZero(r);
            payer = Account.findById(payer_id);
            if (payer != null) {
                if (!isAdmin() && payer.getCustomerId() != c.getId()) {
                    showError("Csak saját számláról küldhet pénzt!");
                    redirectBack();
                }
            } else {
                throw new Exception();
            }
            t.setPayerAccount(payer);
            r = req.queryParams("payee_account_id");
            t.setPayeeAccount(parseOrZero(r));
            t.setStatement(req.queryParams("tr_statement"));
            t.setComment(req.queryParams("tr_comment"));
        } catch (Exception e) {
            Logger.getLogger(Transaction.class.getName()).log(Level.SEVERE, null, e);
            showError("Hibás adatok!");
            redirectBack();
        }

        if (t.save()) {
            showSuccess("Tranzakció mentve!");
            res.redirect("/account/" + payer.getId());
        } else {
            System.out.println(t.getErrors());
            showError("Hibás tranzakció", true);
            attrs.put("transaction", t);
            attrs.put("accounts", accs);
            attrs.put("customer", c);
        }
        System.out.println(t);
        return templateFor("transaction/new", attrs);
    };

}

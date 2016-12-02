package controllers;

import java.util.HashMap;
import java.util.List;
import models.Office;
import spark.Request;
import spark.Response;
import spark.TemplateViewRoute;

/**
 * Fiók vezérlő. Fiókok listázása/megjelenítése.
 * @author akos
 */
public class OfficeController extends BaseController {
    public static final String LIST_PAGE = "/offices";
    public static final String SHOW_PAGE = "/office/:oid";
    
    public static TemplateViewRoute all = (Request req, Response res) -> {
        HashMap<String, Object> attrs = new HashMap<>();
        List<Office> offices = null;
        try {
            offices = Office.getAll(0);
        } catch (Exception e) {
            showError("Nem sikerült megjeleníteni", true);
        }
        attrs.put("offices", offices);
        return templateFor("office/all", attrs);
    };
    
    public static TemplateViewRoute show = (Request req, Response res) -> {
        HashMap<String, Object> attrs = new HashMap<>();
        String soid = req.params(":oid");
        int oid = 1;
        Office off = null;
        try {
            if (soid == null) {
                throw new Exception();
            }
            oid = Integer.parseInt(soid);
        } catch (Exception e) {
            showError("Hibás fiókazonosító!");
            return redirectBack();
        }
        try {
            off = Office.findById(oid);
            if (off == null) {
                throw new Exception();
            }
        } catch (Exception e) {
            showError("Nincs ilyen fiók!");
            return redirectBack();
        }
        attrs.put("office", off);
        return templateFor("office/show", attrs);
    };
}

package models;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Office extends BaseModel {
    private static final int COLUMN_CITY        = 2;
    private static final int COLUMN_ADDRESS     = 3;
    private static final int COLUMN_POSTAL_CODE = 4;

    private static final String COLUMN_NAMES[] = { "city", "address", "postal_code" };

    private String city     = null;
    private String address  = null;
    private int postal_code = 0;

    /* ========~~~~--+ PUBLIKUS OSZTÁLYMETÓDUSOK +--~~~~======== */
    public static List<Office> getAll(int limit) throws SQLException {
        ArrayList<Office> all = new ArrayList<>();
        ResultSet rs = find(Office.tableName(), null, null, limit);
        while (rs.next()) {
            Office c = new Office();
            c.setValues(rs);
            all.add(c);
        }
        return all;
    }
    
    public static Map<Integer, String> getAllAsStrings() throws SQLException {
        HashMap<Integer, String> all = new HashMap<>();
        ResultSet rs = find(Office.tableName(), null, null, 0);
        while (rs.next()) {
            Office c = new Office();
            c.setValues(rs);
            all.put(c.getId(), c.getLongAddress());
        }
        return all;
    }

    public static Office findById(int id) throws SQLException {
        Office c = null;
        ResultSet rs = findSingle(Office.tableName(), "id", Integer.toString(id));
        if (rs.next()) {
            c = new Office();
            /* model betöltáse rekordból */
            c.setValues(rs);
        }
        return c;
    }

    public static String tableName() {
        return Office.class.getSimpleName();
    }

    /* ========~~~~--+ PUBLIKUS PÉLDÁNYMETÓDUSOK +--~~~~======== */
    public Office() {
        this(null, null, 0);
    }

    public Office(String city, String address, int pcode) {
        super(COLUMN_NAMES);
        is_new = true;
        this.city = city;
        this.address = address;
        this.postal_code = pcode;
    }

    public Customer newCustomer() {
        Customer cust = new Customer();

        return cust;
    }

    @Override
    public boolean validate() {
        String svals[] = new String[]{
            city, address
        };
        String scols[] = new String[]{
            "city", "address"
        };
        String se_errors[] = new String[]{
            "A város nem lehet üres", "A cím nem lehet üres"
        };
        failIfStringsEmpty(svals, scols, se_errors);
        failForIf(postal_code == 0, "postal_code", "Az irányítószám nem lehet üres");
        failForIf(postal_code < 0, "postal_code", "Az irányítószám nem lehet negatív");
        return !hasErrors();
    }

    /* ========~~~~--+ PUBLIKUS LEKÉRDEZŐ METÓDUSOK +--~~~~======== */
    public String getCity() {
        return city;
    }

    public String getAddress() {
        return address;
    }

    public int getPostalCode() {
        return postal_code;
    }
    
    public String getLongAddress() {
        String s = new String();
        s += Integer.toString(getPostalCode()) + " " 
                + getCity() + ", " + getAddress();
        return s;
    }

    /* ========~~~~--+ PUBLIKUS BEÁLLÍTÓ METÓDUSOK +--~~~~======== */
    public Office setCity(String city) {
        checkForChange(COLUMN_CITY, this.city, city);
        this.city = city;
        return this;
    }

    public Office setAddress(String address) {
        checkForChange(COLUMN_ADDRESS, this.address, address);
        this.address = address;
        return this;
    }

    public Office setPostalCode(int pcode) {
        checkForChange(COLUMN_POSTAL_CODE, this.postal_code, pcode);
        this.postal_code = pcode;
        return this;
    }

    /* ========~~~~--+ MEZŐLEKÉRDEZŐ/BEÁLLÍTÓ FÜGGVÉNYEK +--~~~~======== */
    @Override
    public String[] getValues() {
        return new String[]{
            city, address, Integer.toString(postal_code)
        };
    }

    @Override
    public void setValues(ResultSet rs) throws SQLException {
        is_new = false;
        id = rs.getInt("id");
        city = rs.getString(COLUMN_NAMES[0]);
        address = rs.getString(COLUMN_NAMES[1]);
        postal_code = rs.getInt(COLUMN_NAMES[2]);
    }
}

package models;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/* TODO */
public class Card extends BaseModel {
    private static final int COLUMN_CARDTYPE    = 2;
    private static final int COLUMN_FEE         = 3;
    private static final int COLUMN_PIN         = 4;
    private static final int COLUMN_CCV2        = 5;
    private static final int COLUMN_CREATED_AT  = 6;
    private static final int COLUMN_EXPIRES_AT  = 7;
    private static final int COLUMN_ACCOUNT_ID  = 8;
    
    public enum CardType { CARD_VISA, CARD_MASTERCARD, CARD_VISA_ELECTRON, CARD_AMERICAN_EXPRESS }
    public static final String CARD_TYPE_NAMES[] = new String[] {
        "Visa", "MasterCard", "Visa Electron", "American Express"
    };

    private static final String COLUMN_NAMES[] = {
        "cardtype", "fee", "pin", "ccv2", "created_at",
        "expires_at", "account_id"
    };

    private int cardtype = 0;
    private int fee = 0;
    private int pin = 0;
    private int ccv2 = 0;
    private Timestamp created_at = null;
    private Timestamp expires_at  = null;
    private int account_id = 0;
    private Account account = null;

    /* ========~~~~--+ PUBLIKUS OSZTÁLYMETÓDUSOK +--~~~~======== */
    public static List<Card> getAll(int limit) throws SQLException {
        ArrayList<Card> all = new ArrayList<>();
        ResultSet rs = find(Card.tableName(), null, null, limit);

        while (rs.next()) {
            Card c = new Card();
            c.setValues(rs);
            all.add(c);
        }
        return all;
    }

    public static Card findById(int id) throws SQLException {
        Card c = null;
        ResultSet rs = findSingle(Card.tableName(), "id", Integer.toString(id));
        if (rs.next()) {
            c = new Card();
            c.setValues(rs);
        }
        return c;
    }

    public static List<Card> findByAccount(Account acc, int limit) throws SQLException {
        ArrayList<Card> all = new ArrayList<>();
        ResultSet rs = findSingle(Card.tableName(), "account_id", Integer.toString(acc.getId()), limit);

        while (rs.next()) {
            Card c = new Card();
            c.setValues(rs);
            all.add(c);
        }
        return all;
    }

    public static List<Card> findByAccount(Account acc) throws SQLException {
        return findByAccount(acc, 0);
    }

    public static String tableName() {
        return Card.class.getSimpleName();
    }

    /* ========~~~~--+ PUBLIKUS PÉLDÁNYMETÓDUSOK +--~~~~======== */
    public Card() {
        this(0, 0, 0, 0, null);
    }

    public Card(int cardtype, int fee, int pin, int ccv2, Timestamp expires_at) {
        super(COLUMN_NAMES);

        this.cardtype = cardtype;
        this.fee = fee;
        this.pin = pin;
        this.ccv2 = ccv2;
        this.expires_at = expires_at;
        
        is_new = is_changed = true;
    }

    @Override
    public boolean validate() {
        failForIf(fee < 0, "fee", "A kártyadíj nem lehet negatív");
        failForIf(pin == 0, "pin", "A PIN kód nem lehet üres vagy nulla");
        failForIf(account_id == 0, "account_id", "A számlát meg kell adni");
        failForIf(expires_at == null, "expires_at", "A lejárati dátum nem lehet üres");
        failForIf(expires_at != null && expires_at.before(Timestamp.valueOf(LocalDateTime.now()))
                , "expires_at", "A lejárati dátumnak a jövőben kell lennie");
        failForIfNot(cardtype >= 0 && cardtype < CARD_TYPE_NAMES.length, "cardtype", "Nincs ilyen kártyatípus");
        return !hasErrors();
    }

    /* ========~~~~--+ PUBLIKUS LEKÉRDEZŐ METÓDUSOK +--~~~~======== */
    public int getCardType() {
        return cardtype;
    }
    
    public String getCardTypeName() {
        return CARD_TYPE_NAMES[cardtype];
    }

    public int getFee() {
        return fee;
    }

    public int getPin() {
        return pin;
    }

    public int getCcv2() {
        return ccv2;
    }
    
    public Timestamp getExpiration() {
        return expires_at;
    }

    public Account getAccount() throws SQLException {
        if (account == null) {
            account = Account.findById(account_id);
        }
        return this.account;
    }
    
    public Customer getCustomer() throws SQLException {
        Account acc = getAccount();
        if (acc != null) {
            return acc.getCustomer();
        }
        return null;
    }

    /* ========~~~~--+ PUBLIKUS BEÁLLITÓMETÓDUSOK +--~~~~======== */
    public Card setCardType(int cardtype) {
        checkForChange(COLUMN_CARDTYPE, this.cardtype, cardtype);
        this.cardtype = cardtype;
        return this;
    }

    public Card setFee(int fee) {
        checkForChange(COLUMN_FEE, this.fee, fee);
        this.fee = fee;
        return this;
    }

    public Card setPin(int pin) {
        checkForChange(COLUMN_PIN, this.pin, pin);
        this.pin = pin;
        return this;
    }

    public Card setCcv2(int ccv2) {
        checkForChange(COLUMN_CCV2, this.ccv2, ccv2);
        this.ccv2 = ccv2;
        return this;
    }

    public Card setAccount(Account acc) {
        setAccount(acc.getId());
        this.account = acc;
        return this;
    }

    public Card setAccount(int account_id) {
        checkForChange(COLUMN_ACCOUNT_ID, this.account_id, account_id);
        this.account_id = account_id;
        return this;
    }
    
    public Card setExpiresAt(Timestamp expires_at) {
        checkForChange(COLUMN_EXPIRES_AT, this.expires_at, expires_at);
        this.expires_at = expires_at;
        return this;  
    }
    
    public Card setCreatedAt(Timestamp t) {
        checkForChange(COLUMN_CREATED_AT, this.created_at, t);
        this.created_at = t;
        return this;
    }

    /* ========~~~~--+ MEZŐLEKÉRDEZŐ/BEÁLLÍTÓ FÜGGVÉNYEK +--~~~~======== */
    @Override
    public String[] getValues() {
        return new String[]{
            Integer.toString(cardtype), Integer.toString(fee), Integer.toString(pin)
                , Integer.toString(ccv2), TM_FORMAT.format(created_at)
                , TM_FORMAT.format(expires_at), Integer.toString(account_id)
        };
    }

    @Override
    public void setValues(ResultSet rs) throws SQLException {
        is_new = false;
        id = rs.getInt("id");
        cardtype = rs.getInt(COLUMN_NAMES[0]);
        fee = rs.getInt(COLUMN_NAMES[1]);
        pin = rs.getInt(COLUMN_NAMES[2]);
        ccv2 = rs.getInt(COLUMN_NAMES[3]);
        created_at = rs.getTimestamp(COLUMN_NAMES[4]);
        expires_at = rs.getTimestamp(COLUMN_NAMES[5]);
        account_id = rs.getInt(COLUMN_NAMES[6]);
    }

    @Override
    public void updateCreatedAt() {
        if (this.created_at == null) {
            this.created_at = Timestamp.valueOf(LocalDateTime.now());
        }
    }

}

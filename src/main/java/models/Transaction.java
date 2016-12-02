package models;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import static models.BaseModel.find;

public class Transaction extends BaseModel {

    private static final int COLUMN_AMOUNT           = 2;
    private static final int COLUMN_TR_STATEMENT     = 3;
    private static final int COLUMN_TR_COMMENT       = 4;
    private static final int COLUMN_PAYER_ACCOUNT_ID = 5;
    private static final int COLUMN_PAYEE_ACCOUNT_ID = 6;
    private static final int COLUMN_CREATED_AT       = 7;

    private static final String COLUMN_NAMES[] = {
        "amount", "tr_statement", "tr_comment",
        "payer_account_id", "payee_account_id", "created_at"
    };

    private int amount            = 0;
    private String tr_statement   = null;
    private String tr_comment     = null;
    private int payer_account_id  = 0;
    private int payee_account_id  = 0;
    private Timestamp created_at  = null;
    private Account payer_account = null;
    private Account payee_account = null;

    private static ArrayList<Transaction> setupTransactions(ResultSet rs) throws SQLException {
        ArrayList<Transaction> all = new ArrayList<>();
        while (rs.next()) {
            Transaction c = new Transaction();
            c.setValues(rs);
            all.add(c);
        }
        return all;
    }
    
    public static List<Transaction> getAll(int limit) throws SQLException {
        /* A find segédfüggvény automatikusan létrehozza és lefuttatja a 
           megfelelő SELECT-et
        */
        ResultSet rs = find(Transaction.tableName(), null, null, limit);
        return setupTransactions(rs);
    }
    
    public static Transaction findById(int id) throws SQLException {
        Transaction c = null;
        ResultSet rs = findSingle(Transaction.tableName(), "id", Integer.toString(id));
        if (rs.next()) {
            c = new Transaction();
            /* model betöltése rekordból */
            c.setValues(rs);
        }
        return c;
    }
    
    public static List<Transaction> findByPayerAccount(int account_id, int limit) throws SQLException {
        ResultSet rs = findSingle(Transaction.tableName()
                , "payer_account_id", Integer.toString(account_id), limit);
        return setupTransactions(rs);
    }
    
    public static List<Transaction> findByPayerAccount(Account acc, int limit) throws SQLException {
        return findByPayerAccount(acc.getId(), limit);
    }
    
    public static List<Transaction> findByPayeeAccount(int account_id, int limit) throws SQLException {
        ResultSet rs = findSingle(Transaction.tableName()
                , "payee_account_id", Integer.toString(account_id), limit);
        return setupTransactions(rs);
    }
    
    public static List<Transaction> findByPayeeAccount(Account acc, int limit) throws SQLException {
        return findByPayeeAccount(acc.getId(), limit);
    }
    
    public static int sumAmout(List<Transaction> trans) {
        int amount = 0;
        
        for (Transaction t : trans) {
            amount += t.getAmount();
        }
        return amount;
    }
    
    public static String tableName() { return Transaction.class.getSimpleName(); }
    
    public Transaction() {
        this(0, null, null, 0, 0);
    }
    
    public Transaction(int amount, String statement, String comment
            , Account payer_account, Account payee_account) {
        
        this(amount, statement, comment
                , (payer_account == null) ? 0 : payer_account.getId()
                , (payee_account == null) ? 0 : payee_account.getId());
        this.payee_account = payee_account;
        this.payer_account = payer_account;
    }
    
    public Transaction(int amount, String tr_statement, String tr_comment,
            int payer_account_id, int payee_account_id) {
        super(COLUMN_NAMES);

        this.amount           = amount;
        this.tr_statement     = tr_statement;
        this.tr_comment       = tr_comment;
        this.payer_account_id = payer_account_id;
        this.payee_account_id = payee_account_id;
        is_new     = true;
    }

    @Override
    public boolean validate() {
        try {
            failForIf(payee_account_id == 0 && payer_account_id == 0, "payer_account_id"
                    , "A címzett számla és a fogadó számla nem lehet egyszerre üres");
            failForIf(amount == 0, "amount", "A küldeni kívánt összeg nem lehet 0");
            failForIf(amount < 0, "amount", "A küldeni kívánt összeg nem lehet negatív");
            failForIf(payee_account_id == payer_account_id, "payee_account_id", "A számlaszámok nem lehet azonosak");
            Account payer = (this.payer_account != null) ? this.payer_account : Account.findById(payer_account_id);
            Account payee = (this.payee_account != null) ? this.payee_account : Account.findById(payee_account_id);
            failForIf((payer == null) && (payer_account_id != 0), "payer_account_id"
                    , "Nincs ilyen küldő számlaszám");
            failForIf((payee == null) && (payee_account_id != 0), "payee_account_id"
                    , "Nincs ilyen fogadó számlaszám");
            failForIf((payer != null) && (payer.getAmount() < amount), "amount", "A számlán nincs fedezet");
        } catch (SQLException ex) {
            Logger.getLogger(Transaction.class.getName()).log(Level.SEVERE, null, ex);
        }
        return !hasErrors();
    }

    public int getAmount()          { return this.amount; }
    public String getStatement()    { return this.tr_statement; }
    public String getComment()      { return this.tr_comment; }
    public int getPayerAccountId()  { return this.payer_account_id; }
    public int getPayeeAccountId()  { return this.payee_account_id; }
    public Timestamp getCreatedAt() { return this.created_at; } 
    
    public Account getPayerAccount() throws SQLException {
        if (this.payer_account == null) {
            this.payer_account = Account.findById(this.payer_account_id);
        }
        return this.payer_account;
    }
    
    public Account getPayeeAccount() throws SQLException {
        if (this.payee_account == null) {
            this.payee_account = Account.findById(this.payee_account_id);
        }
        return this.payee_account;
    }
    
    public Customer getPayer() throws SQLException {
        Account acc = getPayeeAccount();
        if (acc != null) {
            return acc.getCustomer();
        }
        return null;
    }
    
    public Customer getPayee() throws SQLException {
        Account acc = getPayeeAccount();
        if (acc != null) {
            return acc.getCustomer();
        }
        return null;
    }

    public Transaction setAmount(int amount) {
        checkForChange(COLUMN_AMOUNT, this.amount, amount);
        this.amount = amount;
        return this;
    }

    public Transaction setStatement(String tr_statement) {
        checkForChange(COLUMN_TR_STATEMENT, this.tr_statement, tr_statement);
        this.tr_statement = tr_statement;
        return this;
    }

    public Transaction setComment(String tr_comment) {
        checkForChange(COLUMN_TR_COMMENT, this.tr_comment, tr_comment);
        this.tr_comment = tr_comment;
        return this;
    }

    public Transaction setPayerAccount(int payer_account_id) {
        checkForChange(COLUMN_PAYER_ACCOUNT_ID, this.payer_account_id, payer_account_id);
        this.payer_account_id = payer_account_id;
        return this;
    }
    
    public Transaction setPayerAccount(Account payer) {
        this.payer_account = payer;
        return setPayerAccount(payer.getId());
    }

    public Transaction setPayeeAccount(int payee_account_id) {
        checkForChange(COLUMN_PAYEE_ACCOUNT_ID, this.payee_account_id, payee_account_id);
        this.payee_account_id = payee_account_id;
        return this;
    }
    
    public Transaction setPayeeAccount(Account payee) {
        this.payee_account = payee;
        return setPayeeAccount(payee.getId());
    }
    
    public Transaction setCreatedAt(Timestamp t) {
        checkForChange(COLUMN_CREATED_AT, this.created_at, t);
        this.created_at = t;
        return this;
    }
    
    @Override
    public void updateCreatedAt() {
        if (this.created_at == null) {
            this.created_at = Timestamp.valueOf(LocalDateTime.now());
        }
    }
    
    @Override
    public String[] getValues() {
        updateCreatedAt();
        return new String[]{
            /* Mivel a tr_* adattagok lehetnek nullok, ezeket üres stringgé kell alakítani. */
            Integer.toString(amount)
                , (tr_statement == null) ? "" : tr_statement
                , (tr_comment == null) ? "" : tr_comment
                , Integer.toString(payer_account_id)
                , Integer.toString(payee_account_id)
                , TM_FORMAT.format(created_at)
        };
    }

    @Override
    public void setValues(ResultSet rs) throws SQLException {
        is_new           = false;
        is_changed       = false;
        id               = rs.getInt("id");
        amount           = rs.getInt(COLUMN_NAMES[0]);
        tr_statement     = rs.getString(COLUMN_NAMES[1]);
        tr_comment       = rs.getString(COLUMN_NAMES[2]);
        payer_account_id = rs.getInt(COLUMN_NAMES[3]);
        payee_account_id = rs.getInt(COLUMN_NAMES[4]);
        created_at       = rs.getTimestamp(COLUMN_NAMES[5]);
    }
}

package models;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Account extends BaseModel {

    private static final int COLUMN_FEE = 2;
    private static final int COLUMN_CREDIT_LOAN = 3;
    private static final int COLUMN_CREDIT_INTEREST = 4;
    private static final int COLUMN_CREATED_AT = 5;
    private static final int COLUMN_CUSTOMER_ID = 6;

    private static final String COLUMN_NAMES[] = {
        "fee", "credit_loan", "credit_interest", "created_at", "customer_id"
    };

    private int fee               = 0;
    private int credit_loan       = 0;
    private float credit_interest = 0;
    private Timestamp created_at  = null;
    private int customer_id       = 0;
    private Customer customer     = null;

    private static ArrayList<Account> setupAccounts(ResultSet rs) throws SQLException {
        ArrayList<Account> all = new ArrayList<>();
        while (rs.next()) {
            Account c = new Account();
            c.setValues(rs);
            all.add(c);
        }
        return all;
    }
    
    /* ========~~~~--+ PUBLIKUS OSZTÁLYMETÓDUSOK +--~~~~======== */
    public static List<Account> getAll(int limit) throws SQLException {
        ResultSet rs = find(Account.tableName(), null, null, limit);
        return setupAccounts(rs);
    }

    public static Account findById(int id) throws SQLException {
        Account c = null;
        ResultSet rs = findSingle(Account.tableName(), "id", Integer.toString(id));
        if (rs.next()) {
            c = new Account();
            c.setValues(rs);
        }
        return c;
    }
    public static List<Account> findByCustomer(int customer_id, int limit) throws SQLException {
        ResultSet rs = findSingle(Account.tableName(), "customer_id", Integer.toString(customer_id), limit);
        return setupAccounts(rs);
    }

    public static List<Account> findByCustomer(Customer cust, int limit) throws SQLException {
        return findByCustomer(cust.getId(), limit);
    }

    public static List<Account> findByCustomer(Customer cust) throws SQLException {
        return findByCustomer(cust, 0);
    }

    public static String tableName() {
        return Account.class.getSimpleName();
    }

    /* ========~~~~--+ PUBLIKUS PÉLDÁNYMETÓDUSOK +--~~~~======== */
    public Account() {
        this(0, 0, 0);
    }

    public Account(int fee, int credit_loan, float credit_interest) {
        super(COLUMN_NAMES);
        this.fee = fee;
        this.credit_loan = credit_loan;
        this.credit_interest = credit_interest;
        is_new = true;
        is_changed = true;
    }

    public Card newCard() {
        Card card = new Card();
        card.setAccount(this);
        return card;
    }
    
    public Transaction newTransaction() {
        Transaction t = new Transaction();
        t.setPayerAccount(this);
        return t;
    }

    /* ========~~~~--+ PUBLIKUS LEKÉRDEZŐ METÓDUSOK +--~~~~======== */
    public int getFee()              { return this.fee;  }
    public int getCreditLoan()       { return this.credit_loan; }
    public float getCreditInterest() { return this.credit_interest; }
    public int getCustomerId()       { return this.customer_id; }
    public Timestamp getCreatedAt()  { return this.created_at; }
    
    public Customer getCustomer() throws SQLException {
        if (this.customer == null) {
            this.customer = Customer.findById(customer_id);
        }
        return this.customer;
    }
    
    public List<Card> getCards() throws SQLException {
        return Card.findByAccount(this);
    }

    @Override
    public boolean validate() {
        failForIf(fee < 0, "fee", "A díj nem lehet negatív");
        failForIf(credit_interest < 0, "credit_interest", "A hitel kamata nem lehet negatív");
        failForIf(credit_loan < 0, "credit_loan", "A hitelkeret nem lehet negatív");
        failForIf(customer_id == 0, "customer_id", "A számlának ügyfélhez kell tartoznia");
        return !hasErrors();
    }

    /* ========~~~~--+ PUBLIKUS BEÁLLÍTÓ METÓDUSOK +--~~~~======== */
    public Account setFee(int fee) {
        checkForChange(COLUMN_FEE, this.fee, fee);
        this.fee = fee;
        return this;
    }

    public Account setCreditLoan(int credit_loan) {
        checkForChange(COLUMN_CREDIT_LOAN, this.credit_loan, credit_loan);
        this.credit_loan = credit_loan;
        return this;
    }

    public Account setCreditInterest(float credit_interest) {
        checkForChange(COLUMN_CREDIT_INTEREST, this.credit_interest, credit_interest);
        this.credit_interest = credit_interest;
        return this;
    }

    public Account setCustomer(Customer cust) {
        setCustomer(cust.getId());
        this.customer = cust;
        return this;
    }

    public Account setCustomer(int customer_id) {
        checkForChange(COLUMN_CUSTOMER_ID, this.customer_id, customer_id);
        this.customer_id = customer_id;
        return this;
    }
    
    public Account setCreatedAt(Timestamp t) {
        checkForChange(COLUMN_CREATED_AT, this.created_at, t);
        this.created_at = t;
        return this;
    }
    
    public List<Transaction> getSentTransactions() throws SQLException {
        return Transaction.findByPayerAccount(this, 0);
    }
    
    public List<Transaction> getReceivedTransactions() throws SQLException {
        return Transaction.findByPayeeAccount(this, 0);
    }
    
    public List<Transaction> getTransactions() throws SQLException {
        List<Transaction> trs = new ArrayList<>();
        String acc_col_names[] = new String[] { "payer_account_id", "payee_account_id" };
        String acc_cols[] = new String[] { Integer.toString(getId()), Integer.toString(getId()) };
        ResultSet rs = find(Transaction.tableName(), acc_col_names, acc_cols);
        while (rs.next()) {
            Transaction tr = new Transaction();
            tr.setValues(rs);
            trs.add(tr);
        }
        return trs;
    }
    
    public int getAmount() {
        int amount = 0;
        try {
            List<Transaction> sent, recv;
            sent = getSentTransactions();
            recv = getReceivedTransactions();
            return Transaction.sumAmout(recv) - Transaction.sumAmout(sent);
        } catch (SQLException ex) {
            Logger.getLogger(Account.class.getName()).log(Level.SEVERE, null, ex);
        }
        return amount;
    }

    /* ========~~~~--+ MEZŐLEKÉRDEZŐ/BEÁLLÍTÓ FÜGGVÉNYEK +--~~~~======== */
    @Override
    public String[] getValues() {
        return new String[]{
            Integer.toString(fee), Integer.toString(credit_loan), Float.toString(credit_interest),
            TM_FORMAT.format(created_at), Integer.toString(customer_id)
        };
    }

    @Override
    public void setValues(ResultSet rs) throws SQLException {
        is_new = false;
        id = rs.getInt("id");
        fee = rs.getInt(COLUMN_NAMES[0]);
        credit_loan = rs.getInt(COLUMN_NAMES[1]);
        credit_interest = rs.getFloat(COLUMN_NAMES[2]);
        created_at = rs.getTimestamp(COLUMN_NAMES[3]);
        customer_id = rs.getInt(COLUMN_NAMES[4]);
    }

    @Override
    public void updateCreatedAt() {
        if (this.created_at == null) {
            this.created_at = Timestamp.valueOf(LocalDateTime.now());
        }
    }
}

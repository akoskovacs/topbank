class DBModel
    def self.connect(user, pass, host, db_name)
        db_params = {
            username: user,
            password: pass,
            host: 'localhost',
            database: db_name
        }
        begin
            @@db = Mysql2::Client.new(db_params)
        rescue Mysql2::Error => e
            puts e
            return false
        end
        return true
    end

    def self.pack_rows q, klass
       return nil if q.nil?
       rows = []
       q.each do |row|
        rows << klass.new(row)
       end
       return rows
    end

    def self.all
       cname = self.name
       begin
           q = @@db.query "SELECT * FROM `#{cname}`;"
       rescue Mysql2::Error => e
           puts e
           return nil
       end
       DBModel.pack_rows q, self
    end

    def self.find_by(by, what)
       cname = self.name
       str = "SELECT * FROM `#{cname}` WHERE `#{cname}`.`#{by.to_s}` = ?"
       begin
           qp = @@db.prepare str
           q = qp.execute(what)
       rescue Mysql2::Error => e
           puts e
           return nil
       end
       pack_rows q, self
    end
end

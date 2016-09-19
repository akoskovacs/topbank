class Tranzakcio < DBModel
    COLS = [:azonosito, :osszeg, :kozlony, :leiras, :kuldo, :fogado]
    COLS.each do |col|
        attr_accessor col
    end

    def initialize(params)
        COLS.each do |col|
            instance_variable_set("@#{col.to_s}", params[col.to_s])
        end
    end

    def find_by_id(id)
       pq = @@db.prepare 'SELECT * FROM `Ugyfel` WHERE Ugyfel.azonosito = ?;'
       q = pq.execute(id)
       Ugyfel.new q.first
    end

    def find_by_name(name)
       pq = @@db.prepare 'SELECT * FROM `Ugyfel` WHERE Ugyfel.nev = ?;'
       q = pq.execute(name)
       Ugyfel.new q.first
    end
    
    def self.belep(cred)
       u = find_by_id(cred[:id]) 
       return nil if u.nil?
       k = u.elso_kartya
       if (k.pin == cred[:pin] and k.ccv2 == cred[:ccv2])
        return u
       end
       return nil
    end


    def fogado_szamla

    end

    def save
       q = @@db.prepare 'INSERT INTO `Tranzakcio`(osszeg, koz, leir, kuldo, fogado) VALUES(?, ?, ?, ?, ?);'
       q.execute(@osszeg, @kozlony, @leiras, @kuldo, @fogado)
    end

    def self.all
       q = @@db.query 'SELECT * FROM `Ugyfel`;'
       DBModel.pack_rows q, Ugyfel
    end
end

class Ugyfel < DBModel
    COLS = [:azonosito, :nev, :fiok_kod, :irszam, :kezd_ido, :lakcim, :szigszam, :varos]
    COLS.each do |col|
        attr_accessor col
    end

    def initialize(params)
        COLS.each do |col|
            instance_variable_set("@#{col.to_s}", params[col.to_s])
        end
    end

    def self.find_by_id(id)
        find_by(:azonosito, id)
    end

    def self.find_by_name(name)
        find_by(:nev, name)
    end
    
    def self.belep(cred)
       begin

           str = "select * from Ugyfel where Ugyfel.azonosito = " +
           "(select Szamla.ugyfelkod from Szamla where Szamla.szamlaszam = " +
           "(select Kartya.szamlaszam from Kartya where Kartya.kartyaszam = ? and pin = ? and ccv2 = ?));";
           puts str
           qp = @@db.prepare str
           q = qp.execute(cred[:id], cred[:pin], cred[:ccv2])
       rescue Mysql2::Error => e
           puts e
           return false
       end
       k = q.first
       puts k
       puts cred
       return false if k.nil?
       return pack_rows q, Ugyfel
    end

    def fiok
        Fiok.find_by(:kod, fiok_kod)
    end

    def szamlak
        Szamla.find_by(:ugyfelkod, azonosito)
    end

    def kartyak
        sz = szamlak()
        kartyak = []
        sz.each do |szamla|
            k = szamla.kartyak
            kartyak += k if !k.nil?
        end
        return kartyak
    end

    def id
        azonosito
    end
end

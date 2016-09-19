class Szamla < DBModel
    COLS = %i(szamlaszam egyenleg adok szamla_dij nyitasi_ido ugyfelkod)
    COLS.each do |col|
        attr_accessor col
    end

    def initialize(params)
        COLS.each do |col|
            instance_variable_set("@#{col.to_s}", params[col.to_s])
        end
    end

    def self.find_by_owner(code)
       find_by(:szamlaszam, code)
    end

    def ugyfel
        Ugyfel.find_by(:azonosito, ugyfelkod)
    end

    def kartyak
        Kartya.find_by(:szamlaszam, szamlaszam)
    end

    def tranzakciok
        Tranzakcio.find_by(:kuldo, szamlaszam)
    end
end

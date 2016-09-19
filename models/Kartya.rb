class Kartya < DBModel
    COLS = [:kartyaszam, :dij, :pin, :ccv2, :lejarati_ido, :nyitasi_ido, :szamlaszam]
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

    def szamla
        Szamla.find_by(:szamlaszam, szamlaszam)
    end

    def ugyfel
        sz = szamla()[0]
        sz.ugyfel
    end
end

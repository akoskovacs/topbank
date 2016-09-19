class Fiok < DBModel
    COLS = [:kod, :varos, :cim, :irszam]
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

    def ugyfelek
        Ugyfel.find_by(:fiok_kod, kod)
    end

    def save
        return false
    end

    def teljes_cim
        cim.gsub(',', '') + ', ' + varos
    end
end

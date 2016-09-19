CREATE TABLE IF NOT EXISTS Fiok(
        kod			INT PRIMARY KEY NOT NULL AUTO_INCREMENT,
        varos		VARCHAR(40) NOT NULL,
        cim			VARCHAR(40) NOT NULL,
	    irszam		INT NOT NULL
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS Ugyfel(
        azonosito   INT PRIMARY KEY NOT NULL AUTO_INCREMENT,
        szigszam	VARCHAR(40) NOT NULL,
        lakcim		VARCHAR(40) NOT NULL,
		varos		VARCHAR(40) NOT NULL,
	    irszam		INT NOT NULL,
		kezd_ido    DATE NOT NULL,
		fiok_kod	INT NOT NULL,
        INDEX       (fiok_kod),
		FOREIGN KEY(fiok_kod) REFERENCES Fiok(kod)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS Szamla(
        `szamlaszam`  INT PRIMARY KEY NOT NULL AUTO_INCREMENT,
        `egyenleg`	INT NOT NULL,
        `adok`		INT NOT NULL,
		`szamla_dij`	INT NOT NULL,
		`nyitasi_ido` DATE NOT NULL,
		`ugyfelkod`   INT NOT NULL,
        INDEX       (`ugyfelkod`),
		FOREIGN KEY(`ugyfelkod`) REFERENCES Ugyfel(`azonososito`)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS Kartya(
        kartyaszam  INT PRIMARY KEY NOT NULL AUTO_INCREMENT,
        dij			INT NOT NULL,
        pin 		INT NOT NULL,
		lejarati_ido DATE NOT NULL,
	    nyitasi_ido DATE NOT NULL,
		szszam  INT NOT NULL,
        INDEX       (`szszam`),
		FOREIGN KEY(szszam) REFERENCES Szamla(szamlaszam)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS Tranzakcio(
        azonosito  INT PRIMARY KEY NOT NULL AUTO_INCREMENT,
        osszeg     INT NOT NULL,
        kozlony    VARCHAR(255),
		leiras     VARCHAR(255),
	    kuldo 	   INT NOT NULL,
		fogado	   INT NOT NULL,

        INDEX       (kuldo, fogado),
		FOREIGN KEY(kuldo) REFERENCES Szamla(szamlaszam),
		FOREIGN KEY(fogado) REFERENCES Szamla(szamlaszam)
) ENGINE = InnoDB;


SELECT nev, osszeg, kuldo, fogado FROM Ugyfel, Tranzakcio 
	WHERE Ugyfel.azonosito = Tranzakcio.kuldo AND Tranzakcio.osszeg > 1000000;

SELECT nev, varos, nyitasi_ido FROM Ugyfel, Szamla 
	WHERE Ugyfel.azonosito = Szamla.ugyfelkod 
		AND Szamla.nyitasi_ido < '2000-01-01';

SELECT szamlaszam, kartyaszam, lejarati_ido FROM Szamla, Kartya 
	WHERE Kartya.szamlaszam = Szamla.szamlaszam 
		AND Kartya.pin = 1234 OR Kartya.pin = 0000 OR Kartya.pin = 1111;


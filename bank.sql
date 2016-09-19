CREATE DATABASE IF NOT EXISTS bank;
USE bank;
SET FOREIGN_KEY_CHECKS=0;

CREATE TABLE IF NOT EXISTS `Fiok` (
  `kod` int(11) NOT NULL AUTO_INCREMENT,
  `varos` varchar(40) NOT NULL,
  `cim` varchar(40) NOT NULL,
  `irszam` int(11) NOT NULL,
  PRIMARY KEY (`kod`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `Ugyfel` (
  `azonosito` int(11) NOT NULL AUTO_INCREMENT,
  `nev` varchar(40) NOT NULL,
  `szigszam` varchar(40)  NOT NULL,
  `lakcim` varchar(40)  NOT NULL,
  `varos` varchar(40) NOT NULL,
  `irszam` int(11) NOT NULL,
  `kezd_ido` date NOT NULL,
  `fiok_kod` int(11) NOT NULL,
  PRIMARY KEY (`azonosito`),
  FOREIGN KEY (`fiok_kod`) REFERENCES `Fiok`(`kod`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `Szamla` (
  `szamlaszam` int(11) NOT NULL,
  `egyenleg` int(11) NOT NULL,
  `adok` int(11) NOT NULL,
  `szamla_dij` int(11) NOT NULL,
  `nyitasi_ido` date NOT NULL,
  `ugyfelkod` int(11) NOT NULL,
  PRIMARY KEY (`szamlaszam`),
  
  FOREIGN KEY (`ugyfelkod`) REFERENCES `Ugyfel`(`azonosito`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `Kartya` (
  `kartyaszam` int(11) NOT NULL,
  `dij` int(11) NOT NULL,
  `pin` int(11) NOT NULL,
  `ccv2`int(11) NOT NULL,
  `lejarati_ido` date NOT NULL,
  `nyitasi_ido` date NOT NULL,
  `szamlaszam` int(11) NOT NULL,
  PRIMARY KEY (`kartyaszam`),
  FOREIGN KEY (`szamlaszam`) REFERENCES `Szamla`(`szamlaszam`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `Tranzakcio` (
  `azonosito` int(11) NOT NULL AUTO_INCREMENT,
  `osszeg` int(11) NOT NULL,
  `kozlony` varchar(120) DEFAULT NULL,
  `leiras` varchar(120) DEFAULT NULL,
  `kuldo` int(11) NOT NULL,
  `fogado` int(11) NOT NULL,
  PRIMARY KEY (`azonosito`),
  FOREIGN KEY (`kuldo`) REFERENCES `Szamla`(`szamlaszam`),
  FOREIGN KEY (`fogado`) REFERENCES `Szamla`(`szamlaszam`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO `Fiok` (`varos`, `cim`, `irszam`) VALUES
('Budapest', 'Károly körút, 42', 1006);

INSERT INTO `Fiok` (`varos`, `cim`, `irszam`) VALUES
('Budapest', 'Tétényi út, 12', 1008);

INSERT INTO `Fiok` (`varos`, `cim`, `irszam`) VALUES
('Budapest', 'Favágó sor, 9', 1010);

INSERT INTO `Fiok` (`varos`, `cim`, `irszam`) VALUES
('Szeged', 'Londoni körút, 11', 6700);

INSERT INTO `Fiok` (`varos`, `cim`, `irszam`) VALUES
('Nyíregyháza', 'Kis Gábor utca, 7', 4000);

INSERT INTO `Fiok` (`varos`, `cim`, `irszam`) VALUES
('Szentendre', 'Bartók Imre út, 29', 1110);

INSERT INTO `Fiok` (`varos`, `cim`, `irszam`) VALUES
('Miskolc', 'Török út, 92', 3000);

INSERT INTO `Fiok` (`varos`, `cim`, `irszam`) VALUES
('Debrecen', 'Madách Imre út, 29', 2000);

INSERT INTO `Szamla` (`szamlaszam`, `egyenleg`, `adok`, `szamla_dij`, `nyitasi_ido`, `ugyfelkod`) VALUES
(872192822, 120000, 1, 1200, '2015-11-05', 1),
(873612811, 5000, 10, 500, '2005-01-10',    3),
(871322122, 122010, 10, 1000, '2000-11-02', 3),
(871691515, 122030, 10, 1000, '1997-09-12', 4),
(871691513, 2212000, 5, 2000, '2000-11-24', 2),
(872301150, 22010, 10, 1000, '2000-02-10', 9),
(871691556, 9099, 1,  4000, '2000-11-02', 10),
(871856159, 41000, 10, 4000, '2007-03-27', 8),
(871441978, 121110, 10, 8000, '2003-11-02', 8),
(871941908, 11900, 10, 1000, '2010-11-02', 7),
(871839128, 120900, 1, 1000, '2011-11-02', 6),
(871836333, 144000, 5, 1000, '2012-11-02', 6);

INSERT INTO `Ugyfel` (`nev`, `szigszam`, `lakcim`, `varos`, `irszam`, `kezd_ido`, `fiok_kod`) VALUES
('Kis Ádám György',    '837429874', 'Dorozsmai út, 1', 'Pápa', 6002, '2012-11-14', 1),
('Bócsi Tamás',        '811120012', 'Nagy Janó út, 7', 'Szeged', 1344, '2000-11-04', 4),
('Kis Ádám György',    '284268424', 'Petőfi út, 9', 'Budapest', 1006, '2001-09-14', 4),
('Kiss Göngyi Csilla', '973922842', 'Berlini körút, 11', 'Szeged', 6700, '1995-05-06', 1),
('Papai Anna',         '614631463', 'Tisza Lajos út, 30', 'Budapest', 1001, '1990-01-10', 2),
('Lakatos Mihály',     '164164316', 'Budapesti út, 22', 'Kisvárda', 4620, '2008-03-13', 5),
('Keresztes Zsolt',    '972332441', 'Széchenyi út, 13', 'Záhony', 4625, '2010-04-25', 5),
('Janó Anett',         '518741966', 'Mexikói út, 9', 'Miskolc', 2200, '2015-07-29', 7),
('Árpád Péter',        '837429514', 'Herceg út, 15', 'Miskolc', 2200, '2011-07-19', 7),
('Fazekas Áron',       '899100273', 'Petőfi tér, 9', 'Budapest', 1003, '2012-04-14', 3),
('Trab Antal',         '114183838', 'Nagy Lajos út, 97', 'Budapest', 1007, '2011-05-21', 2);


INSERT INTO `Kartya` (`kartyaszam`, `dij`, `pin`, `ccv2`, `lejarati_ido`, `nyitasi_ido`, `szamlaszam`) VALUES
(986561231, 1, 1234, 923, '2016-03-04', '2001-11-14', 871836333),
(948237421, 10, 9999, 314, '2000-01-01', '1994-01-22', 871836333),
(152451223, 5, 2110, 112, '2012-03-20', '2010-09-20', 871441978),
(235723523, 2, 1231, 130, '2019-05-04', '2015-11-01', 871691513),
(234623641, 20, 1654, 149, '2020-01-01', '2015-10-01', 872192822),
(164361436, 10, 4234, 942, '2018-03-04', '2015-07-01', 986561231),
(981741524, 5, 9133, 262, '2016-01-04', '2015-01-01', 871941908),
(514351351, 10, 4321, 787, '2016-01-04', '2015-02-01', 871941908),
(638226724, 10, 5123, 726, '2017-01-04', '2015-06-01', 871441978);

INSERT INTO `Tranzakcio` (`osszeg`, `kozlony`, `leiras`, `kuldo`, `fogado`) VALUES
(11200, NULL, 'Befizetés', 871836333 ,871322122),
(55300, NULL, 'TESCO vásárlás',872192822, 871691513),
(120090, NULL, NULL, 871691513, 871441978),
(99000, NULL, NULL, 871691515, 871691513),
(5000, NULL, 'Pénzfelvétel', 871941908, 871941908),
(1000, NULL, 'MO-ZI jegyvásárlás',871441978 ,871941908),
(9000, NULL, NULL, 872301150,871691556 ),
(86900, NULL, NULL,  871941908, 871322122),
(16540, NULL, 'OFOTÉRT szemüvegvásárlás',873612811, 871839128);

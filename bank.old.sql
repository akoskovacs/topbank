CREATE DATABASE IF NOT EXISTS bank;
USE bank;

CREATE TABLE IF NOT EXISTS `Fiok` (
  `kod` int(11) NOT NULL AUTO_INCREMENT,
  `varos` varchar(40) NOT NULL,
  `cim` varchar(40) NOT NULL,
  `irszam` int(11) NOT NULL,
  PRIMARY KEY (`kod`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8;


INSERT INTO `Fiok` (`varos`, `cim`, `irszam`) VALUES
('Szeged', 'Londoni körút, 11', 6700);
INSERT INTO `Fiok` (`varos`, `cim`, `irszam`) VALUES
('Budapest', 'Madách Imre út, 29', 1075);

CREATE TABLE IF NOT EXISTS `Szamla` (
  `szamlaszam` int(11) NOT NULL,
  `egyenleg` int(11) NOT NULL,
  `adok` int(11) NOT NULL,
  `szamla_dij` int(11) NOT NULL,
  `nyitasi_ido` date NOT NULL,
  `ugyfelkod` int(11) NOT NULL,
  PRIMARY KEY (`szamlaszam`),
  FOREIGN KEY (`ugyfelkod`) REFERENCES `Ugyfel`(`ugyfelkod`)
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


CREATE TABLE IF NOT EXISTS `Kartya` (
  `kartyaszam` int(11) NOT NULL,
  `dij` int(11) NOT NULL,
  `pin` int(11) NOT NULL,
  `lejarati_ido` int(11) NOT NULL,
  `nyitasi_ido` int(11) NOT NULL,
  `szamlaszam` int(11) NOT NULL,
  PRIMARY KEY (`kartyaszam`),
  FOREIGN KEY (`szamlaszam`) REFERENCES `Szamla`(`szamlaszam`)
      ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `Tranzakcio` (
  `azonosito` int(11) NOT NULL AUTO_INCREMENT,
  `osszeg` int(11) NOT NULL,
  `kozlony` int(11) DEFAULT NULL,
  `leiras` int(11) DEFAULT NULL,
  `kuldo` int(11) NOT NULL,
  `fogado` int(11) NOT NULL,
  PRIMARY KEY (`azonosito`),
  FOREIGN KEY (`kuldo`) REFERENCES `Szamla`(`szamlaszam`)
      ON DELETE CASCADE,
  FOREIGN KEY (`fogado`) REFERENCES `Szamla`(`szamlaszam`)
      ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


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
      ON DELETE CASCADE
) ENGINE=InnoDB  DEFAULT CHARSET=utf8;

INSERT INTO `Ugyfel` (`nev`, `szigszam`, `lakcim`, `varos`, `irszam`, `kezd_ido`, `fiok_kod`) VALUES
('Nagy János', '9832749283', 'Rácz út, 10', 'Nagyfalva', 1222, '2015-11-01', 1);

ALTER TABLE `Kartya`
  ADD CONSTRAINT `Kartya_ibfk_1` FOREIGN KEY (`szamlaszam`) REFERENCES `Szamla` (`szamlaszam`);

ALTER TABLE `Szamla`
  ADD CONSTRAINT `Szamla_ibfk_1` FOREIGN KEY (`ugyfelkod`) REFERENCES `Ugyfel` (`azonosito`);

ALTER TABLE `Tranzakcio`
  ADD CONSTRAINT `Tranzakcio_ibfk_2` FOREIGN KEY (`fogado`) REFERENCES `Szamla` (`szamlaszam`),
  ADD CONSTRAINT `Tranzakcio_ibfk_1` FOREIGN KEY (`kuldo`) REFERENCES `Szamla` (`szamlaszam`);
  
ALTER TABLE `Ugyfel`
  ADD CONSTRAINT `Ugyfel_ibfk_1` FOREIGN KEY (`fiok_kod`) REFERENCES `Fiok` (`kod`);

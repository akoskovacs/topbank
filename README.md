# OnlineBank
![Tranzakciók](https://github.com/akoskovacs/topbank/blob/java/screenshots/account.png)
## Függőségek
### Telepítés Linuxon
Maven függőségkezelő
```
$ sudo apt-get install maven
```

### Java Programkönyvtárak
A NetBeans alaptelepítésben tartalmaz Maven-t. A függőségkezelő
képes telepíteni a pom.xml fájlban leírt Java könyvtárakat. Az alakalmazás
a következő Java library-ket használja:

* Spark (web framework)
* Jetty (A Sparkhoz használt beépített webszerver)
* log4j (A Spark loggere)
* Freemarker (HTML sablonkönyvtár)
* HSQL

## Fordítás
A fordítás a Maven parancssori felületén keresztül történik. Az alábbi parancs
letölti a fent említett '.jar' állományokat, beleértve a frameworkot és
annak webszerverét. 

Ezután lefordítja az src/ könyvtárban található forráskódot és mind a kapott objektumfájlokat, mind azok fűggőségeit a 'target/OnlineBank-1.0-jar-with-dependencies.jar' futtatható Java állományba pakolja.

```
$ mvn compile assembly:single
```

## Futtatás
A futtatás a szokott módon történik a
```
$ java -jar target/OnlineBank-1.0-jar-with-dependencies.jar  
```
parancs kiadásával.

Ha a port szabad a webalkalmazás a http://localhost:4567/ címen nyitható meg.

## Felépítés
* src/java/controllers/: Java (MVC) vezérlők
* src/resources/public/: Statikus fájlok (CSS, JavaScript, statikus HTML)
* target/: Lefordított állományok
* views/: (MVC) Freemarker HTML nézetsablonok
* pom.xml: Maven konfiguráció (projektnév, Java függőségek leírása) 
![Új tranzakció](https://github.com/akoskovacs/topbank/blob/java/screenshots/transaction-new.png)

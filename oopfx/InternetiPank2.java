package oopfx;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Random;

public class InternetiPank2 extends Application {

    private HashMap<String, String> kasutajad = new HashMap<>();

    private ArrayList<String> tehingud = new ArrayList<>();
    private Label saldoSilt;
    private String praeguneKasutaja;
    private String praeguneKontoNr;
    private String praeguneParool;

    public static void main(String[] args) {
        launch(args);
    }



    private void näitaSisselogimisDialoogi(Stage lava) {
        laeKontod(); //Kontode laadimine failist kontod.txt kasutajad hashmapi.
        Stage sisselogimisAken = new Stage();
        sisselogimisAken.setTitle("Sisselogimine");

        GridPane sisselogimiseRuudustik = new GridPane();
        sisselogimiseRuudustik.setPadding(new Insets(10, 10, 10, 10));
        sisselogimiseRuudustik.setVgap(8);
        sisselogimiseRuudustik.setHgap(10);

        Label kasutajanimiSilt = new Label("Kasutajanimi:");
        GridPane.setConstraints(kasutajanimiSilt, 0, 0);
        TextField kasutajanimeVäli = new TextField();
        GridPane.setConstraints(kasutajanimeVäli, 1, 0);

        Label parooliSilt = new Label("Parool:");
        GridPane.setConstraints(parooliSilt, 0, 1);
        PasswordField parooliVäli = new PasswordField();
        GridPane.setConstraints(parooliVäli, 1, 1);

        Button sisselogimiseNupp = new Button("Logi sisse");
        GridPane.setConstraints(sisselogimiseNupp, 1, 2);
        sisselogimiseNupp.setOnAction(e -> {
            String kasutajanimi = kasutajanimeVäli.getText();
            String parool = parooliVäli.getText();
            if (autentimine(kasutajanimi, parool)) {
                praeguneKasutaja = kasutajanimi; //Väärtustatakse muutujad kasutajanime ja parooliga, et neid mujal kasutada saaks.
                praeguneParool = parool;
                // Tehingute faili loomine uue kasutajaga seotud nimega
                looTehinguFail(kasutajanimi);
//                loadTransactions(); //Ebavajalik sest menüü näitamisel seda ujuba tehakse.
                näitaPeamenüüd(lava); // Peamise stseeni kuvamine pärast sisselogimist
                sisselogimisAken.close();
            } else {
                näitaTeavitus(Alert.AlertType.ERROR, "Viga", "Vale kasutajanimi või parool!");
            }
        });

        Button looKontoNupp = new Button("Loo uus konto");
        GridPane.setConstraints(looKontoNupp, 1, 3);
        looKontoNupp.setOnAction(e -> { //Kui soovitakse luua uut kontot.
            String kasutajanimi = kasutajanimeVäli.getText();
            String parool = parooliVäli.getText();
            if (kasutajanimi.isEmpty() || parool.isEmpty()) {
                näitaTeavitus(Alert.AlertType.ERROR, "Viga", "Kasutajanimi ja parool peavad olema täidetud!");
            } else if (kasutajad.containsKey(kasutajanimi)) {
                näitaTeavitus(Alert.AlertType.ERROR, "Viga", "Sellise kasutajanimega konto on juba olemas!");
            } else {
                kasutajad.put(kasutajanimi, parool);
                praeguneKasutaja = kasutajanimi;
                näitaTeavitus(Alert.AlertType.INFORMATION, "Info", "Uus konto loodud edukalt!");
                näitaPeamenüüd(lava); // Peamise stseeni kuvamine pärast sisselogimist
                sisselogimisAken.close();
            }
        });

        sisselogimiseRuudustik.getChildren().addAll(kasutajanimiSilt, kasutajanimeVäli, parooliSilt, parooliVäli, sisselogimiseNupp, looKontoNupp);
        Scene sisselogimiseStseen = new Scene(sisselogimiseRuudustik, 300, 200);
        sisselogimisAken.setScene(sisselogimiseStseen);
        sisselogimisAken.show();
    }




    private boolean autentimine(String kasutajanimi, String parool) {
        return kasutajad.containsKey(kasutajanimi) && kasutajad.get(kasutajanimi).equals(parool);
    }

    private void näitaPeamenüüd(Stage lava) {
        loadTransactions(praeguneKasutaja); // Kasutaja konkreetse faili laadimine

        // Arvutab algse saldo
        double algneSaldo = arvutaAlgseSaldo(praeguneKasutaja);

        GridPane ruudustik = new GridPane();
        ruudustik.setPadding(new Insets(10, 10, 10, 10));
        ruudustik.setVgap(8);
        ruudustik.setHgap(10);

//Siltide ja nuppude seadistused.
        saldoSilt = new Label("Saldo: " + String.format("%.2f", algneSaldo) + " eurot");
        GridPane.setConstraints(saldoSilt, 0, 0);

        Button ülekanneNupp = new Button("Ülekanne");
        GridPane.setConstraints(ülekanneNupp, 1, 0);
        ülekanneNupp.setOnAction(e -> näitaÜlekandeDialoogi());

        Button pangalotoNupp = new Button("Pangaloto");
        GridPane.setConstraints(pangalotoNupp, 0, 1);
        pangalotoNupp.setOnAction(e -> mängiPangaloto());

        Button sulgeNupp = new Button("Sulge programm");
        GridPane.setConstraints(sulgeNupp, 1, 1);
        sulgeNupp.setOnAction(e -> {
            // Salvesta tehingud faili
            salvestaTehingud(praeguneKasutaja);
            salvestaKontod();
            lava.close();
        });
        Button kasutajaAndmeteNupp = new Button("Minu andmed");
        GridPane.setConstraints(kasutajaAndmeteNupp, 0, 3);
        kasutajaAndmeteNupp.setOnAction(e -> näitaKasutajaAndmeteDialoogi());
        ruudustik.getChildren().add(kasutajaAndmeteNupp);


        ruudustik.getChildren().addAll(saldoSilt, ülekanneNupp, pangalotoNupp, sulgeNupp);

        Scene stseen = new Scene(ruudustik, 300, 200);
        lava.setScene(stseen);
        lava.show();
    }

    @Override
    public void start(Stage lava) {
        lava.setTitle("Internetipank");
        //test kasutaja
//        kasutajad.put("kasutaja1", "salasõna1");

        // Sisse logimise akna näitamine
        näitaSisselogimisDialoogi(lava);
    }
    private void loadTransactions(String kasutajanimi) {
        // Tehingute laadimine konkreetse kasutaja failist
        try {
            FileReader failiLugeja = new FileReader("tehingud_" + kasutajanimi + ".txt");
            BufferedReader puhverLugeja = new BufferedReader(failiLugeja);
            String rida;
            while ((rida = puhverLugeja.readLine()) != null) {
                tehingud.add(rida);
            }
            puhverLugeja.close();
        } catch (IOException e) {
            System.out.println("Tehingute faili lugemisel ilmnes viga kasutajale " + kasutajanimi + ": " + e.getMessage() + ". Luuakse uus fail tehingud_" + kasutajanimi + ".txt");
            looTehinguFail(kasutajanimi); // Loob uue faili, kui seda ei leitud.
        }
    }

    private void laeKontod() {
        try {
            BufferedReader lugeja = new BufferedReader(new FileReader("kontod.txt"));
            String rida;
            while ((rida = lugeja.readLine()) != null) {
                String[] osad = rida.split(" ");
                if (osad.length == 2) {
                    kasutajad.put(osad[0], osad[1]);
                }
            }
            lugeja.close();
        } catch (IOException e) {
            System.out.println("Viga kontode laadimisel: " + e.getMessage());
        }
    }

    private void salvestaKontod() {
        try {
            PrintWriter kirjutaja = new PrintWriter(new FileWriter("kontod.txt"));
            for (String kasutajanimi : kasutajad.keySet()) {
                kirjutaja.println(kasutajanimi + " " + kasutajad.get(kasutajanimi));
            }
            kirjutaja.close();
        } catch (IOException e) {
            System.out.println("Viga kontode salvestamisel: " + e.getMessage());
        }
    }

    private void näitaTeavitus(Alert.AlertType tüüp, String pealkiri, String sisu) {
        Alert teavitus = new Alert(tüüp);
        teavitus.setTitle(pealkiri);
        teavitus.setHeaderText(null);
        teavitus.setContentText(sisu);
        teavitus.showAndWait();
    }

    private void looTehinguFail(String kasutajanimi) {
        // Tehingute faili loomine uue kasutajaga seotud nimega
        try {
            File fail = new File("tehingud_" + kasutajanimi + ".txt");
            if (fail.createNewFile()) {
                System.out.println("Uus tehingute fail loodud kasutajale: " + kasutajanimi);
            } else {
                System.out.println("Tehingute fail juba olemas kasutajale: " + kasutajanimi);
            }
        } catch (IOException e) {
            System.out.println("Viga tehingute faili loomisel: " + e.getMessage());
        }
    }


    private double arvutaAlgseSaldo(String kasutajanimi) {
        // Saldo arvutamine konkreetse kasutaja tehingute põhjal
        if (tehingud.isEmpty()) {
            tehingud.add("Panga kink uutele kasutajatele: +" + String.format("%.2f", 15.0) + " eurot"); //Panga kink uutele kasutajatele.
            return 15.0; // Kui faili pole, siis on konto seis 15 eurot, sest tegu on uue kasutajaga.

        }
        double saldo = 0.0;
        for (String tehing : tehingud) {
            if (tehing.contains("+")) {
                String[] osad = tehing.split("\\+");
                if (osad.length > 1) {
                    saldo += Double.parseDouble(osad[1].trim().replace(" eurot", "").replace(",", ".")); //Kui leidub , siis tehakse .
                }
            } else if (tehing.contains("-")) {
                String[] osad = tehing.split("-");
                if (osad.length > 1) {
                    saldo -= Double.parseDouble(osad[1].trim().replace(" eurot", "").replace(",", "."));
                }
            }
        }
        return saldo;
    }

    private void salvestaTehingud(String kasutajanimi) {
        try {
            PrintWriter kirjutaja = new PrintWriter(new FileWriter("tehingud_" + kasutajanimi + ".txt"));
            for (String tehing : tehingud) {
                kirjutaja.println(tehing);
            }
            kirjutaja.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void uuendaSaldo(double summa, Label saldoSilt) {
        double praeguneSaldo = Double.parseDouble(saldoSilt.getText().split(" ")[1].replace(" eurot", "").replace(",", "."));
        praeguneSaldo += summa;
        saldoSilt.setText("Saldo: " + String.format("%.2f", praeguneSaldo) + " eurot");
    }
    private void näitaÜlekandeDialoogi() {
        Stage ülekandeAken = new Stage();
        ülekandeAken.setTitle("Ülekanne");

        GridPane ülekandeRuudustik = new GridPane();
        ülekandeRuudustik.setPadding(new Insets(10, 10, 10, 10));
        ülekandeRuudustik.setVgap(8);
        ülekandeRuudustik.setHgap(10);

        Label saajaSilt = new Label("Saaja kontonumber:");
        GridPane.setConstraints(saajaSilt, 0, 0);
        TextField saajaVäli = new TextField();
        GridPane.setConstraints(saajaVäli, 1, 0);

        Label nimeSilt = new Label("Saaja nimi:");
        GridPane.setConstraints(nimeSilt, 0, 1);
        TextField nimeVäli = new TextField();
        GridPane.setConstraints(nimeVäli, 1, 1);

        Label summaSilt = new Label("Summa:");
        GridPane.setConstraints(summaSilt, 0, 2);
        TextField summaVäli = new TextField();
        GridPane.setConstraints(summaVäli, 1, 2);

        Button kinnitaNupp = new Button("Kinnita");
        GridPane.setConstraints(kinnitaNupp, 1, 3);
        kinnitaNupp.setOnAction(e -> {
            // Kinnita ülekanne ja sulge aken
            double summa = Double.parseDouble(summaVäli.getText());
            teeÜlekanne(saajaVäli.getText(), nimeVäli.getText(), summa);
            ülekandeAken.close();
        });

        ülekandeRuudustik.getChildren().addAll(saajaSilt, saajaVäli, nimeSilt, nimeVäli, summaSilt, summaVäli, kinnitaNupp);

        Scene ülekandeStseen = new Scene(ülekandeRuudustik, 300, 150);
        ülekandeAken.setScene(ülekandeStseen);
        ülekandeAken.show();
    }


    private void mängiPangaloto() {
        // Pangaloto loogika
        int juhuslikNumber = new Random().nextInt(100);
        if (juhuslikNumber == 0) {
            double võidusumma = 100000.0; // Võidusumma
            tehingud.add("Pangaloto võit: +" + String.format("%.2f", võidusumma) + " eurot");
            uuendaSaldo(võidusumma, saldoSilt);
        } else {
            tehingud.add("Pangaloto kaotus: -0.50 eurot");
            uuendaSaldo(-0.50, saldoSilt);
        }
    }

    private void teeÜlekanne(String saaja, String nimi, double summa) {
        if (saaja.isEmpty() || nimi.isEmpty() || summa == 0.0 || String.valueOf(summa).isEmpty()) { // summa väärtuse kontroll vajab parandamist kui ta tühi on
            näitaTeavitus(Alert.AlertType.ERROR, "Viga", "Väljad ei sa olla tühjad!");
        }
        // Kontroll, kas kontol on piisavalt raha
        if (summa <= Double.parseDouble(saldoSilt.getText().split(" ")[1].replace(" eurot", "").replace(",", "."))) {
            // Teosta ülekanne
            tehingud.add("Ülekanne " + saaja + " (" + nimi + "): -" + String.format(Locale.ENGLISH, "%.2f", summa) + " eurot");
            uuendaSaldo(-summa, saldoSilt);
        } else {
            // Kontol ei ole piisavalt raha
            Alert teavitus = new Alert(Alert.AlertType.ERROR);
            teavitus.setTitle("Viga");
            teavitus.setHeaderText(null);
            teavitus.setContentText("Kontol pole piisavalt raha selle ülekande jaoks!");
            teavitus.showAndWait();
        }
    }
    private void näitaKasutajaAndmeteDialoogi() {
        Alert andmeDialoog = new Alert(Alert.AlertType.INFORMATION);
        andmeDialoog.setTitle("Minu andmed");
        andmeDialoog.setHeaderText("Kasutajanimi: " + praeguneKasutaja+", Parool: " + praeguneParool);
//        andmeDialoog.setContentText("Kasutajanimi: " + praeguneKasutaja+", Parool: " + praeguneParool); // Siin võiks lisada ka teisi kasutajaandmeid vastavalt vajadusele.
        andmeDialoog.showAndWait();
    }


}

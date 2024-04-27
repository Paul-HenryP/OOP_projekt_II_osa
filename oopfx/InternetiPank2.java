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

    private HashMap<String, String> users = new HashMap<>();


    private ArrayList<String> transactions = new ArrayList<>();
    private Label balanceLabel;
    private String currentUser;

    public static void main(String[] args) {
        launch(args);
    }



    private void showLoginDialog(Stage primaryStage) {
        loadAccounts(); //Kontode laadimine failist kontod.txt users hashmapi.
        Stage loginStage = new Stage();
        loginStage.setTitle("Sisselogimine / Uue konto loomine");

        GridPane loginGrid = new GridPane();
        loginGrid.setPadding(new Insets(10, 10, 10, 10));
        loginGrid.setVgap(8);
        loginGrid.setHgap(10);

        Label usernameLabel = new Label("Kasutajanimi:");
        GridPane.setConstraints(usernameLabel, 0, 0);
        TextField usernameField = new TextField();
        GridPane.setConstraints(usernameField, 1, 0);

        Label passwordLabel = new Label("Parool:");
        GridPane.setConstraints(passwordLabel, 0, 1);
        PasswordField passwordField = new PasswordField();
        GridPane.setConstraints(passwordField, 1, 1);

        Button loginButton = new Button("Logi sisse");
        GridPane.setConstraints(loginButton, 1, 2);
        loginButton.setOnAction(e -> {
            String username = usernameField.getText();
            String password = passwordField.getText();
            if (authenticate(username, password)) {
                currentUser = username; //Väärtustatakse muutuja kasutajanimega, et seda mujal kasutada saaks.
                // Tehingute faili loomine uue kasutajaga seotud nimega
                createTransactionFile(username);
//                loadTransactions(); //Ebavajalik sest menüü näitamisel seda ujuba tehakse.
                showMainMenu(primaryStage); // Peamise stseeni kuvamine pärast sisselogimist
                loginStage.close();
            } else {
                showAlert(Alert.AlertType.ERROR, "Viga", "Vale kasutajanimi või parool!");
            }
        });

        Button createAccountButton = new Button("Loo uus konto");
        GridPane.setConstraints(createAccountButton, 1, 3);
        createAccountButton.setOnAction(e -> {
            String username = usernameField.getText();
            String password = passwordField.getText();
            if (username.isEmpty() || password.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Viga", "Kasutajanimi ja parool peavad olema täidetud!");
            } else if (users.containsKey(username)) {
                showAlert(Alert.AlertType.ERROR, "Viga", "Sellise kasutajanimega konto on juba olemas!");
            } else {
                users.put(username, password);
                currentUser = username;
                showAlert(Alert.AlertType.INFORMATION, "Info", "Uus konto loodud edukalt!");
                showMainMenu(primaryStage); // Peamise stseeni kuvamine pärast sisselogimist
                loginStage.close();
            }
        });

        loginGrid.getChildren().addAll(usernameLabel, usernameField, passwordLabel, passwordField, loginButton, createAccountButton);

        Scene loginScene = new Scene(loginGrid, 300, 200);
        loginStage.setScene(loginScene);
        loginStage.show();
    }




    private boolean authenticate(String username, String password) {
        return users.containsKey(username) && users.get(username).equals(password);
    }

    private void showMainMenu(Stage primaryStage) {
        // Peamine menüü peaks olema nähtav ainult siis, kui kasutaja on edukalt sisse loginud
        // Lae varasemad tehingud failist

//        loadTransactions();

        loadTransactions(currentUser); // Kasutaja konkreetse faili laadimine

        // Arvutab algne saldo
        double initialBalance = calculateInitialBalance(currentUser);

//        // Arvuta algne saldo
//        double initialBalance = calculateInitialBalance();

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10, 10, 10, 10));
        grid.setVgap(8);
        grid.setHgap(10);

        balanceLabel = new Label("Saldo: " + String.format("%.2f", initialBalance) + " eurot");
        GridPane.setConstraints(balanceLabel, 0, 0);

        Button transferButton = new Button("Ülekanne");
        GridPane.setConstraints(transferButton, 1, 0);
        transferButton.setOnAction(e -> showTransferDialog());

        Button lotteryButton = new Button("Pangaloto");
        GridPane.setConstraints(lotteryButton, 0, 1);
        lotteryButton.setOnAction(e -> playLottery());

        Button closeButton = new Button("Sulge programm");
        GridPane.setConstraints(closeButton, 1, 1);
        closeButton.setOnAction(e -> {
            // Salvesta tehingud faili
            saveTransactions(currentUser);
            saveAccounts();
            primaryStage.close();
        });

        grid.getChildren().addAll(balanceLabel, transferButton, lotteryButton, closeButton);

        Scene scene = new Scene(grid, 300, 200);
        primaryStage.setScene(scene);
        primaryStage.show();


    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Internetipank");

        //test kasutaja
//        users.put("kasutaja1", "salasõna1");

        // Sisse logimise aken

        showLoginDialog(primaryStage);
    }
    private void loadTransactions(String username) {
        // Tehingute laadimine konkreetse kasutaja failist
        try {
            FileReader fileReader = new FileReader("tehingud_" + username + ".txt");
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                transactions.add(line);
            }
            bufferedReader.close();
        } catch (IOException e) {
            System.out.println("Tehingute faili lugemisel ilmnes viga kasutajale " + username + ": " + e.getMessage() + ". Luuakse uus fail tehingud_" + username + ".txt");
            createTransactionFile(username); // Loome uue faili, kui seda ei leitud
        }
    }

    private void loadAccounts() {
        try {
            BufferedReader reader = new BufferedReader(new FileReader("kontod.txt"));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(" ");
                if (parts.length == 2) {
                    users.put(parts[0], parts[1]);
                }
            }
            reader.close();
        } catch (IOException e) {
            System.out.println("Viga kontode laadimisel: " + e.getMessage());
        }
    }

    private void saveAccounts() {
        try {
            PrintWriter writer = new PrintWriter(new FileWriter("kontod.txt"));
            for (String username : users.keySet()) {
                writer.println(username + " " + users.get(username));
            }
            writer.close();
        } catch (IOException e) {
            System.out.println("Viga kontode salvestamisel: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void createTransactionFile(String username) {
        // Tehingute faili loomine uue kasutajaga seotud nimega
        try {
            File file = new File("tehingud_" + username + ".txt");
            if (file.createNewFile()) {
                System.out.println("Uus tehingute fail loodud kasutajale: " + username);
            } else {
                System.out.println("Tehingute fail juba olemas kasutajale: " + username);
            }
        } catch (IOException e) {
            System.out.println("Viga tehingute faili loomisel: " + e.getMessage());
        }
    }


    private double calculateInitialBalance(String username) {
        // Saldo arvutamine konkreetse kasutaja tehingute järgi
        if (transactions.isEmpty()) {
            transactions.add("Panga kink uutele kasutajatele: +" + String.format("%.2f", 15.0) + " eurot"); //Panga kink uutele kasutajatele.
            return 15.0; // Kui faili pole, siis on konto seis 15 eurot

        }
        double balance = 0.0;
        for (String transaction : transactions) {
            if (transaction.contains("+")) {
                String[] parts = transaction.split("\\+");
                if (parts.length > 1) {
                    balance += Double.parseDouble(parts[1].trim().replace(" eurot", "").replace(",", ".")); //Kui leidub , siis tehakse .
                }
            } else if (transaction.contains("-")) {
                String[] parts = transaction.split("-");
                if (parts.length > 1) {
                    balance -= Double.parseDouble(parts[1].trim().replace(" eurot", "").replace(",", "."));
                }
            }
        }
        return balance;
    }

    private void saveTransactions(String username) {
        try {
            PrintWriter writer = new PrintWriter(new FileWriter("tehingud_" + username + ".txt"));
            for (String transaction : transactions) {
                writer.println(transaction);
            }
            writer.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void updateBalance(double amount, Label balanceLabel) {
        double currentBalance = Double.parseDouble(balanceLabel.getText().split(" ")[1].replace(" eurot", "").replace(",", "."));
        currentBalance += amount;
        balanceLabel.setText("Saldo: " + String.format("%.2f", currentBalance) + " eurot");
    }
    private void showTransferDialog() {
        Stage transferStage = new Stage();
        transferStage.setTitle("Ülekanne");

        GridPane transferGrid = new GridPane();
        transferGrid.setPadding(new Insets(10, 10, 10, 10));
        transferGrid.setVgap(8);
        transferGrid.setHgap(10);

        Label recipientLabel = new Label("Saaja kontonumber:");
        GridPane.setConstraints(recipientLabel, 0, 0);
        TextField recipientField = new TextField();
        GridPane.setConstraints(recipientField, 1, 0);

        Label nameLabel = new Label("Saaja nimi:");
        GridPane.setConstraints(nameLabel, 0, 1);
        TextField nameField = new TextField();
        GridPane.setConstraints(nameField, 1, 1);

        Label amountLabel = new Label("Summa:");
        GridPane.setConstraints(amountLabel, 0, 2);
        TextField amountField = new TextField();
        GridPane.setConstraints(amountField, 1, 2);

        Button confirmButton = new Button("Kinnita");
        GridPane.setConstraints(confirmButton, 1, 3);
        confirmButton.setOnAction(e -> {
            // Kinnita ülekanne ja sulge aken
            double amount = Double.parseDouble(amountField.getText());
            makeTransfer(recipientField.getText(), nameField.getText(), amount);
            transferStage.close();
        });

        transferGrid.getChildren().addAll(recipientLabel, recipientField, nameLabel, nameField, amountLabel, amountField, confirmButton);

        Scene transferScene = new Scene(transferGrid, 300, 150);
        transferStage.setScene(transferScene);
        transferStage.show();
    }


    private void playLottery() {
        // Pangaloto loogika
        int randomNumber = new Random().nextInt(100);
        if (randomNumber == 0) {
            double winAmount = 100000.0; // Võidusumma
            transactions.add("Pangaloto võit: +" + String.format("%.2f", winAmount) + " eurot");
            updateBalance(winAmount, balanceLabel);
        } else {
            transactions.add("Pangaloto kaotus: -0.50 eurot");
            updateBalance(-0.50, balanceLabel);
        }
    }

    private void makeTransfer(String recipient, String name, double amount) {
        if (recipient.isEmpty() || name.isEmpty() || amount == 0.0 || String.valueOf(amount).isEmpty()) { // amount väärtuse kontroll vajab parandamist kui ta tühi on
            showAlert(Alert.AlertType.ERROR, "Viga", "Väljad ei sa olla tühjad!");
        }
        // Kontrolli, kas kontol on piisavalt raha
        if (amount <= Double.parseDouble(balanceLabel.getText().split(" ")[1].replace(" eurot", "").replace(",", "."))) {
            // Teosta ülekanne
            transactions.add("Ülekanne " + recipient + " (" + name + "): -" + String.format(Locale.ENGLISH, "%.2f", amount) + " eurot");
            updateBalance(-amount, balanceLabel);
        } else {
            // Kontol ei ole piisavalt raha
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Viga");
            alert.setHeaderText(null);
            alert.setContentText("Kontol pole piisavalt raha selle ülekande jaoks!");
            alert.showAndWait();
        }
    }
}
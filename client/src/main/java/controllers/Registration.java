package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import nettwork.NetworkNetty;
import utils.Command;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;


public class Registration implements Initializable {
    private NetworkNetty network;
    private boolean isAuthOK;

    @FXML
    Button enter;

    @FXML
    TextField addLogin;

    @FXML
    TextField addPassword;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        network = new NetworkNetty((args) -> {

            if (args[0] instanceof Command) {
                Command cmd = (Command) args[0];
                switch (cmd.getType()) {
                    case AUTH_OK: {
                        isAuthOK = true;
                        String pathHome = resources.toString();
                        network.sendCommand(Command.generate(Command.CommandType.LIST));
                        break;
                    }
                }
            }
        });
    }

    public void btnEnterCloudAction(ActionEvent actionEvent) throws IOException {
        String log = addLogin.getText().trim().equals("") ? null : addLogin.getText();
        String pass = addPassword.getText().trim().equals("") ? null : addPassword.getText();
        if (log != null && pass != null) {
            network.sendCommand(Command.generate(Command.CommandType.REGISTER, log, pass));
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Укажите логин и пароль", ButtonType.OK);
            alert.showAndWait();
        }
        if(isAuthOK) {
            Stage stage = (Stage) enter.getScene().getWindow();
            stage.close();
            Parent second = FXMLLoader.load(getClass().getClassLoader().getResource("main.fxml"));
            Stage secondWindow = new Stage();
            secondWindow.setTitle("Cloud");
            secondWindow.initModality(Modality.WINDOW_MODAL);
            secondWindow.setScene(new Scene(second, 800, 600));
            secondWindow.show();
        }
    }


}

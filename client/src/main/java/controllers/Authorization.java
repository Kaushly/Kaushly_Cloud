package controllers;

import javafx.application.Platform;
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
import utils.FileInfo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;
import java.util.ResourceBundle;

public class Authorization implements Initializable {
    private NetworkNetty network;
    private Command cmd;
    private String remotePathField;

    @FXML
    Button open;

    @FXML
    Button reg;

    @FXML
    TextField login;

    @FXML
    TextField password;

    private boolean isAuthOK;

    public void btnEnterCloudAction(ActionEvent actionEvent) throws IOException {
        network.sendCommand(Command.generate(Command.CommandType.AUTH, login.getText(), password.getText()));
        if(isAuthOK) {
            Stage stage = (Stage) open.getScene().getWindow();
            stage.close();
            Parent second = FXMLLoader.load(getClass().getResource("/main.fxml"));
            Stage secondWindow = new Stage();
            secondWindow.setTitle("Cloud");
            secondWindow.initModality(Modality.WINDOW_MODAL);
            secondWindow.setScene(new Scene(second, 800, 600));
            secondWindow.show();
        }
    }

    public void btnRegAction(ActionEvent actionEvent) throws IOException {
        Stage stage = (Stage) reg.getScene().getWindow();
        stage.close();
        Parent reg = FXMLLoader.load(getClass().getResource("/regist.fxml"));
        Stage secondWindow = new Stage();
        secondWindow.setTitle("Регистрация");
        secondWindow.initModality(Modality.APPLICATION_MODAL);
        secondWindow.setScene(new Scene(reg, 300, 200));
        secondWindow.show();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        network = new NetworkNetty((args) -> {

            if (args[0] instanceof Command) {
                cmd = (Command) args[0];
                switch (cmd.getType()) {
                    case AUTH_OK: {
                        isAuthOK = true;
                        remotePathField = cmd.getArgs()[0];
                        network.sendCommand(Command.generate(Command.CommandType.LIST));
                        break;
                    }
                }
            }
        });
    }

}

package client;

import controllers.Controller;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class ClientApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/auth.fxml"));
        Parent root = loader.load();
//        Controller controller = loader.getController();
        primaryStage.setTitle("Клиент облачного хранилища");
        primaryStage.setScene(new Scene(root, 200, 300));

//        primaryStage.setOnHidden(e -> controller.menuExitClick(null));

        primaryStage.show();
    }

    public static void main(String[] args) throws IOException {
        launch();
    }
}

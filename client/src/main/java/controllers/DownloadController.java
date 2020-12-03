package controllers;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import utils.FileInfo;

import java.io.IOException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class DownloadController implements Initializable {

    private Path load = Paths.get("/Users/mark/Documents");
    private Path dst;
    private Controller controller;

    public void setParent (Controller controller){
        this.controller = controller;
    }


    @FXML
    TableView<FileInfo> downloadFiles;

    @FXML
    TextField pathDownload;

    @FXML
    ComboBox<String> discBox;

    @FXML
    Button close;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        TableColumn<FileInfo, String> fileTypeColumn = new TableColumn<>();
        fileTypeColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getFileType().getName()));
        fileTypeColumn.setPrefWidth(24);

        TableColumn<FileInfo, String> fileNameColumn = new TableColumn<>("Имя");
        fileNameColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getFilename()));
        fileNameColumn.setPrefWidth(240);

        TableColumn<FileInfo, Long> fileSizeColumn = new TableColumn<>("Размер файла");
        fileSizeColumn.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getSize()));
        fileSizeColumn.setCellFactory(column -> {
            return new TableCell<FileInfo, Long>() {
                @Override
                protected void updateItem(Long item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item == null || empty) {
                        setText(null);
                        setStyle("");
                    } else {
                        String text = String.format("%,d bytes", item);
                        if (item == -1L) {
                            text = "[DIR]";
                        }
                        setText(text);
                    }
                }
            };
        });
        fileSizeColumn.setPrefWidth(120);

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        TableColumn<FileInfo, String> fileDataColumn = new TableColumn<>("Дата изменения");
        fileDataColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getLastModified().format(dtf)));
        fileDataColumn.setPrefWidth(240);

        downloadFiles.getColumns().addAll(fileTypeColumn, fileNameColumn, fileSizeColumn, fileDataColumn);
        downloadFiles.getSortOrder().add(fileTypeColumn);

        discBox.getItems().clear();
        for (Path p : FileSystems.getDefault().getRootDirectories()) {
            discBox.getItems().add(p.toString());
        }
        discBox.getSelectionModel().select(0);


        downloadFiles.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (event.getClickCount() == 2) {
                    Path path = Paths.get(pathDownload.getText()).resolve(downloadFiles.getSelectionModel().getSelectedItem().getFilename());
                    if (Files.isDirectory(path)) {
                        updateList(path);
                    }
                }
            }
        });
        updateList(load);
    }

    public void updateList(Path path) {
        try {
            pathDownload.setText(path.normalize().toString());
            downloadFiles.getItems().clear();
            downloadFiles.getItems().addAll(Files.list(path.normalize()).map(FileInfo::new).collect(Collectors.toList()));
            downloadFiles.sort();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "УПС", ButtonType.OK);
            alert.showAndWait();
        }
    }

    public void btnOkAction(ActionEvent actionEvent) {
        dst = Paths.get(pathDownload.getText());
        Stage stage = (Stage) close.getScene().getWindow();
        stage.close();
    }

    public void btnPathUpAction(ActionEvent actionEvent) {
        Path upperPath = Paths.get(pathDownload.getText()).getParent();
        if (upperPath != null) {
            updateList(upperPath);
        }
    }

    public void btnGoHomeAction(ActionEvent actionEvent) {
        updateList(load);
    }

    public Path getDst() {
        return dst;
    }


    public String getSelectedFileName() {
        if (!downloadFiles.isFocused()) {
            return null;
        }
        return downloadFiles.getSelectionModel().getSelectedItem().getFilename();
    }

}

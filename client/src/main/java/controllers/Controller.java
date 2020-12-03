package controllers;

import client.ClientApp;
import io.netty.handler.ssl.ClientAuth;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import nettwork.CommandInboundHandler;
import nettwork.NetworkNetty;
import utils.CallBack;
import utils.Command;
import utils.FileInfo;

import java.io.IOException;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class Controller implements Initializable {

    private Path home;
    private Path dst;
    private Path src;
    private DownloadController downloadController;
    private Command cmd;

    @FXML
    TableView<FileInfo> filesTables;

    @FXML
    TextField pathField;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        home = Paths.get(String.format("%s", pathField.getText()));

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

        filesTables.getColumns().addAll(fileTypeColumn, fileNameColumn, fileSizeColumn, fileDataColumn);
        filesTables.getSortOrder().add(fileTypeColumn);

        filesTables.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (event.getClickCount() == 2) {
                    Path path = Paths.get(pathField.getText()).resolve(filesTables.getSelectionModel().getSelectedItem().getFilename());
                    if (Files.isDirectory(path)) {
                        updateList(path);
                    }
                }
            }
        });
        updateList(home);


    }

    public void updateList(Path path) {
        try {
            if (!Files.exists(home)) {
                Files.createDirectory(home);
            }
            pathField.setText(path.normalize().toString());
            filesTables.getItems().clear();
            filesTables.getItems().addAll(Files.list(path).map(FileInfo::new).collect(Collectors.toList()));
            filesTables.sort();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Не удалось обновить список файлов", ButtonType.OK);
            alert.showAndWait();
        }
    }

    public void btnExitAction(ActionEvent actionEvent) {
        Platform.exit();
    }


    public void btnPathUpAction(ActionEvent actionEvent) {
        Path upperPath = Paths.get(pathField.getText()).getParent();
        if (upperPath != null) {
            updateList(upperPath);
        }
    }

    public void btnGoHomeAction(ActionEvent actionEvent) {
        updateList(home);
    }

    public void btnDeleteAction(ActionEvent actionEvent) {
        try {
            Path path = Paths.get(pathField.getText()).resolve(filesTables.getSelectionModel().getSelectedItem().getFilename());
            if (Files.isDirectory(path)) {
                Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        Files.delete(file);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                        Files.delete(dir);
                        return FileVisitResult.CONTINUE;
                    }
                });
            } else {
                Files.delete(path);
            }
            updateList(path.getParent());
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public void btnDownloadAction(ActionEvent actionEvent) {

        try {
            if (getSelectedFileName() == null) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Файл не выбран!", ButtonType.OK);
                alert.showAndWait();
            } else {
                Stage secondWindow = new Stage();
                FXMLLoader loader = new FXMLLoader();
                loader.setLocation(getClass().getResource("/load.fxml"));
                Parent second = loader.load();
                secondWindow.setTitle("Сохранить в ...");
                secondWindow.initModality(Modality.APPLICATION_MODAL);
                secondWindow.setScene(new Scene(second, 600, 400));
                secondWindow.showAndWait();

                downloadController = loader.getController();
                downloadController.setParent(this);

                if (downloadController.getDst() != null) {
                    Path src = Paths.get(pathField.getText()).resolve(filesTables.getSelectionModel().getSelectedItem().getFilename());
                    dst = Paths.get(downloadController.getDst().toString()).resolve(filesTables.getSelectionModel().getSelectedItem().getFilename());
                    if (Files.isDirectory(src)) {
                        Files.walkFileTree(src, new SimpleFileVisitor<Path>() {
                            @Override
                            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                                Path targetPath = dst.resolve(src.relativize(dir));
                                if (!Files.exists(targetPath)) {
                                    Files.createDirectory(targetPath);
                                }
                                return FileVisitResult.CONTINUE;
                            }

                            @Override
                            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                                Files.copy(file, dst.resolve(src.relativize(file)), StandardCopyOption.REPLACE_EXISTING);
                                return FileVisitResult.CONTINUE;
                            }

                        });
                    } else {
                        Files.copy(src, dst, StandardCopyOption.REPLACE_EXISTING);
                    }
                }
            }
        } catch (
                IOException e) {
            e.printStackTrace();
        }

    }

    public void btnMoveAction(ActionEvent actionEvent) {
        try {
            Stage secondWindow = new Stage();
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/load.fxml"));
            Parent second = loader.load();
            secondWindow.setTitle("Переместить в ...");
            secondWindow.initModality(Modality.APPLICATION_MODAL);
            secondWindow.setScene(new Scene(second, 600, 400));
            secondWindow.showAndWait();

            downloadController = loader.getController();
            downloadController.setParent(this);

            if (downloadController.getDst() != null) {
                Path src = Paths.get(pathField.getText()).resolve(filesTables.getSelectionModel().getSelectedItem().getFilename());
                dst = Paths.get(downloadController.getDst().toString()).resolve(filesTables.getSelectionModel().getSelectedItem().getFilename());
                if (Files.isDirectory(src)) {
                    Files.walkFileTree(src, new SimpleFileVisitor<Path>() {
                        @Override
                        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                            Path targetPath = dst.resolve(src.relativize(dir));
                            if (!Files.exists(targetPath)) {
                                Files.createDirectory(targetPath);
                            }
                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                            Files.move(file, dst.resolve(src.relativize(file)), StandardCopyOption.REPLACE_EXISTING);
                            return FileVisitResult.CONTINUE;
                        }
                    });
                } else {
                    Files.move(src, dst, StandardCopyOption.REPLACE_EXISTING);
                }
                if (Files.isDirectory(src)) {
                    Files.walkFileTree(src, new SimpleFileVisitor<Path>() {
                        @Override
                        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                            Files.delete(dir);
                            return FileVisitResult.CONTINUE;
                        }
                    });
                }
                updateList(src.getParent());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void btnUploadAction(ActionEvent actionEvent) {
        try {
            Stage secondWindow = new Stage();
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/load.fxml"));
            Parent second = loader.load();
            secondWindow.setTitle("Загрузить файл ...");
            secondWindow.initModality(Modality.APPLICATION_MODAL);
            secondWindow.setScene(new Scene(second, 600, 400));
            secondWindow.showAndWait();

            downloadController = loader.getController();
            downloadController.setParent(this);

            src = Paths.get(downloadController.getDst().toString()).resolve(downloadController.downloadFiles.getSelectionModel().getSelectedItem().getFilename());
            if (src != null) {
                dst = Paths.get(pathField.getText()).resolve(downloadController.downloadFiles.getSelectionModel().getSelectedItem().getFilename());
                if (Files.isDirectory(src)) {
                    Files.walkFileTree(src, new SimpleFileVisitor<Path>() {
                        @Override
                        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                            Path targetPath = dst.resolve(src.relativize(dir));
                            if (!Files.exists(targetPath)) {
                                Files.createDirectory(targetPath);
                            }
                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                            Files.copy(file, dst.resolve(src.relativize(file)), StandardCopyOption.REPLACE_EXISTING);
                            return FileVisitResult.CONTINUE;
                        }

                    });
                } else {
                    Files.copy(src, dst, StandardCopyOption.REPLACE_EXISTING);
                }
                updateList(dst.getParent());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public String getSelectedFileName() {
        if (!filesTables.isFocused()) {
            return null;
        }
        return filesTables.getSelectionModel().getSelectedItem().getFilename();
    }
}

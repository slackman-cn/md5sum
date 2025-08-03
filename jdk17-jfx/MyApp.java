package com.example.md5fx;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.Objects;

public class MyApp extends Application {

    public static void main(String[] args) {
        launch();
    }

    CheckSumTask checkSumTask;

    @Override
    public void start(Stage stage) throws Exception {
        stage.setTitle("JavaMD5 (jfx)");
        stage.getIcons().add(new Image("/file.png"));
        stage.setWidth(550);
        stage.setHeight(300);
        stage.setResizable(false);

        VBox layout = new VBox();
        layout.setPadding(new Insets(10));
        layout.setSpacing(5.0);
        layout.getChildren().add(new Text("Select a file to compute MD5 checksum (or drag and drop a file onto this window)"));

        TextField tFile = new TextField();
        tFile.setPrefWidth(430);
        tFile.setEditable(false);
        Button btnSelect = new Button("Browse..");

        ProgressBar progress = new ProgressBar(0);
        progress.setPrefWidth(430);
        Button btnCancel = new Button("Cancel  ");
        btnCancel.setDisable(true);

        GridPane g1 = new GridPane();
        g1.addRow(0, tFile, btnSelect);
        g1.addRow(1, progress, btnCancel);
        g1.setHgap(5);
        g1.setVgap(10);
        layout.getChildren().add(g1);

        layout.getChildren().add(new HBox());

        Text tInfo = new Text("File Size: ");
        Text tSize = new Text("n/a");
        HBox g2 = new HBox(tInfo, tSize);
        VBox.setMargin(g2, new Insets(15, 0, 0, 0));
        layout.getChildren().add(g2);


        layout.getChildren().add(new Text("Current file MD5 checksum value:"));
        TextField tMD5 = new TextField();
        tMD5.setPromptText("n/a");
        tMD5.setEditable(false);
        tMD5.setPrefWidth(300);
        layout.getChildren().add(new HBox(tMD5));

        layout.getChildren().add(new Text("Original file MD5 checksum value:"));
        TextField tVerify = new TextField();
        tVerify.setPromptText("paste its original md5 value to verify");
        tVerify.setPrefWidth(300);
        Button btnVerify = new Button("Verify");
        HBox g3 = new HBox(tVerify, btnVerify);
        g3.setSpacing(10);
        layout.getChildren().add(g3);

        final FileChooser fileChooser = new FileChooser();
        String userHomeDir = System.getProperty("user.home");
        fileChooser.setInitialDirectory(new File(userHomeDir));

        btnSelect.setOnAction(actionEvent -> {
            File file = fileChooser.showOpenDialog(stage);
            if (file != null && file.exists()) {
                btnCancel.setDisable(false);
                tFile.setText(file.getAbsolutePath());
                tSize.setText(file.length() + " bytes");
                checkSumTask = new CheckSumTask(file);
                progress.progressProperty().unbind();
                progress.progressProperty().bind(checkSumTask.progressProperty());
                new Thread(checkSumTask).start();
                checkSumTask.setOnSucceeded(workerStateEvent -> {
                    tMD5.setText(checkSumTask.getValue());
                    btnCancel.setDisable(true);
                });
            }
        });

        btnCancel.setOnAction(actionEvent -> {
            progress.progressProperty().unbind();
            progress.setProgress(0);
            if (checkSumTask != null && checkSumTask.isRunning()) {
                checkSumTask.cancel();
            }
            tFile.clear();
            tSize.setText("n/a");
            btnCancel.setDisable(true);
        });

        Alert alert = new Alert(Alert.AlertType.NONE);
        alert.setTitle("MD5");
        btnVerify.setOnAction(actionEvent -> {
            boolean matched = Objects.equals(tMD5.getText(), tVerify.getText());
            alert.setAlertType(matched ? Alert.AlertType.INFORMATION : Alert.AlertType.WARNING);
            alert.setHeaderText("Original:\t" + tMD5.getText() + "\nCurrent:\t" + tVerify.getText());
            alert.setContentText(matched ? "Matched" : "Not Matched");
            alert.show();
        });

        stage.setScene(new Scene(layout));
        stage.show();
    }
}

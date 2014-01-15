package pl.aetas.bakspad;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import pl.aetas.bakspad.controllers.MainController;
import pl.aetas.bakspad.data.DataLoader;
import pl.aetas.bakspad.data.DataSaver;

public class App extends Application {

    private MainController mainController;

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("main.fxml"));
        stage.setTitle("Aetas BaksPad");
        stage.setScene(new Scene((Parent)loader.load()));
        mainController = loader.getController();
        final ObjectMapper objectMapper = new ObjectMapper();
        new DataSaver(objectMapper).createDataFolderIfDoesNotExist();
        mainController.setNotesTable(new DataLoader(objectMapper).load());
        mainController.setStage(stage);
        stage.show();
    }

    @Override
    public void stop() throws Exception {
        mainController.saveFocusedNoteEntryNow();
        super.stop();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

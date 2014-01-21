package pl.aetas.bakspad;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import pl.aetas.bakspad.presentation.controller.MainController;

public class App extends Application {

    private static final String MAIN_WINDOW_TITLE = "Aetas BaksPad";

    private MainController mainController;

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("main.fxml"));
        stage.setTitle(MAIN_WINDOW_TITLE);
        stage.setScene(new Scene((Parent)loader.load()));
        mainController = loader.getController();
        mainController.setStage(stage);
        stage.show();
    }

    @Override
    public void stop() throws Exception {
        mainController.handleCloseApplicationAction(new ActionEvent());
        super.stop();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

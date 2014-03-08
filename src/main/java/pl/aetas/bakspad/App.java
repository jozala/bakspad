package pl.aetas.bakspad;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import jfxtras.labs.dialogs.MonologFX;
import jfxtras.labs.dialogs.MonologFXBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pl.aetas.bakspad.presentation.controller.MainController;

public class App extends Application {

    private static final String MAIN_WINDOW_TITLE = "Aetas BaksPad";

    private MainController mainController;

    private static final Logger LOGGER = LogManager.getLogger();

    @Override
    public void start(Stage stage) throws Exception {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("main.fxml"));
            stage.setTitle(MAIN_WINDOW_TITLE);
            stage.setScene(new Scene((Parent)loader.load()));
            mainController = loader.getController();
            mainController.setStage(stage);
            stage.show();
        } catch (Exception e) {
            LOGGER.fatal("Something went really wrong. Closing application.", e);
            stop();
        }
    }

    @Override
    public void stop() throws Exception {
        MonologFX errorDialog = MonologFXBuilder.create().type(MonologFX.Type.ERROR).titleText("Poważny błąd programu")
                .message("Skontaktuj się z twórcą programu. Program zostanie zamknięty.").modal(true).build();

        errorDialog.showDialog();
        mainController.handleCloseApplicationAction(new ActionEvent());
        super.stop();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

package pl.aetas.bakspad.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class NoteEntryDialogController {

    private Stage parentStage;

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private TextField nameTextField;

    @FXML
    private TextField descriptionTextField;

    private boolean finishedWithSave;

    @FXML
    void initialize() {
        assert nameTextField != null : "fx:id=\"nameTextField\" was not injected: check your FXML file 'NoteEntryDialog.fxml'.";
        assert descriptionTextField != null : "fx:id=\"descriptionTextField\" was not injected: check your FXML file 'NoteEntryDialog.fxml'.";

    }

    @FXML
    private void handleCancelButtonAction(ActionEvent event) {
        finishedWithSave = false;
        parentStage.close();
    }

    @FXML
    private void handleSaveButtonAction(ActionEvent event) {
        finishedWithSave = true;
        parentStage.close();
    }


    public void setStage(Stage temp){
        parentStage = temp;
    }


    public boolean isFinishedWithSave() {
        return finishedWithSave;
    }

    public String getNoteEntryName() {
        if (!finishedWithSave) {
            throw new IllegalStateException("Add new entry dialog has been closed with cancel. Values should not be read.");
        }
        return nameTextField.getText();
    }

    public String getNoteEntryDescription() {
        if (!finishedWithSave) {
            throw new IllegalStateException("Add new entry dialog has been closed with cancel. Values should not be read.");
        }
        return descriptionTextField.getText();
    }
}



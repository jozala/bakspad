package pl.aetas.bakspad.presentation.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class AddNoteDialogController {

    private Stage parentStage;

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
    public void handleCancelButtonAction(ActionEvent event) {
        finishedWithSave = false;
        parentStage.close();
    }

    @FXML
    public void handleSaveButtonAction(ActionEvent event) {
        finishedWithSave = true;
        parentStage.close();
    }


    public void setStage(Stage temp){
        parentStage = temp;
    }


    public boolean isFinishedWithSave() {
        return finishedWithSave;
    }

    public String getNoteName() {
        if (!finishedWithSave) {
            throw new IllegalStateException("Add new entry dialog has been closed with cancel. Values should not be read.");
        }
        return nameTextField.getText();
    }

    public String getNoteDescription() {
        if (!finishedWithSave) {
            throw new IllegalStateException("Add new entry dialog has been closed with cancel. Values should not be read.");
        }
        return descriptionTextField.getText();
    }
}



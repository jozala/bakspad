package pl.aetas.bakspad.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import jfxtras.labs.dialogs.MonologFX;
import jfxtras.labs.dialogs.MonologFXBuilder;
import jfxtras.labs.dialogs.MonologFXButton;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pl.aetas.bakspad.data.JsonDataManipulator;
import pl.aetas.bakspad.data.NoteFile;
import pl.aetas.bakspad.exception.DeleteFailedException;
import pl.aetas.bakspad.exception.SaveFailedException;
import pl.aetas.bakspad.model.Note;
import pl.aetas.bakspad.presentation.GenericCellFactory;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final Path DATA_PATH = Paths.get("data");

    @FXML
    private TextArea contentTextArea;

    @FXML
    private TableView<NoteFile> notesTable;

    @FXML
    TableColumn<NoteFile, String> nameColumn;

    @FXML
    TableColumn<NoteFile, String> descriptionColumn;

    private Stage stage;

    private ObservableList<NoteFile> notesList;

    private JsonDataManipulator jsonDataManipulator;


    public MainController() {
        notesList = FXCollections.observableArrayList();
        jsonDataManipulator = new JsonDataManipulator(new ObjectMapper(), DATA_PATH);
    }

    @FXML
    public void initialize(URL url, ResourceBundle resourceBundle) {
        assert contentTextArea != null : "fx:id=\"contentTextArea\" was not injected: check your FXML file 'main.fxml'.";
        assert notesTable != null : "fx:id=\"notesTable\" was not injected: check your FXML file 'main.fxml'.";
        assert nameColumn != null;
        assert descriptionColumn != null;

        contentTextArea.setVisible(false);
        loadNotes();
        setCellsOnNotesTable();
        notesTable.setItems(notesList);
        contentTextArea.textProperty().addListener(
            new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observableValue, String oldContent, String newContent) {
                    setContentOnSelectedNote(newContent);
                }
            }
        );
        notesTable.getSelectionModel().selectedItemProperty().addListener(
            new ChangeListener<NoteFile>() {
                @Override
                public void changed(ObservableValue observableValue, NoteFile oldValue, NoteFile newValue) {
                    onNotesTableSelectionChangeAction(newValue);
                }
            }
        );


    }

    private void onNotesTableSelectionChangeAction(NoteFile newValue) {
        contentTextArea.setVisible(true);
        if (newValue != null) {
            contentTextArea.setText(newValue.getContent());
        }
    }

    private void setContentOnSelectedNote(String newContent) {
        notesTable.getSelectionModel().getSelectedItem().setContent(newContent);
    }

    private void setCellsOnNotesTable() {
        final ContextMenu contextMenu = createContextMenu();
        final GenericCellFactory cellFactory = new GenericCellFactory(contextMenu);
        nameColumn.setCellFactory(cellFactory);
        descriptionColumn.setCellFactory(cellFactory);
    }

    private ContextMenu createContextMenu() {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem addNoteMenuItem = new MenuItem("Dodaj");
        MenuItem removeNoteMenuItem = new MenuItem("Usuń");
        removeNoteMenuItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                final int selectedIndex = notesTable.getSelectionModel().getSelectedIndex();
                removeNoteEntry(notesTable.getItems().get(selectedIndex));
            }
        });
        addNoteMenuItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                try {
                    handleAddNewEntryAction(actionEvent);
                } catch (IOException e) {
                    LOGGER.fatal("Could not load dialog for new note", e);
                    throw new IllegalStateException("Could not load dialog for new note", e);
                }
            }
        });
        contextMenu.getItems().addAll(removeNoteMenuItem, addNoteMenuItem);
        return contextMenu;
    }

    private void loadNotes() {
        try {
            notesList.clear();
            notesList.addAll(jsonDataManipulator.load());
        } catch (IOException e) {
            LOGGER.error("Failed to load notes", e);
            showErrorDialog("Wystąpił błąd podczas wczytywania notatek. Zgłoś problem.");
        }
    }

    public void removeNoteEntry(final NoteFile noteFile) {
        notesList.remove(noteFile);
        try {
            noteFile.delete();
        } catch (DeleteFailedException e) {
            LOGGER.error("Deleting of note with name {} failed", noteFile.getName());
            showErrorDialog("Wystąpił błąd podczas usuwania notatki. Zgłoś problem.");

        }
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    public void handleAddNewEntryAction(ActionEvent actionEvent) throws IOException {
        final FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("NoteEntryDialog.fxml"));
        fxmlLoader.load();
        final Parent noteEntryDialogRoot = fxmlLoader.getRoot();
        Scene noteEntryDialogScene = new Scene(noteEntryDialogRoot);
        Stage noteEntryDialogStage = new Stage(StageStyle.UTILITY);
        noteEntryDialogStage.setScene(noteEntryDialogScene);
        noteEntryDialogStage.initModality(Modality.APPLICATION_MODAL);
        noteEntryDialogStage.setResizable(false);
        noteEntryDialogStage.initOwner(stage);
        final AddNoteDialogController addNoteDialogController = fxmlLoader.getController();
        addNoteDialogController.setStage(noteEntryDialogStage);
        noteEntryDialogStage.showAndWait();
        if (addNoteDialogController.isFinishedWithSave()) {
            Note newNote = new Note(addNoteDialogController.getNoteName(), addNoteDialogController.getNoteDescription(), "");
            final NoteFile newNoteFile = new NoteFile(addNoteDialogController.getNoteName(), newNote, new JsonDataManipulator(new ObjectMapper(), DATA_PATH));
            notesList.add(newNoteFile);
        }
    }

    @FXML
    public void handleCloseApplicationAction(ActionEvent actionEvent) {
        checkForNotSavedNotes();
        System.exit(0);
    }

    private void checkForNotSavedNotes() {
        if (isAnyNoteDirty()) {
            String message = "Niektóre notatki nie zostały zapisane:";
            for (NoteFile noteFile :notesList) {
                if (noteFile.isDirty()) {
                    message += "\n" + noteFile.getName();
                }
            }
            message += " \nCzy chcesz je zapisać przed zamknięciem programu?";
            final MonologFX dialog = MonologFXBuilder.create().modal(true).type(MonologFX.Type.QUESTION).message(message).build();
            final MonologFXButton.Type returnedValue = dialog.showDialog();
            if (returnedValue == MonologFXButton.Type.YES) {
                LOGGER.info("Saving files when closing application");
                handleSaveAllNotesAction(null);
            }
        }
    }

    private boolean isAnyNoteDirty() {
        for (NoteFile noteFile :notesList) {
            if (noteFile.isDirty()) {
                return true;
            }
        }
        return false;
    }

    @FXML
    public void handleNoteNameEditAction(TableColumn.CellEditEvent<NoteFile, String> stCellEditEvent) {
        final NoteFile noteBeforeEdit = stCellEditEvent.getRowValue();
        noteBeforeEdit.setName(stCellEditEvent.getNewValue());
    }

    public void handleNoteDescriptionEditAction(TableColumn.CellEditEvent<NoteFile, String> stCellEditEvent) {
        final NoteFile noteBeforeEdit = stCellEditEvent.getRowValue();
        noteBeforeEdit.setDescription(stCellEditEvent.getNewValue());
    }

    public void handleSaveSelectedNoteAction(ActionEvent actionEvent) {
        try {
            notesTable.getSelectionModel().getSelectedItem().save();
        } catch (SaveFailedException e) {
            LOGGER.error("Saving selected note action failed", e);
            showErrorDialog("Wystąpił błąd podczas zapisywania zmian. Zmiany nie zostaną zapisane. Zgłoś problem.");
        }
    }

    public void handleSaveAllNotesAction(ActionEvent actionEvent) {
        try {
            for(NoteFile noteFile : notesList) {
                noteFile.save();
            }
        } catch (SaveFailedException e) {
            LOGGER.error("Saving all notes action failed", e);
            showErrorDialog("Wystąpił błąd podczas zapisywania zmian. Zmiany nie zostaną zapisane. Zgłoś problem.");
        }
    }

    private void showErrorDialog(String message) {
        final MonologFX dialog = MonologFXBuilder.create().modal(true).type(MonologFX.Type.ERROR).message(message).build();
        dialog.showDialog();
    }

    public void handleOpenAboutDialogAction(ActionEvent actionEvent) throws IOException {
        final FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("about.fxml"));
        fxmlLoader.load();
        final Parent aboutDialogRoot = fxmlLoader.getRoot();
        Scene aboutDialogScene = new Scene(aboutDialogRoot);
        Stage aboutDialogStage = new Stage(StageStyle.DECORATED);
        aboutDialogStage.setScene(aboutDialogScene);
        aboutDialogStage.initModality(Modality.APPLICATION_MODAL);
        aboutDialogStage.setResizable(false);
        aboutDialogStage.initOwner(stage);
        aboutDialogStage.show();
    }
}

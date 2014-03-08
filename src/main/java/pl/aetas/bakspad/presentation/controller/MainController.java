package pl.aetas.bakspad.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.web.HTMLEditor;
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
import pl.aetas.bakspad.presentation.LocaleAwareStringComparator;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Locale;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final Path DATA_PATH = Paths.get("data");

    @FXML
    private HTMLEditor contentTextArea;

    @FXML
    private TableView<NoteFile> notesTable;

    @FXML
    TableColumn<NoteFile, String> nameColumn;

    @FXML
    TableColumn<NoteFile, String> descriptionColumn;

    @FXML
    private TextField filterTextField;

    private Stage stage;

    private ObservableList<NoteFile> notesList;
    private ObservableList<NoteFile> filteredNotesList;

    private JsonDataManipulator jsonDataManipulator;


    public MainController() {
        notesList = FXCollections.observableArrayList();
        filteredNotesList = FXCollections.observableArrayList();
        jsonDataManipulator = new JsonDataManipulator(new ObjectMapper(), DATA_PATH);
    }

    @FXML
    public void initialize(URL url, ResourceBundle resourceBundle) {
        assert contentTextArea != null : "fx:id=\"contentTextArea\" was not injected: check your FXML file 'main.fxml'.";
        assert notesTable != null : "fx:id=\"notesTable\" was not injected: check your FXML file 'main.fxml'.";
        assert nameColumn != null;
        assert descriptionColumn != null;

        LocaleAwareStringComparator localeAwareStringComparator = new LocaleAwareStringComparator(Locale.getDefault());
        nameColumn.setComparator(localeAwareStringComparator);
        descriptionColumn.setComparator(localeAwareStringComparator);

        contentTextArea.setVisible(false);
        loadNotes();
        keepFilteredNotesListInSync();
        setCellsOnNotesTable();
        notesTable.setItems(filteredNotesList);
        listenForFilterTextFieldChanges();
        contentTextArea.addEventHandler(KeyEvent.KEY_RELEASED, new EventHandler<Event>() {
            @Override
            public void handle(Event event) {
                setContentOnSelectedNote(contentTextArea.getHtmlText());
            }
        });
        notesTable.getSelectionModel().selectedItemProperty().addListener(
                new ChangeListener<NoteFile>() {
                    @Override
                    public void changed(ObservableValue observableValue, NoteFile oldValue, NoteFile newValue) {
                        onNotesTableSelectionChangeAction(newValue);
                    }
                }
        );


    }

    private void listenForFilterTextFieldChanges() {
        filterTextField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable,
                                String oldValue, String newValue) {

                updateFilteredData();
            }
        });

    }

    private void keepFilteredNotesListInSync() {
        filteredNotesList.addAll(notesList);
        notesList.addListener(new ListChangeListener<NoteFile>() {
            @Override
            public void onChanged(ListChangeListener.Change<? extends NoteFile> change) {
                updateFilteredData();
            }
        });

    }

    /**
     * Updates the filteredData to contain all data from the masterData that
     * matches the current filter.
     */
    private void updateFilteredData() {
        filteredNotesList.clear();

        for (NoteFile p : notesList) {
            if (matchesFilter(p)) {
                filteredNotesList.add(p);
            }
        }

        // Must re-sort table after items changed
        reapplyTableSortOrder();
    }

    private boolean matchesFilter(NoteFile noteFile) {
        String filterString = filterTextField.getText();
        if (filterString == null || filterString.isEmpty()) {
            // No filter --> Add all.
            return true;
        }

        String lowerCaseFilterString = filterString.toLowerCase();

        if (noteFile.getName().toLowerCase().indexOf(lowerCaseFilterString) != -1) {
            return true;
        } else if (noteFile.getDescription().toLowerCase().indexOf(lowerCaseFilterString) != -1) {
            return true;
        }

        return false; // Does not match
    }

    private void reapplyTableSortOrder() {
        ArrayList<TableColumn<NoteFile, ?>> sortOrder = new ArrayList<>(notesTable.getSortOrder());
        notesTable.getSortOrder().clear();
        notesTable.getSortOrder().addAll(sortOrder);
    }


    private void onNotesTableSelectionChangeAction(NoteFile newValue) {
        contentTextArea.setVisible(true);
        if (newValue != null) {
            contentTextArea.setHtmlText(newValue.getContent());
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
                    handleAddNewNoteAction(actionEvent);
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
        final String message = "Czy na pewno chcesz usunąć tę notatkę (" + noteFile.getName() + ")?\nTej operacji nie można cofnąć.";
        final MonologFX dialog = MonologFXBuilder.create().modal(true).type(MonologFX.Type.QUESTION).message(message).build();
        final MonologFXButton.Type returnedValue = dialog.showDialog();
        if (returnedValue == MonologFXButton.Type.YES) {
            notesList.remove(noteFile);
            try {
                noteFile.delete();
                LOGGER.info("Note deleted");
            } catch (DeleteFailedException e) {
                LOGGER.error("Deleting of note with name {} failed", noteFile.getName());
                showErrorDialog("Wystąpił błąd podczas usuwania notatki. Zgłoś problem.");
            }
        }
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    public void handleAddNewNoteAction(ActionEvent actionEvent) throws IOException {
        final FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("NoteEntryDialog.fxml"));
        if (!checkAllNotesAreSaved("Niezapisane zmiany muszą zostać zapisane przed dodaniem nowej notatki.")) {
            return;
        }
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
            String filename = jsonDataManipulator.createProperFilename(addNoteDialogController.getNoteName());
            final NoteFile newNoteFile = NoteFile.newlyCreatedNoteFile(filename, newNote, jsonDataManipulator);
            notesList.add(newNoteFile);
            notesTable.getSelectionModel().select(newNoteFile);
            handleSaveSelectedNoteAction(null);
        }
    }

    @FXML
    public void handleCloseApplicationAction(ActionEvent actionEvent) {
        checkAllNotesAreSaved("Zamykanie programu. Niezapisane zmiany zostaną utracone!");
        System.exit(0);
    }

    private boolean checkAllNotesAreSaved(String messageStart) {
        if (isAnyNoteDirty()) {
            String message = messageStart;
            message += "\nTe notatki nie zostały zapisane:";
            for (NoteFile noteFile : notesList) {
                if (noteFile.isDirty()) {
                    message += "\n" + noteFile.getName();
                }
            }
            message += " \nCzy chcesz je teraz zapisać?";
            final MonologFX dialog = MonologFXBuilder.create().modal(true).type(MonologFX.Type.QUESTION).message(message).build();
            final MonologFXButton.Type returnedValue = dialog.showDialog();
            if (returnedValue == MonologFXButton.Type.YES) {
                LOGGER.info("Saving dirty notes files");
                handleSaveAllNotesAction(null);
                return true;
            }
            return false;
        }
        return true;
    }

    private boolean isAnyNoteDirty() {
        for (NoteFile noteFile : notesList) {
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
            for (NoteFile noteFile : notesList) {
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

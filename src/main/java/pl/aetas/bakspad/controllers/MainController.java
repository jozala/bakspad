package pl.aetas.bakspad.controllers;

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
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import pl.aetas.bakspad.data.DataSaver;
import pl.aetas.bakspad.model.NoteEntry;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Set;

public class MainController implements Initializable {

    @FXML // fx:id="contentTextArea"
    private TextArea contentTextArea;

    @FXML // fx:id="notesTable"
    private TableView<NoteEntry> notesTable;

    private ObservableList<NoteEntry> notesList;
    private Stage stage;

    public MainController() {
        notesList = FXCollections.observableArrayList();
    }

    @FXML
    public void initialize(URL url, ResourceBundle resourceBundle) {
        assert contentTextArea != null : "fx:id=\"contentTextArea\" was not injected: check your FXML file 'main.fxml'.";
        assert notesTable != null : "fx:id=\"notesTable\" was not injected: check your FXML file 'main.fxml'.";
        contentTextArea.setVisible(false);
        ContextMenu contextMenu = new ContextMenu();
        MenuItem addNoteMenuItem = new MenuItem("Dodaj");
        MenuItem removeNoteMenuItem = new MenuItem("Usuń");
        removeNoteMenuItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                final int selectedIndex = notesTable.getSelectionModel().getSelectedIndex();
                try {
                    removeNoteEntry(notesTable.getItems().get(selectedIndex));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        addNoteMenuItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                handleAddNewEntryAction(actionEvent);
            }
        });
        contextMenu.getItems().addAll(removeNoteMenuItem, addNoteMenuItem);
        final GenericCellFactory cellFactory = new GenericCellFactory(contextMenu);
        final TableColumn<NoteEntry, String> nameColumn = (TableColumn<NoteEntry, String>) notesTable.getColumns().get(0);
        final TableColumn<NoteEntry, String> descriptionColumn = (TableColumn<NoteEntry, String>) notesTable.getColumns().get(1);
        nameColumn.setCellValueFactory(new PropertyValueFactory<NoteEntry, String>("name"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<NoteEntry, String>("description"));
        nameColumn.setCellFactory(cellFactory);
        descriptionColumn.setCellFactory(cellFactory);
        notesTable.setItems(notesList);
        contentTextArea.textProperty().addListener(
                new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue<? extends String> observableValue, String oldContent, String newContent) {
                        notesTable.getSelectionModel().getSelectedItem().setContent(newContent);
                    }
                }
        );
        notesTable.getSelectionModel().selectedItemProperty().addListener(
                new ChangeListener<NoteEntry>() {
                    @Override
                    public void changed(ObservableValue observableValue, NoteEntry oldValue, NoteEntry newValue) {
                        contentTextArea.setVisible(true);
                        if (oldValue != null) {
                            try {
                                new DataSaver(new ObjectMapper()).save(oldValue);
                            } catch (IOException e) {
                                e.printStackTrace();
                                throw new RuntimeException(e);
                            }
                        }
                        if (newValue != null) {
                            contentTextArea.setText(newValue.getContent());
                        }

                    }
                }
        );

    }

    public void setNotesTable(Set<NoteEntry> noteEntries) throws IOException {
        notesList.clear();
        notesList.addAll(noteEntries);
    }

    public void addNoteEntry(final NoteEntry noteEntry) {
        if (notesList.contains(noteEntry)) {
            throw new IllegalArgumentException("Cannot add new note with the same name");
        }
        try {
            new DataSaver(new ObjectMapper()).save(noteEntry);
        } catch (IOException e) {
            e.printStackTrace();
        }
        notesList.add(noteEntry);
    }

    public void removeNoteEntry(final NoteEntry noteEntry) throws IOException {
        notesList.remove(noteEntry);
        new DataSaver(new ObjectMapper()).remove(noteEntry);
    }

    public void saveFocusedNoteEntryNow() throws IOException {
        if(!notesTable.getSelectionModel().isEmpty()) {
            new DataSaver(new ObjectMapper()).save(notesTable.getSelectionModel().getSelectedItem());
        }
    }


    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    public void handleAddNewEntryAction(ActionEvent actionEvent) {
        final FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("NoteEntryDialog.fxml"));
        try {
            fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        final Parent noteEntryDialogRoot = fxmlLoader.getRoot();
        Scene noteEntryDialogScene = new Scene(noteEntryDialogRoot);
        Stage noteEntryDialogStage = new Stage(StageStyle.UTILITY);
        noteEntryDialogStage.setScene(noteEntryDialogScene);
        noteEntryDialogStage.initModality(Modality.APPLICATION_MODAL);
        noteEntryDialogStage.setResizable(false);
        noteEntryDialogStage.initOwner(stage);
        final NoteEntryDialogController noteEntryDialogController = fxmlLoader.getController();
        noteEntryDialogController.setStage(noteEntryDialogStage);
        noteEntryDialogStage.showAndWait();
        if (noteEntryDialogController.isFinishedWithSave()) {
            addNoteEntry(new NoteEntry(noteEntryDialogController.getNoteEntryName(), noteEntryDialogController.getNoteEntryDescription(), ""));
        }
    }

    @FXML
    public void handleCloseApplicationAction(ActionEvent actionEvent) throws IOException {
        saveFocusedNoteEntryNow();
        System.exit(0);
    }
}

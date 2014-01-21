package pl.aetas.bakspad.presentation;

import javafx.fxml.FXML;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import javafx.util.converter.DefaultStringConverter;
import pl.aetas.bakspad.data.NoteFile;

public class GenericCellFactory implements Callback<TableColumn<NoteFile, String>,TableCell<NoteFile,String>> {

    @FXML
    private final ContextMenu contextMenu;

    public GenericCellFactory(ContextMenu contextMenu) {
        this.contextMenu = contextMenu;
    }

    @Override
    public TableCell<NoteFile, String> call(final TableColumn<NoteFile, String> p) {
        final NoteFileTableCell noteFileTableCell = new NoteFileTableCell(contextMenu);
        noteFileTableCell.setConverter(new DefaultStringConverter());
        return noteFileTableCell;
    }


}
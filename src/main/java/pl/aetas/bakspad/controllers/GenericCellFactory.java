package pl.aetas.bakspad.controllers;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import javafx.util.converter.DefaultStringConverter;
import pl.aetas.bakspad.model.NoteEntry;
import pl.aetas.bakspad.presentation.NoteEntryTableCell;

public class GenericCellFactory implements Callback<TableColumn<NoteEntry, String>,TableCell<NoteEntry,String>> {

    private final ContextMenu contextMenu;

    public GenericCellFactory(ContextMenu contextMenu) {
        this.contextMenu = contextMenu;
    }

    @Override
    public TableCell<NoteEntry, String> call(final TableColumn<NoteEntry, String> p) {
        final NoteEntryTableCell noteEntryTableCell = new NoteEntryTableCell(contextMenu);
        noteEntryTableCell.setConverter(new DefaultStringConverter());
        return noteEntryTableCell;
    }


}
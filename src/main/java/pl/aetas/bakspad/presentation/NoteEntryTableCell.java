package pl.aetas.bakspad.presentation;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.TableCell;
import pl.aetas.bakspad.model.NoteEntry;

public class NoteEntryTableCell extends TableCell<NoteEntry, String> {


    private final ContextMenu contextMenu;

    public NoteEntryTableCell(final ContextMenu contextMenu) {
        this.contextMenu = contextMenu;
    }

    @Override
    public void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        if (!empty) {
            setText(getString());
            setContextMenu(contextMenu);
        }
    }

    private String getString() {
        return getItem() == null ? "" : getItem();
    }




}

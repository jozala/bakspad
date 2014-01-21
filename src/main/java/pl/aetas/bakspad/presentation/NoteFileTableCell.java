package pl.aetas.bakspad.presentation;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pl.aetas.bakspad.data.NoteFile;
import pl.aetas.bakspad.data.NoteFileIsDirtyListener;

import java.util.Objects;

public class NoteFileTableCell extends TextFieldTableCell<NoteFile, String> implements NoteFileIsDirtyListener {

    private static final Logger LOGGER = LogManager.getLogger();
    private final ContextMenu contextMenu;

    public NoteFileTableCell(final ContextMenu contextMenu) {
        this.contextMenu = Objects.requireNonNull(contextMenu);
    }


    @Override
    public void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        unregisterFromAllNotes();
        if (!empty) {
            setText(getString());
            setContextMenu(contextMenu);
            final NoteFile noteFile = (NoteFile) getTableRow().getItem();
            setNoteFileCellDirty(noteFile.isDirty());
            noteFile.registerIsDirtyListener(this);
        }
    }

    private void unregisterFromAllNotes() {
        for (NoteFile noteFile : getTableView().getItems()) {
            noteFile.unregisterIsDirtyListener(this);
        }
    }

    private void setNoteFileCellDirty(boolean isDirty) {
        if (isDirty) {
            setFont(Font.font("System", FontWeight.BOLD, 13));
        } else {
            setFont(Font.font("System", FontWeight.NORMAL, 13));
        }
    }

    private String getString() {
        return getItem() == null ? "" : getItem();
    }

    @Override
    public void change(boolean isDirty) {
        LOGGER.debug("Is dirty in {} changed to {}", this.getItem(), isDirty);
        setNoteFileCellDirty(isDirty);
    }
}

package pl.aetas.bakspad.data;

import pl.aetas.bakspad.exception.DeleteFailedException;
import pl.aetas.bakspad.exception.SaveFailedException;
import pl.aetas.bakspad.model.Note;

import java.io.IOException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class NoteFile {

    private final String filename;
    private final Note note;
    private final JsonDataManipulator jsonDataManipulator;
    private boolean dirty;
    private final Set<NoteFileIsDirtyListener> isDirtyListeners;

    private NoteFile(String filename, Note note, JsonDataManipulator jsonDataManipulator, boolean dirty) {
        this.filename = filename;
        this.note = note;
        this.jsonDataManipulator = jsonDataManipulator;
        this.dirty = dirty;
        isDirtyListeners = new HashSet<>();
    }

    public static NoteFile loadedNoteFile(String filename, Note note, JsonDataManipulator jsonDataManipulator) {
        return new NoteFile(filename, note, jsonDataManipulator, false);
    }

    public static NoteFile newlyCreatedNoteFile(String filename, Note note, JsonDataManipulator jsonDataManipulator) {
        return new NoteFile(filename, note, jsonDataManipulator, true);
    }

    public void setName(String name) {
        Objects.requireNonNull(name);
        if (!name.equals(getName())) {
            setDirty(true);
        }
        note.setName(name);
    }

    public void setDescription(String description) {
        Objects.requireNonNull(description);
        if (!description.equals(getDescription())) {
            setDirty(true);
        }
        note.setDescription(description);
    }

    public void setContent(String content) {
        Objects.requireNonNull(content);
        if (!content.equals(getContent())) {
            setDirty(true);
        }
        note.setContent(content);
    }

    public String getName() {
        return note.getName();
    }

    public String getDescription() {
        return note.getDescription();
    }

    public String getContent() {
        return note.getContent();
    }

    public boolean isDirty() {
        return dirty;
    }

    private void setDirty(boolean isDirty) {
        dirty = isDirty;
        for (NoteFileIsDirtyListener listener : isDirtyListeners) {
            listener.change(isDirty);
        }
    }

    public void save() throws SaveFailedException {
        if (!dirty) {
            return;
        }
        try {
            jsonDataManipulator.save(this);
        } catch (IOException e) {
            throw new SaveFailedException("Saving note entry to file " + filename + " has failed", e);
        }
        setDirty(false);
    }

    public void delete() throws DeleteFailedException {
        try {
            jsonDataManipulator.delete(this);
        } catch (IOException e) {
            throw new DeleteFailedException("Delete of note file with name " + filename + "has failed", e);
        }
    }

    public Note getNote() {
        return note;
    }

    public String getFilename() {
        return filename;
    }

    public void registerIsDirtyListener(NoteFileIsDirtyListener listener) {
        Objects.requireNonNull(listener);
        isDirtyListeners.add(listener);
    }

    public void unregisterIsDirtyListener(NoteFileIsDirtyListener listener) {
        Objects.requireNonNull(listener);
        isDirtyListeners.remove(listener);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NoteFile)) return false;

        NoteFile noteFile = (NoteFile) o;

        if (!filename.equals(noteFile.filename)) return false;
        if (!note.equals(noteFile.note)) return false;

        return true;
    }


    @Override
    public int hashCode() {
        return filename.hashCode();
    }


}

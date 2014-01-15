package pl.aetas.bakspad.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import pl.aetas.bakspad.model.NoteEntry;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DataSaver {

    public static final String DATA_DIRECTORY_NAME = "data";

    private ObjectMapper objectMapper;

    public DataSaver(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void save(final NoteEntry noteEntry) throws IOException {
        if (noteEntry == null) throw new NullPointerException("noteEntry cannot be null");
        createDataFolderIfDoesNotExist();
        saveToFile(noteEntry);
    }

    public void remove(final NoteEntry noteEntry) throws IOException {
        if (noteEntry == null) throw new NullPointerException("noteEntry cannot be null");
        final Path pathToFile = Paths.get(DATA_DIRECTORY_NAME, noteEntry.getName());
        Files.delete(pathToFile);
    }

    private void saveToFile(NoteEntry noteEntry) throws IOException {
        final Path pathToFile = Paths.get(DATA_DIRECTORY_NAME, noteEntry.getName());
        if (Files.notExists(pathToFile)) {
            Files.createFile(pathToFile);
        }
        objectMapper.writeValue(pathToFile.toFile(), noteEntry);
    }

    public void createDataFolderIfDoesNotExist() throws IOException {
        final Path dataDirectory = Paths.get(DATA_DIRECTORY_NAME);
        if (Files.notExists(dataDirectory)) {
            Files.createDirectory(dataDirectory);
        }
    }
}

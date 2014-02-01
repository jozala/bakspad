package pl.aetas.bakspad.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pl.aetas.bakspad.model.Note;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static java.util.Objects.requireNonNull;

/**
 * Class responsible for manipulating (read and write) Json files of Note data
 */
public class JsonDataManipulator {

    private final static Logger LOGGER = LogManager.getLogger();

    private ObjectMapper objectMapper;
    private final Path dataDirectoryPath;

    public JsonDataManipulator(ObjectMapper objectMapper, Path dataDirectoryPath) {
        this.objectMapper = objectMapper;
        this.dataDirectoryPath = dataDirectoryPath;
    }

    public void save(final NoteFile noteFile) throws IOException {
        requireNonNull(noteFile, "noteFile cannot be null");
        createDataFolderIfDoesNotExist();
        saveToFile(noteFile);
    }

    private void createDataFolderIfDoesNotExist() throws IOException {
        if (Files.notExists(dataDirectoryPath)) {
            Files.createDirectory(dataDirectoryPath);
        }
    }

    private void saveToFile(NoteFile noteFile) throws IOException {
        assert noteFile != null;
        final Path pathToFile = dataDirectoryPath.resolve(noteFile.getFilename());
        objectMapper.writeValue(pathToFile.toFile(), noteFile.getNote());
    }

    public void delete(final NoteFile noteFile) throws IOException {
        requireNonNull(noteFile, "noteFile cannot be null");
        final Path pathToFile = dataDirectoryPath.resolve(noteFile.getFilename());
        Files.delete(pathToFile);
    }

    public Set<NoteFile> load() throws IOException {
        return readAllFilesFromDataDirectory();
    }

    private Set<NoteFile> readAllFilesFromDataDirectory() throws IOException {
        if (Files.notExists(dataDirectoryPath)) {
            LOGGER.warn("Data directory does not exists: {}. Maybe this is just first run of this Application.",
                    dataDirectoryPath.toAbsolutePath().toString());
            return Collections.emptySet();
        }
        final Set<NoteFile> noteFiles = new HashSet<>();
        Files.walkFileTree(dataDirectoryPath,
                new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        final Note note = objectMapper.readValue(file.toFile(), Note.class);
                        final NoteFile noteFile = NoteFile.loadedNoteFile(file.getFileName().toString(), note, JsonDataManipulator.this);
                        noteFiles.add(noteFile);
                        return FileVisitResult.CONTINUE;
                    }
                });
        return noteFiles;
    }

    public String createProperFilename(String suggestedFilename) {
        int number = 1;
        if (Files.notExists(dataDirectoryPath.resolve(suggestedFilename))) {
            return suggestedFilename;
        }
        while(Files.exists(dataDirectoryPath.resolve(suggestedFilename + "-" + number))) {
            number++;
        }
        final String finalFilename = suggestedFilename + "-" + number;
        LOGGER.info("Suggested filename {} could not have been used and has been changed to {}", suggestedFilename, finalFilename);
        return finalFilename;
    }
}

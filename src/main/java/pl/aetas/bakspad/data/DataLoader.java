package pl.aetas.bakspad.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import pl.aetas.bakspad.model.NoteEntry;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class DataLoader {

    private ObjectMapper objectMapper;

    public DataLoader(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Set<NoteEntry> load() throws IOException {
        return readAllFilesFromDataDirectory();
    }

    private Set<NoteEntry> readAllFilesFromDataDirectory() throws IOException {
        final Set<NoteEntry> noteEntries = new HashSet<>();
        Files.walkFileTree(Paths.get(DataSaver.DATA_DIRECTORY_NAME), Collections.<FileVisitOption>emptySet(), 1,
                new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        final NoteEntry noteEntry = objectMapper.readValue(file.toFile(), NoteEntry.class);
                        noteEntries.add(noteEntry);
                        return FileVisitResult.CONTINUE;
                    }
                });
        return noteEntries;
    }
}

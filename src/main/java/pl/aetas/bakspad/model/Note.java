package pl.aetas.bakspad.model;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class Note {
    private String name;

    private String description;

    private String content;

    @JsonCreator
    public Note(@JsonProperty("name") final String name, @JsonProperty("description") final String description,
                @JsonProperty("content") final String content) {
        this.name = Objects.requireNonNull(name);
        this.description = Objects.requireNonNull(description);
        this.content = Objects.requireNonNull(content);
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getContent() {
        return content;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Note)) return false;

        Note note = (Note) o;

        if (!content.equals(note.content)) return false;
        if (!description.equals(note.description)) return false;
        if (!name.equals(note.name)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}

package pl.aetas.bakspad.model;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class NoteEntry {
    private String name;

    private String description;

    private String content;

    @JsonCreator
    public NoteEntry(@JsonProperty("name") final String name, @JsonProperty("description") final String description,
                     @JsonProperty("content") final String content) {
        this.name = name;
        this.description = description;
        this.content = content;
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
        if (!(o instanceof NoteEntry)) return false;

        NoteEntry noteEntry = (NoteEntry) o;

        if (!name.equals(noteEntry.name)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}

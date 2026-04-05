package dto;


import jakarta.persistence.Id;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

/**
 * Data transfer object that represents a single chat conversation summary
 * shown in the left-hand user list of the chat view.
 *
 * <p>The {@code isRead} state is backed by a JavaFX {@link BooleanProperty} so
 * that the UI can reactively update the unread indicator dot when the property changes.
 */
public class ChatPreview {

    private Long id;
    private String name;
    private String surname;
    private final BooleanProperty isRead = new SimpleBooleanProperty();

    /**
     * Creates a new chat preview entry.
     *
     * @param id      the other user's ID
     * @param name    the other user's first name
     * @param surname the other user's surname
     * @param isRead  {@code true} if the latest message in this conversation has been read
     */
    public ChatPreview (Long id, String name, String surname, Boolean isRead){
        this.id = id;
        this.name = name;
        this.surname = surname;
        setIsRead(isRead);
    }

    /** Returns the other user's first name. */
    public String getName(){
        return name;
    }

    /** Returns the other user's surname. */
    public String getSurname(){
        return surname;
    }

    /** Returns {@code true} if the latest message has been read. */
    public boolean getIsRead() { return isRead.get(); }
    /** Sets the read state, triggering any bound UI observers. */
    public void setIsRead(boolean value) { isRead.set(value); }
    /** Returns the underlying JavaFX property for UI binding. */
    public BooleanProperty isReadProperty() { return isRead; }

    /** Returns the other user's ID. */
    public Long getUserId (){
        return id;
    }
}

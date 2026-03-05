package dto;


import jakarta.persistence.Id;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class ChatPreview {

    private Long id;
    private String name;
    private String surname;
    private final BooleanProperty isRead = new SimpleBooleanProperty();

    public ChatPreview (Long id, String name, String surname, Boolean isRead){
        this.id = id;
        this.name = name;
        this.surname = surname;
        setIsRead(isRead);
    }

    public String getName(){
        return name;
    }

    public String getSurname(){
        return surname;
    }



    public boolean getIsRead() { return isRead.get(); }
    public void setIsRead(boolean value) { isRead.set(value); }
    public BooleanProperty isReadProperty() { return isRead; }

    public Long getUserId (){
        return id;
    }
}

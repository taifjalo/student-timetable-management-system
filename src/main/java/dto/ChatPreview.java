package dto;


import jakarta.persistence.Id;

public class ChatPreview {

    private Long id;
    private String name;
    private String surname;
    private Boolean isRead;

    public ChatPreview (Long id, String name, String surname, Boolean isRead){
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.isRead = isRead;
    }

    public String getName(){
        return name;
    }

    public String getSurname(){
        return surname;
    }

    public Boolean getIsRead(){
        return isRead;
    }

    public void setIsRead(Boolean isRead){
        this.isRead = isRead;
    }

    public Long getId (){
        return id;
    }
}

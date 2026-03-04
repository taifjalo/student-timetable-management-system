package org.service;

import dto.ChatPreview;
import org.dao.MessageDao;
import org.entities.Message;
import org.entities.User;

import java.util.*;

public class ChatService {

    public MessageDao messageDao = new MessageDao();

    public List<ChatPreview> getChatPreviews(Long id){
        List<Message> messages = messageDao.findUserMessages(id);
        Map<Long, ChatPreview> chatPreviews = new LinkedHashMap();
        User otherUser;
        Boolean isRead;
        for (Message message: messages){
             if (message.getSenderUser().getId().equals(id)){
                 otherUser = message.getRecipientUser();
                 isRead = true;
             }
             else {
                 otherUser = message.getSenderUser();
                 isRead = message.isRead();
             }
             if (!chatPreviews.containsKey(otherUser.getId())) {
                 chatPreviews.put(otherUser.getId(), new ChatPreview(otherUser.getId(), otherUser.getFirstName(), otherUser.getSureName(), isRead));
            }
             else{
                ChatPreview preview = chatPreviews.get(otherUser.getId());
                if (!message.isRead() && message.getRecipientUser().getId().equals(id)) {
                    preview.setIsRead(false);
                }
            }
        }
    return new ArrayList<>(chatPreviews.values());
    }

    public List<ChatPreview> getNewChatPreviews(List<ChatPreview> oldChatPreviews, Long id){
        List <ChatPreview>  chatPreviews = getChatPreviews(id);
        List <ChatPreview> newChatPreviews = new ArrayList<>();
        Boolean inList = false;
        for (ChatPreview chatPreview: chatPreviews){
            for (ChatPreview chatPreview1: oldChatPreviews){
                if (chatPreview1.getId().equals(chatPreview.getId())){
                    inList = true;
                    chatPreview.setIsRead(chatPreview1.getIsRead());
                    break;
                };
            }
            if (!inList) {
                newChatPreviews.add(chatPreview);
            }
            inList = false;
        }
        return newChatPreviews;
    }
}

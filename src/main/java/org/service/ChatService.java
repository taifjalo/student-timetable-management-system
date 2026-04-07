package org.service;

import dto.ChatPreview;
import org.dao.MessageDao;
import org.entities.Message;
import org.entities.User;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Service that builds the list of chat conversation previews shown in the
 * left-hand panel of the chat view.
 *
 * <p>A "chat preview" represents the most recent state of a conversation between
 * the current user and one other user. The read/unread status is derived by
 * inspecting message ownership: messages sent by the current user are always
 * considered read in the preview; messages received are read only if the entity
 * flag says so.
 */
public class ChatService {

    private final MessageDao messageDao;

    /**
     * Creates a {@code ChatService} with the given DAO.
     * The constructor signature also satisfies {@code @InjectMocks + @Mock} in unit tests.
     *
     * @param messageDao the DAO used to fetch messages
     */
    public ChatService(MessageDao messageDao) {
        this.messageDao = messageDao;
    }

    /**
     * Builds a list of {@link ChatPreview} objects representing all conversations
     * involving the given user.
     *
     * <p>Messages are grouped by the "other" participant. Within each group the
     * preview is marked as unread if any received message has not been read yet.
     * The map preserves insertion order (first-encountered conversation first).
     *
     * @param id the current user's ID
     * @return list of chat previews, one per unique conversation partner
     */
    public List<ChatPreview> getChatPreviews(Long id) {
        List<Message> messages = messageDao.findUserMessages(id);
        Map<Long, ChatPreview> chatPreviews = new LinkedHashMap<>();
        User otherUser;
        boolean isRead;
        for (Message message : messages) {
            if (message.getSenderUser().getId().equals(id)) {
                otherUser = message.getRecipientUser();
                isRead = true;
            } else {
                otherUser = message.getSenderUser();
                isRead = message.isRead();
            }
            if (!chatPreviews.containsKey(otherUser.getId())) {
                chatPreviews.put(otherUser.getId(), new ChatPreview(
                        otherUser.getId(), otherUser.getFirstName(), otherUser.getSureName(), isRead));
            } else {
                ChatPreview preview = chatPreviews.get(otherUser.getId());
                if (!message.isRead() && message.getRecipientUser().getId().equals(id)) {
                    preview.setIsRead(false);
                }
            }
        }
        return new ArrayList<>(chatPreviews.values());
    }

    /**
     * Returns only the conversations that are not already present in
     * {@code oldChatPreviews}. For conversations that do exist, the read status
     * in {@code oldChatPreviews} is updated in-place to reflect the latest state.
     *
     * @param oldChatPreviews the current list of previews already displayed
     * @param id              the current user's ID
     * @return list of new conversation previews not yet shown to the user
     */
    public List<ChatPreview> getNewChatPreviews(List<ChatPreview> oldChatPreviews, Long id) {
        List<ChatPreview> chatPreviews = getChatPreviews(id);
        List<ChatPreview> newChatPreviews = new ArrayList<>();
        boolean inList = false;
        for (ChatPreview chatPreview : chatPreviews) {
            for (ChatPreview chatPreview1 : oldChatPreviews) {
                if (chatPreview1.getUserId().equals(chatPreview.getUserId())) {
                    inList = true;
                    chatPreview1.setIsRead(chatPreview.getIsRead());
                    break;
                }
            }
            if (!inList) {
                newChatPreviews.add(chatPreview);
            }
            inList = false;
        }
        return newChatPreviews;
    }
}

package org.dao;

import jakarta.persistence.EntityManager;
import org.datasource.TimetableConnection;
import org.entities.Notification;
import org.entities.NotificationReceiver;
import org.entities.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Data-access object for {@link Notification} and {@link NotificationReceiver} entities.
 * Handles creating, querying, and marking notifications as read.
 *
 * <p>Display text is stored per-locale in {@code notification_translations}:
 * at creation time {@link #saveNotification} renders the message for every supported
 * locale and inserts a row per language. At query time {@link #findTranslatedForUser}
 * returns the content for the current JVM default locale, falling back to English.
 */
public class NotificationDao {

    private static final String USER_ID_PARAM = "userId";

    /** All locales for which translations are pre-rendered at save time. */
    @SuppressWarnings("deprecation")
    private static final Map<String, Locale> SUPPORTED_LOCALES = Map.of(
        "en", Locale.of("en", "US"),
        "fi", Locale.of("fi", "FI"),
        "ar", Locale.of("ar", "IQ"),
        "ru", Locale.of("ru", "RU")
    );

    /**
     * Creates a new notification, pre-renders its message into all supported
     * locales, stores each translation in {@code notification_translations}, and
     * delivers the notification to all specified recipients.
     *
     * @param messageKey    i18n bundle key for the notification message template
     * @param messageParams pipe-separated format arguments matching the bundle key's
     *                      {@code %s} placeholders (may be {@code null})
     * @param recipientIds  IDs of users who should receive this notification
     * @return the persisted {@link Notification} with its generated ID set
     */
    public Notification saveNotification(String messageKey, String messageParams, List<Long> recipientIds) {
        try (EntityManager em = TimetableConnection.createEntityManager()) {
            em.getTransaction().begin();

            Notification notification = new Notification(LocalDateTime.now());
            em.persist(notification);
            em.flush(); // ensure notification_id is generated before inserting translations

            String[] params = messageParams != null ? messageParams.split("\\|") : new String[0];
            for (Map.Entry<String, Locale> entry : SUPPORTED_LOCALES.entrySet()) {
                String content = renderContent(messageKey, params, entry.getValue());
                em.createNativeQuery(
                    "INSERT INTO notification_translations (notification_id, language_code, content) "
                    + "VALUES (?, ?, ?)"
                ).setParameter(1, notification.getId())
                 .setParameter(2, entry.getKey())
                 .setParameter(3, content)
                 .executeUpdate();
            }

            for (Long recipientId : recipientIds) {
                User user = em.getReference(User.class, recipientId);
                em.persist(new NotificationReceiver(user, notification));
            }

            em.getTransaction().commit();
            return notification;
        }
    }

    /**
     * Returns notification data for the given user as raw rows, with the content
     * resolved for the JVM default locale (falling back to English if no translation
     * exists for that locale).
     *
     * <p>Each {@code Object[]} element contains:
     * <ol>
     *   <li>{@code notification_id} — {@link Number}</li>
     *   <li>{@code content} — {@link String}</li>
     *   <li>{@code sent_at} — {@link java.sql.Timestamp}</li>
     *   <li>{@code is_read} — {@link Number} (0 = false, 1 = true)</li>
     * </ol>
     *
     * @param userId the recipient's user ID
     * @return list of raw rows ordered newest-first
     */
    public List<Object[]> findTranslatedForUser(Long userId) {
        String lang = Locale.getDefault().getLanguage();
        try (EntityManager em = TimetableConnection.createEntityManager()) {
            @SuppressWarnings("unchecked")
            List<Object[]> result = em.createNativeQuery(
                "SELECT nr.notification_id, "
                + "  COALESCE(nt.content, nt_en.content) AS content, "
                + "  n.sent_at, nr.is_read "
                + "FROM notification_receivers nr "
                + "JOIN notifications n ON n.notification_id = nr.notification_id "
                + "LEFT JOIN notification_translations nt "
                + "  ON nt.notification_id = nr.notification_id AND nt.language_code = ? "
                + "LEFT JOIN notification_translations nt_en "
                + "  ON nt_en.notification_id = nr.notification_id AND nt_en.language_code = 'en' "
                + "WHERE nr.user_id = ? "
                + "ORDER BY n.sent_at DESC"
            )
            .setParameter(1, lang)
            .setParameter(2, userId)
            .getResultList();
            return result;
        }
    }

    /**
     * Returns all notification receiver rows for the given user, ordered by
     * notification date descending. The associated {@link Notification} is eagerly
     * fetched to avoid lazy-load issues after the session closes.
     *
     * @param userId the recipient's user ID
     * @return list of receiver records, possibly empty
     */
    public List<NotificationReceiver> findByUserId(Long userId) {
        try (EntityManager em = TimetableConnection.createEntityManager()) {
            return em.createQuery(
                "SELECT nr FROM NotificationReceiver nr "
                + "JOIN FETCH nr.notification n "
                + "WHERE nr.id.userId = :userId "
                + "ORDER BY n.sentAt DESC",
                NotificationReceiver.class
            ).setParameter(USER_ID_PARAM, userId).getResultList();
        }
    }

    /**
     * Marks a single notification as read for the specified user.
     *
     * @param userId         the recipient's user ID
     * @param notificationId the notification's primary key
     */
    public void markAsRead(Long userId, Long notificationId) {
        try (EntityManager em = TimetableConnection.createEntityManager()) {
            em.getTransaction().begin();
            em.createQuery(
                "UPDATE NotificationReceiver nr SET nr.isRead = true "
                + "WHERE nr.id.userId = :userId AND nr.id.notificationId = :notifId"
            ).setParameter(USER_ID_PARAM, userId)
             .setParameter("notifId", notificationId)
             .executeUpdate();
            em.getTransaction().commit();
        }
    }

    /**
     * Marks all notifications as read for the specified user.
     *
     * @param userId the recipient's user ID
     */
    public void markAllAsRead(Long userId) {
        try (EntityManager em = TimetableConnection.createEntityManager()) {
            em.getTransaction().begin();
            em.createQuery(
                "UPDATE NotificationReceiver nr SET nr.isRead = true "
                + "WHERE nr.id.userId = :userId"
            ).setParameter(USER_ID_PARAM, userId).executeUpdate();
            em.getTransaction().commit();
        }
    }

    /**
     * Returns the number of unread notifications for the specified user.
     *
     * @param userId the recipient's user ID
     * @return count of unread notifications (0 if none)
     */
    public long countUnread(Long userId) {
        try (EntityManager em = TimetableConnection.createEntityManager()) {
            return (Long) em.createQuery(
                "SELECT COUNT(nr) FROM NotificationReceiver nr "
                + "WHERE nr.id.userId = :userId AND nr.isRead = false"
            ).setParameter(USER_ID_PARAM, userId).getSingleResult();
        }
    }

    /**
     * Renders the notification message for a specific locale by looking up the
     * bundle key and formatting it with the supplied arguments.
     * Falls back to the raw key if the lookup or formatting fails.
     *
     * @param key    the i18n bundle key
     * @param params format arguments for the bundle string's {@code %s} placeholders
     * @param locale the target locale
     * @return the rendered notification text
     */
    private static String renderContent(String key, String[] params, Locale locale) {
        try {
            ResourceBundle bundle = ResourceBundle.getBundle("i18n/MessagesBundle", locale);
            return String.format(bundle.getString(key), (Object[]) params);
        } catch (Exception e) {
            return key;
        }
    }
}

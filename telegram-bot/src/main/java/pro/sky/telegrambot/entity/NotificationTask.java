package pro.sky.telegrambot.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

// Сущьность для хранения в БД. Пришлось заморочиться с названием колонок и генерацмей id
@Entity
@Table(name = "notification_task")
public class NotificationTask {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @JoinColumn(name = "chat_id")
    private long chatId;
    private String message;
    @JoinColumn(name = "date_time")
    private LocalDateTime dateTime;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getChatId() {
        return chatId;
    }

    public void setChatId(long chatId) {
        this.chatId = chatId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime localDateTime) {
        this.dateTime = localDateTime;
    }
}

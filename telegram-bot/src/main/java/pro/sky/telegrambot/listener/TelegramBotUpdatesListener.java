package pro.sky.telegrambot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.entity.NotificationTask;
import pro.sky.telegrambot.repository.NotificationTaskRepository;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TelegramBotUpdatesListener implements UpdatesListener {

    private Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);
    private final NotificationTaskRepository notificationTaskRepository;

    @Autowired
    private TelegramBot telegramBot;

    public TelegramBotUpdatesListener(NotificationTaskRepository notificationTaskRepository) {
        this.notificationTaskRepository = notificationTaskRepository;
    }

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }

    @Override
    public int process(List<Update> updates) {
        updates.forEach(update -> {
            logger.info("Processing update: {}", update);

            if(update.message().text().startsWith("/")) {
                String messageText;
                if (update.message().text().equals("/start")) {
                    messageText = "Приветстую! Я бот, который будет напоминать вам о делах. Например введите 01.01.2022 20:00 Сделать домашнюю работу. В 20:00 1 января 2022 года я пришлю вам напоминание с текстом “Сделать домашнюю работу” ";
                } else {
                    messageText = "Извините, но такую команду не знаю!";
                }
                SendMessage message = new SendMessage(update.message().chat().id(), messageText);
                telegramBot.execute(message);
            } else {
                saveEntity(update);
            }
        });
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    private void saveEntity(Update update) {
        logger.info("Processing update: {}", update);
        if(!(update.message().text() == null)) {
            NotificationTask notificationTask = new NotificationTask();
            notificationTask.setChatId(update.message().chat().id());
            notificationTask.setMessage(parsingString(update.message().text()));
            notificationTask.setDateTime(parsingDate(update.message().text()));
            notificationTaskRepository.save(notificationTask);
        }
    }

    private LocalDateTime parsingDate(String messageText) {
        Pattern pattern = Pattern.compile("([0-9\\.\\:\\s]{16})(\\s)([\\W+]+)");
        Matcher matcher = pattern.matcher(messageText);
        LocalDateTime date = null;
        if (matcher.matches()) {
            date = LocalDateTime.parse(matcher.group(1), DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
        }
        return date;
    }

    private String parsingString(String messageText) {
        Pattern pattern = Pattern.compile("([0-9\\.\\:\\s]{16})(\\s)([\\W+]+)");
        Matcher matcher = pattern.matcher(messageText);
        String text = "";
        if (matcher.matches()) {
            text = matcher.group(3);
        }
        return text;
    }


}

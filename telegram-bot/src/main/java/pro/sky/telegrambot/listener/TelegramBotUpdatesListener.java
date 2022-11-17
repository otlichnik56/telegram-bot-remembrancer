package pro.sky.telegrambot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.entity.NotificationTask;
import pro.sky.telegrambot.repository.NotificationTaskRepository;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TelegramBotUpdatesListener implements UpdatesListener {

    private final Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);
    private final TelegramBot telegramBot;
    private final NotificationTaskRepository notificationTaskRepository;
    private static Pattern pattern = Pattern.compile("([0-9\\.\\:\\s]{16})(\\s)([\\W+]+)");
    private static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    public TelegramBotUpdatesListener(NotificationTaskRepository notificationTaskRepository, TelegramBot telegramBot) {
        this.notificationTaskRepository = notificationTaskRepository;
        this.telegramBot = telegramBot;
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
                answer(update);
            } else {
                saveEntity(update);
            }
        });
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    // Отправляет сообщение на команду /start или если не распознал команду
    private void answer(Update update) {
        String messageText;
        if (update.message().text().equals("/start")) {
            messageText = "Приветстую! Я бот, который будет напоминать вам о делах. Например введите 01.01.2022 20:00 Сделать домашнюю работу. В 20:00 1 января 2022 года я пришлю вам напоминание с текстом “Сделать домашнюю работу” ";
        } else {
            messageText = "Извините, но такую команду не знаю!";
        }
        SendMessage message = new SendMessage(update.message().chat().id(), messageText);
        telegramBot.execute(message);
    }

    // Сохраняет корректную запись в БД. При некорректном вводе отправляет сообщение "Запись введена не корректно"
    private void saveEntity(Update update) {
        String messageText = update.message().text();
        if(!(messageText == null)) {
            NotificationTask notificationTask = new NotificationTask();
            notificationTask.setChatId(update.message().chat().id());
            notificationTask.setMessage(parsingString(messageText));
            notificationTask.setDateTime(parsingDate(messageText));
            if(parsingString(messageText) == null || parsingDate(messageText) == null){
                SendMessage message = new SendMessage(update.message().chat().id(), "Запись введена не корректно");
                telegramBot.execute(message);
            } else {
                notificationTaskRepository.save(notificationTask);
            }
        }
    }

    // Парсит строку. Получает дату и время. При некорректном вводе записи возвращает null
    private LocalDateTime parsingDate(String messageText) {
        Matcher matcher = pattern.matcher(messageText);
        LocalDateTime date = null;
        if (matcher.matches()) {
            date = LocalDateTime.parse(matcher.group(1), dateTimeFormatter);
        }
        return date;
    }

    // Парсит строку. Получает строку. При некорректном вводе записи возвращает null
    private String parsingString(String messageText) {
        Matcher matcher = pattern.matcher(messageText);
        String text = null;
        if (matcher.matches()) {
            text = matcher.group(3);
        }
        return text;
    }

}

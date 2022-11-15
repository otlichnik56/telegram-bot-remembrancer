package pro.sky.telegrambot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private final Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);
    private final TelegramBot telegramBot;
    private final NotificationTaskRepository notificationTaskRepository;


    // Хотел порефачить код, но времени до сдачи почти нет, займусь этим потом!!!!! Главное всё работает!
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

    @Scheduled(cron = "0 0/1 * * * *")
    public void run() {
        LocalDateTime localDateTime = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        List<NotificationTask> result = notificationTaskRepository.getNotificationTaskNowDateTime(localDateTime);
        if (!(result == null)){
            for (NotificationTask notificationTask : result) {
                SendMessage message = new SendMessage(notificationTask.getChatId(), notificationTask.getMessage());
                telegramBot.execute(message);
            }
        }
    }

    // Сохраняет корректную запись в БД. При некорректном вводе отправляет сообщение "Запись введена не корректно"
    private void saveEntity(Update update) {
        logger.info("Processing update: {}", update);
        if(!(update.message().text() == null)) {
            NotificationTask notificationTask = new NotificationTask();
            notificationTask.setChatId(update.message().chat().id());
            notificationTask.setMessage(parsingString(update.message().text()));
            notificationTask.setDateTime(parsingDate(update.message().text()));
            if(parsingString(update.message().text()) == null || parsingDate(update.message().text()) == null){
                SendMessage message = new SendMessage(update.message().chat().id(), "Запись введена не корректно");
                telegramBot.execute(message);
            } else {
                notificationTaskRepository.save(notificationTask);
            }
        }
    }

    // Парсит строку. Получает дату и время. При некорректном вводе записи возвращает null
    private LocalDateTime parsingDate(String messageText) {
        Pattern pattern = Pattern.compile("([0-9\\.\\:\\s]{16})(\\s)([\\W+]+)");
        Matcher matcher = pattern.matcher(messageText);
        LocalDateTime date = null;
        if (matcher.matches()) {
            date = LocalDateTime.parse(matcher.group(1), DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
        }
        return date;
    }

    // Парсит строку. Получает строку. При некорректном вводе записи возвращает null
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

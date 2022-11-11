package pro.sky.telegrambot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;

@Service
public class TelegramBotUpdatesListener implements UpdatesListener {

    private Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);

    @Autowired
    private TelegramBot telegramBot;

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }

    @Override
    public int process(List<Update> updates) {

        updates.forEach(update -> {
            logger.info("Processing update: {}", update);
            // Process your updates here
            String messageText = update.message().text();
            Long chatId = update.message().chat().id();
            telegramBot.setUpdatesListener(up -> {
                // Обрабатываем входящие сообщения. Как?
                // возвращаем id последнего обработанного сообщения или отмечаем все как обработанные. Как?
                SendMessage message = new SendMessage(chatId, messageText);
                SendResponse response = telegramBot.execute(message);
                return UpdatesListener.CONFIRMED_UPDATES_ALL;
            });
        });
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

}

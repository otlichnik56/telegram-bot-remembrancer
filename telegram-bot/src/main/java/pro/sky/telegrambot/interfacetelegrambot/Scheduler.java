package pro.sky.telegrambot.interfacetelegrambot;

public interface Scheduler {
    int CONFIRMED_UPDATES_ALL = -1;
    int CONFIRMED_UPDATES_NONE = -2;

    void run();
}

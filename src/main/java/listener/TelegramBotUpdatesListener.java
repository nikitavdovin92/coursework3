package listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import jakarta.annotation.Nullable;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.NotificationTaskService;
import service.TelegramBotService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TelegramBotUpdatesListener implements UpdatesListener {
    private static final Logger LOG = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);
    private final TelegramBot telegramBot;

    private final TelegramBotService telegramBotService;

    private final NotificationTaskService notificationTaskService;
    private static final Pattern PATTERN = Pattern.compile("(\\d{1,2}.\\d{1,2}.\\d{4} \\d{1,2}:\\d{2}) ([А-яA-z\\d,\\s!?.]+)");

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("d.M.yyyy HH:mm");

    public TelegramBotUpdatesListener(TelegramBot telegramBot, TelegramBotService telegramBotService, NotificationTaskService notificationTaskService) {
        this.telegramBot = telegramBot;
        this.telegramBotService = telegramBotService;
        this.notificationTaskService = notificationTaskService;
    }

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }

    @Override
    public int process(List<Update> updates) {
        updates.forEach(update -> {
            LOG.info("Processing update: {}", update);
            Long chatId = update.message().chat().id();
            Message message = update.message();
            String text = message.text();
            LocalDateTime dateTime;
            if (update.message() != null && text != null) {
                Matcher matcher = PATTERN.matcher(text);
                if (text.equals("/start")) {
                    telegramBotService.sendMessage(chatId,
                            "Для планирования задачи отправьте ее в формате:\\n*01.01.2023 20:00 Сделать домашнюю работу*",
                            ParseMode.Markdown
                    );
                } else if (matcher.matches() && (dateTime = parse(matcher.group(1))) != null) {
                    notificationTaskService.save(chatId, matcher.group(2), dateTime);
                    telegramBotService.sendMessage(chatId,
                            "Ваша задача успешно запланирована");
                }else {
                    telegramBotService.sendMessage(chatId,
                            "Отправьте команду /start или сообщение для планирования задачи");
                }
            }
        });
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }
    @Nullable
    private LocalDateTime parse(String DataTime) {
        try {
            return LocalDateTime.parse(DataTime, DATE_TIME_FORMATTER);
        }catch (DateTimeParseException e) {
            return null;
        }
    }
}

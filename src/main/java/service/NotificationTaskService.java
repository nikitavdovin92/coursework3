package service;


import entity.NotificationTask;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import repository.NotificationTaskRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
public class NotificationTaskService {
    private final NotificationTaskRepository notificationTaskRepository;

    public NotificationTaskService(NotificationTaskRepository notificationTaskRepository) {
        this.notificationTaskRepository = notificationTaskRepository;
    }

    @Transactional
    public void save (Long chatId, String text, LocalDateTime dateTime) {
        notificationTaskRepository.save(
                new NotificationTask(
                        text,
                        chatId,
                        dateTime.truncatedTo(ChronoUnit.MINUTES)
                )
        );
    }


}

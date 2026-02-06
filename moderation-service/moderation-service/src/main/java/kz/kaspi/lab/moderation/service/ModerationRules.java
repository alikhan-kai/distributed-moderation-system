package kz.kaspi.lab.moderation.service;

import kz.kaspi.lab.moderation.model.AppealEvent;
import kz.kaspi.lab.moderation.model.ClientDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Component
@Slf4j
public class ModerationRules {
    // Проверка актуальности: заявки больше 24 часов не обрабатываются автоматически
    public boolean isFresh(AppealEvent event) {
        if (event.getTimestamp() == null) return true;
        LocalDateTime cutoff = LocalDateTime.now().minusHours(24);
        if (event.getTimestamp().isBefore(cutoff)) {
            log.warn("Rule: Freshness — Событие {} просрочено (создано {})", event.getEventId(), event.getTimestamp());
            return false;
        }
        return true;
    }

    // Ограничение по времени для специфичных категорий (например, Кредиты)
    public boolean isAllowedByTime(AppealEvent event){
        if(event.getTimestamp() == null) return true;
        LocalTime appealTime = event.getTimestamp().toLocalTime();
        String category = event.getCategory() != null ? event.getCategory().toUpperCase() : "GENERAL";

        // Кредитные заявки обрабатываются только в стандартное рабочее время (09:00 - 18:00)
        if(category.contains("CREDIT")){
            if(appealTime.isBefore(LocalTime.of(9, 0)) || appealTime.isAfter(LocalTime.of(18, 0))){
                log.warn("Rule: Working Hours — Категория {} запрещена в {}", category, appealTime);
                return false;
            }
        }
        return true;
    }

    // Проверка наличия уже открытых заявок у клиента (блокировка дубликатов в системе)
    public boolean hasNoActiveAppeals(ClientDetails details){
        if(details.isHasActiveAppeals()){
            log.warn("Rule: Active Appeals — У клиента {} уже есть открытые заявки", details.getClientId());
            return false;
        }
        return true;
    }
}
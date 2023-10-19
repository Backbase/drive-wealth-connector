package com.backbase.modelbank.scheduler;

import com.backbase.modelbank.config.DriveWealthConfigurationProperties;
import com.backbase.modelbank.config.DriveWealthConfigurationProperties.Holiday;
import com.backbase.modelbank.mapper.MarketStatusMapper;
import com.backbase.portfolio.instrument.integration.api.service.v1.MarketManagementApi;
import com.backbase.portfolio.instrument.integration.api.service.v1.model.MarketStatusEnum;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.TriggerContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Component
@Slf4j
public class MarketStatusScheduler {

    private final DriveWealthConfigurationProperties configurationProperties;
    private final MarketStatusMapper marketStatusMapper;
    private final MarketManagementApi marketManagementApi;

    @PostConstruct
    public void onStartUp() {
        var openTime = getGetTimeFromCron(configurationProperties.getMarketStatus().open().intervalInCron());
        var closeTime = getGetTimeFromCron(configurationProperties.getMarketStatus().close().intervalInCron());
        var currentDate = Calendar.getInstance();

        if (currentDate.before(openTime) || currentDate.after(closeTime)) {
            updateMarketCloseStatus();
        } else {
            updateMarketOpenStatus();
        }
    }

    @Scheduled(cron = "${drive-wealth.market-status.open.interval-in-cron}")
    public void updateMarketOpenStatus() {
        if (!isTodayPublicHoliday() && !isWeekend()) {
            updateMarketStatus(MarketStatusEnum.OPEN.getValue());
        } else {
            updateMarketStatus(MarketStatusEnum.CLOSE.getValue());
        }
    }

    @Scheduled(cron = "${drive-wealth.market-status.close.interval-in-cron}")
    public void updateMarketCloseStatus() {
        updateMarketStatus(MarketStatusEnum.CLOSE.getValue());
    }

    private void updateMarketStatus(String marketStatus) {
        Flux.fromIterable(configurationProperties.getMarketStatus().markets())
            .flatMap(marketExchange -> marketManagementApi.putMarketStatus(marketExchange.id(),
                    marketStatusMapper.mapMarketStatusPutRequestBody(marketStatus))
                .then(Mono.just(marketExchange.id()))
                .doOnSuccess(
                    c -> log.info("Market {} Status Updated to {}", marketExchange.name(), marketStatus))
                .onErrorResume(WebClientResponseException.NotFound.class, throwable -> Mono.empty())
                .switchIfEmpty(
                    Mono.defer(() -> this.marketManagementApi.postMarket(
                                marketStatusMapper.mapMarket(marketExchange, marketStatus))
                            .doOnSuccess(
                                c -> log.info("Created Market {} with Status {}", marketExchange.name(), marketStatus)))
                        .then(Mono.fromCallable(marketExchange::id))))
            .subscribe();
    }

    private boolean isTodayPublicHoliday() {
        for (Holiday holiday : configurationProperties.getMarketStatus().publicHolidays()) {
            if (Objects.equals(holiday,
                new Holiday(LocalDate.now().getDayOfMonth(), LocalDate.now().getMonthValue()))) {
                log.info("Today is a holiday");
                return true;
            }
        }
        return false;
    }

    private Calendar getGetTimeFromCron(String cronExpression) {

        CronTrigger trigger = new CronTrigger(cronExpression);
        TriggerContext context = new TriggerContext() {

            public Date lastScheduledExecutionTime() {
                return null;
            }

            public Date lastActualExecutionTime() {
                return null;
            }

            public Date lastCompletionTime() {
                return null;
            }
        };

        var nextExecution = Calendar.getInstance();
        Optional.ofNullable(trigger.nextExecutionTime(context)).ifPresent(nextExecution::setTime);
        nextExecution.set(Calendar.DAY_OF_MONTH, LocalDate.now().getDayOfMonth());
        return nextExecution;
    }

    private boolean isWeekend() {
        return Calendar.getInstance().get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY
            || Calendar.getInstance().get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY;
    }
}

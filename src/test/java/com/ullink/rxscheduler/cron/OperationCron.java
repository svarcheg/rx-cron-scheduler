package com.ullink.rxscheduler.cron;

import rx.Observable.OnSubscribeFunc;
import rx.Observer;
import rx.Subscription;
import rx.subscriptions.Subscriptions;
import rx.util.functions.Action0;

import com.ullink.rxscheduler.cron.calendar.Calendar;
import com.ullink.rxscheduler.cron.calendar.CronExpression;

public class OperationCron {

    /**
     * Creates an event each time interval.
     */
    public static OnSubscribeFunc<Long> cron(final CronExpression cronExpression, final Calendar calendar, final RxCronScheduler scheduler) {
        // wrapped in order to work with multiple subscribers
        return new OnSubscribeFunc<Long>() {
            @Override
            public Subscription onSubscribe(Observer<? super Long> observer) {
                return new Fire(cronExpression, calendar, scheduler).onSubscribe(observer);
            }
        };
    }

    private static class Fire implements OnSubscribeFunc<Long> {
        private final CronExpression cronExpression;
        private final Calendar calendar;
        private final RxCronScheduler scheduler;

        private long currentValue;

        private Fire(CronExpression cronExpression, Calendar calendar, RxCronScheduler scheduler) {
            this.cronExpression = cronExpression;
            this.calendar = calendar;
            this.scheduler = scheduler;
        }

        @Override
        public Subscription onSubscribe(final Observer<? super Long> observer) {
            final Subscription wrapped = scheduler.schedule(new Action0() {
                @Override
                public void call() {
                    observer.onNext(currentValue);
                    currentValue++;
                }
            }, cronExpression, calendar);

            return Subscriptions.create(new Action0() {
                @Override
                public void call() {
                    wrapped.unsubscribe();
                }
            });
        }
    }
}

package com.ullink.rxscheduler.cron;

import rx.Scheduler;
import rx.Subscription;
import rx.util.functions.Action0;
import rx.util.functions.Func2;
import com.ullink.rxscheduler.cron.calendar.Calendar;
import com.ullink.rxscheduler.cron.calendar.CronExpression;

public interface RxCronScheduler
{
    /**
     * Schedules an action without any parameters using cron expression. Check {@link CronExpression} for the format.
     * @param action
     * @param cronExpression
     * @return a subscription, use the unsubscribe to cancel scheduled executions.
     */
    Subscription schedule(final Action0 action, final CronExpression cronExpression);

    /**
     * In addition if scheduling an action using a cron expression, as {@link RxCronScheduler#schedule(Action0, CronExpression)} it also
     * takes a {@link Calendar} parameter. This calendar, depending on an underlying implementation may be used to exclude holidays, some days
     * of wee and so on. Check com.ullink.rxscheduler.cron.calendar for details.
     * @param action
     * @param cronExpression
     * @param calendar
     * @return
     */
    Subscription schedule(final Action0 action, final CronExpression cronExpression, final Calendar calendar);

    /**
     * Same as {@link RxCronScheduler#schedule(Action0, CronExpression)} but takes an rx-java {@link Func2} parameter.
     * @param state
     * @param action
     * @param cronExpression
     * @return
     */
    <T> Subscription schedule(T state, final Func2<? super Scheduler, ? super T, ? extends Subscription> action, final CronExpression cronExpression);

    /**
     * Same as {@link RxCronScheduler#schedule(Action0, CronExpression, Calendar)} but takes an rx-java {@link Func2} parameter.
     * @param state
     * @param action
     * @param cronExpression
     * @param calendar
     * @return
     */
    <T> Subscription schedule(T state, final Func2<? super Scheduler, ? super T, ? extends Subscription> action, final CronExpression cronExpression, final Calendar calendar);
}

package com.ullink.rxscheduler.cron;

import rx.Scheduler;
import rx.Subscription;
import rx.util.functions.Action0;
import rx.util.functions.Func2;
import com.ullink.rxscheduler.cron.calendar.Calendar;
import com.ullink.rxscheduler.cron.calendar.CronExpression;

public interface RxCronScheduler
{
    Subscription schedule(final Action0 action, final CronExpression cronExpression);

    Subscription schedule(final Action0 action, final CronExpression cronExpression, final Calendar calendar);

    <T> Subscription schedule(T state, final Func2<? super Scheduler, ? super T, ? extends Subscription> action, final CronExpression cronExpression);

    <T> Subscription schedule(T state, final Func2<? super Scheduler, ? super T, ? extends Subscription> action, final CronExpression cronExpression, final Calendar calendar);
}

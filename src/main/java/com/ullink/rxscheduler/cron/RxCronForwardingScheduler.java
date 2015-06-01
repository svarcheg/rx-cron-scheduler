package com.ullink.rxscheduler.cron;

import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import rx.Scheduler;
import rx.Subscription;
import rx.schedulers.TestScheduler;
import rx.subscriptions.Subscriptions;
import rx.util.functions.Action0;
import rx.util.functions.Func2;
import com.ullink.rxscheduler.cron.calendar.Calendar;
import com.ullink.rxscheduler.cron.calendar.CronExpression;

/**
 * Using forwarding here because would like to be able to reuse {@link TestScheduler} features.
 */
public class RxCronForwardingScheduler extends Scheduler implements RxCronScheduler
{

    private final Scheduler underlying;

    public RxCronForwardingScheduler(Scheduler underlying)
    {
        this.underlying = underlying;
    }

    @Override
    public Subscription schedule(Action0 action)
    {
        return underlying.schedule(action);
    }

    @Override
    public <T> Subscription schedule(T state, Func2<? super Scheduler, ? super T, ? extends Subscription> action)
    {
        return underlying.schedule(state, action);
    }

    @Override
    public Subscription schedule(Action0 action, long dueTime, TimeUnit unit)
    {
        return underlying.schedule(action, dueTime, unit);
    }

    @Override
    public <T> Subscription schedule(T state, Func2<? super Scheduler, ? super T, ? extends Subscription> action, long dueTime, TimeUnit unit)
    {
        return underlying.schedule(state, action, dueTime, unit);
    }

    @Override
    public Subscription schedulePeriodically(Action0 action, long initialDelay, long period, TimeUnit unit)
    {
        return underlying.schedulePeriodically(action, initialDelay, period, unit);
    }

    @Override
    public <T> Subscription schedulePeriodically(T state, Func2<? super Scheduler, ? super T, ? extends Subscription> action, long initialDelay, long period, TimeUnit unit)
    {
        return underlying.schedulePeriodically(state, action, initialDelay, period, unit);
    }

    @Override
    public long now()
    {
        return underlying.now();
    }

    private Date findNextExecutionTime(Date guess, CronExpression cronExpression, Calendar calendar)
    {
        Date fireTime = cronExpression.getNextValidTimeAfter(guess);
        while (fireTime != null && calendar != null && !calendar.isTimeIncluded(fireTime.getTime()))
        {
            fireTime = findNextExecutionTime(fireTime, cronExpression, calendar);

        }
        return fireTime;
    }

    @Override
    public Subscription schedule(Action0 action, CronExpression cronExpression)
    {
        return schedule(action, cronExpression, null);
    }

    @Override
    public Subscription schedule(final Action0 action, final CronExpression cronExpression, final Calendar calendar)
    {
        return schedule(null, new Func2<Scheduler, Void, Subscription>()
        {

            @Override
            public Subscription call(Scheduler scheduler, Void state)
            {
                action.call();
                return Subscriptions.empty();
            }
        }, cronExpression, calendar);
    }

    @Override
    public <T> Subscription schedule(T state, final Func2<? super Scheduler, ? super T, ? extends Subscription> action, final CronExpression cronExpression)
    {
        return schedule(state, action, cronExpression, null);
    }

    final class RecursiveAction<T>  implements  Func2<Scheduler, T, Subscription>
    {
        private final AtomicBoolean                                               complete;
        private final Func2<? super Scheduler, ? super T, ? extends Subscription> action;
        private final CronExpression                                              cronExpression;
        private final Calendar                                                    calendar;
        private final Date dueTime;

        RecursiveAction(AtomicBoolean complete, Func2<? super Scheduler, ? super T, ? extends Subscription> action, CronExpression cronExpression, Calendar calendar, Date dueTime)
        {
            this.complete = complete;
            this.action = action;
            this.cronExpression = cronExpression;
            this.calendar = calendar;
            this.dueTime = dueTime;
        }

        @Override
        public Subscription call(Scheduler scheduler, T state0)
        {
            if (!complete.get())
            {
                final Subscription sub1;
                if (scheduler.now() < dueTime.getTime())
                {
                    sub1 = Subscriptions.empty();
                }
                else
                {
                    sub1 = action.call(scheduler, state0);
                }
                Date fireTime = findNextExecutionTime(new Date(scheduler.now()), cronExpression, calendar);
                final Subscription sub2;
                if (fireTime == null)
                {
                    sub2 = Subscriptions.empty();
                }
                else
                {
                    sub2 = scheduler.schedule(state0, new RecursiveAction<T>(complete, action, cronExpression, calendar, fireTime), fireTime);
                }
                return Subscriptions.create(new Action0()
                {
                    @Override
                    public void call()
                    {
                        sub1.unsubscribe();
                        sub2.unsubscribe();
                    }
                });
            }
            return Subscriptions.empty();
        }
    }


    @Override
    public <T> Subscription schedule(T state, final Func2<? super Scheduler, ? super T, ? extends Subscription> action, final CronExpression cronExpression, final Calendar calendar)
    {
        final AtomicBoolean complete = new AtomicBoolean();
        Date initialfireTime = findNextExecutionTime(new Date(now()), cronExpression, calendar);
        RecursiveAction<T> recursiveAction = new RecursiveAction<T>(complete, action, cronExpression, calendar, initialfireTime);
        final Subscription sub;
        if (initialfireTime == null)
        {
            sub = Subscriptions.empty();
        }
        else
        {
            sub = schedule(state, recursiveAction, initialfireTime);
        }
        return Subscriptions.create(new Action0()
        {
            @Override
            public void call()
            {
                complete.set(true);
                sub.unsubscribe();
            }
        });
    }
}

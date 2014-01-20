package com.ullink.rxscheduler.cron;

import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import rx.schedulers.TestScheduler;
import rx.util.functions.Action0;
import com.ullink.rxscheduler.cron.calendar.CronExpression;
import com.ullink.rxscheduler.cron.calendar.WeeklyCalendar;

public class TestSchedulingService {

        private static class SideEffectTask implements Action0
        {
                private AtomicLong counter = new AtomicLong();
                @Override
                public void call() {
                        counter.incrementAndGet();
                }
        }

        @Test
        public void testOnce() throws Exception {
                TestScheduler underlying = new TestScheduler();
                RxCronScheduler service = new RxCronForwardingScheduler(underlying);
                final SideEffectTask task = new SideEffectTask();
                service.schedule(task, new CronExpression("0 0 15 01 01 ? 1970"));
                Assert.assertEquals(0, task.counter.intValue());
                underlying.advanceTimeTo(System.currentTimeMillis(), TimeUnit.MILLISECONDS);
                Assert.assertEquals(1, task.counter.intValue());
        }

        @Test
        public void testWithCalendar() throws Exception {
                TestScheduler underlying = new TestScheduler();
                RxCronScheduler service = new RxCronForwardingScheduler(underlying);
                final SideEffectTask task = new SideEffectTask();
                underlying.advanceTimeTo(new GregorianCalendar(1970, 00, 01).getTimeInMillis(), TimeUnit.MILLISECONDS);
                WeeklyCalendar weeklyCalendar = new WeeklyCalendar();
                weeklyCalendar.setDaysExcluded(new boolean [] {false, false, true, true,true,true,true, true});
                service.schedule(task, new CronExpression("0 0 15 ? * *"), weeklyCalendar );
                Assert.assertEquals(0, task.counter.intValue());
                underlying.advanceTimeTo(new GregorianCalendar(1970, 11, 31).getTimeInMillis(), TimeUnit.MILLISECONDS);
                Assert.assertEquals(52, task.counter.intValue());
        }


        @Test
        @Ignore
        public void testDaily() throws Exception {
                TestScheduler underlying = new TestScheduler();
                RxCronScheduler service = new RxCronForwardingScheduler(underlying);
                SideEffectTask task = new SideEffectTask();
                service.schedule(task, new CronExpression("0 0 15 ? * *"));
                Assert.assertEquals(0, task.counter.intValue());
                underlying.advanceTimeBy(16, TimeUnit.HOURS);
                Assert.assertEquals(1, task.counter.intValue());
                underlying.advanceTimeBy(23, TimeUnit.HOURS);
                Assert.assertEquals(2, task.counter.intValue());
        }

}

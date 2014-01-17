rx-cron-scheduler
=================

Main idea here is to add Quartz (http://quartz-scheduler.org/)
calendars and crons to rx-java (https://github.com/Netflix/RxJava)
schedulers. It is a "forwarding" scheduler implementation, so one can
change the "inner" scheduler (to replace it by a TestScheduler for
example) at will.

Installation
------------
For now please grab the jar from cloudbees CI server.

Usage
------------
```java
static class SideEffectTask implements Action0
{
   private AtomicLong counter = new AtomicLong();
   @Override
   public void call() {
     counter.incrementAndGet();
   }
}
ExecutorService executor = Executors.newSingleThreadScheduledExecutor();
Scheduler inner = Schedulers.executor(executor);
RxCronScheduler service = new RxCronForwardingScheduler(inner);
final SideEffectTask task = new SideEffectTask();
WeeklyCalendar weeklyCalendar = new WeeklyCalendar();
weeklyCalendar.setDaysExcluded(new boolean [] {false, false, true, true,true,true,true, true});
service.schedule(task, new CronExpression("0 0 15 ? * *"), weeklyCalendar );
```

Build status
------------

[![Build Status](https://svarcheg.ci.cloudbees.com/job/rx-cron-scheduler/badge/icon)](https://svarcheg.ci.cloudbees.com/job/rx-cron-scheduler/badge/icon)


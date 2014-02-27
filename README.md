rx-cron-scheduler
=================

Main idea here is to add Quartz (http://quartz-scheduler.org/)
calendars and crons to rx-java (https://github.com/Netflix/RxJava)
schedulers. It is a "forwarding" scheduler implementation, so one can
change the "inner" scheduler (to replace it by a TestScheduler for
example) at will.

Build status
------------

[![Build Status](https://svarcheg.ci.cloudbees.com/buildStatus/icon?job=rx-cron-scheduler)](https://svarcheg.ci.cloudbees.com/me/my-views/view/All/job/rx-cron-scheduler/)


Dependency status
------------

[![Dependency Status](https://www.versioneye.com/user/projects/52e26562ec137520dc00003f/badge.png)](https://www.versioneye.com/user/projects/52e26562ec137520dc00003f)

Installation
------------
Available at clojars
(https://clojars.org/com.ullink.rx/rx-cron-scheduler).

1/ Add a clojars repo. If you use gradle following should be enough:
```groovy
repositories {
    mavenRepo url: 'http://clojars.org/repo'
}
```

2/ Add a dependency to your project. Using gradle:
```groovy
compile 'com.ullink.rx:rx-cron-scheduler:1.3'
```

Usage
------------
```java
static class SideEffectTask implements Action0
{
   @Override
   public void call() {
      .. some stuff ...
   }
}
ExecutorService executor = Executors.newSingleThreadScheduledExecutor();
Scheduler inner = Schedulers.executor(executor);
RxCronScheduler service = new RxCronForwardingScheduler(inner);
final SideEffectTask task = new SideEffectTask();
WeeklyCalendar weeklyCalendar = new WeeklyCalendar();
boolean [] excluded = new boolean [] {false, false, true, true,true,true,true, true};
weeklyCalendar.setDaysExcluded(excluded);
service.schedule(task, new CronExpression("0 0 15 ? * *"), weeklyCalendar );
```

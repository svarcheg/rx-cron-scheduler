package com.ullink.rxscheduler.cron;

import static org.junit.Assert.assertEquals;
import java.text.ParseException;
import java.util.GregorianCalendar;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.Assert;
import org.junit.Test;
import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.concurrency.Schedulers;
import rx.concurrency.TestScheduler;
import rx.operators.SafeObservableSubscription;
import rx.util.functions.Action0;
import rx.util.functions.Func1;
import com.ullink.rxscheduler.cron.RxCronForwardingScheduler;
import com.ullink.rxscheduler.cron.RxCronScheduler;
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
	public void testDaily() throws Exception {
		TestScheduler underlying = new TestScheduler();
		RxCronScheduler service = new RxCronForwardingScheduler(underlying);
		SideEffectTask task = new SideEffectTask();
		service.schedule(task, new CronExpression("0 0 15 ? * *"));
		Assert.assertEquals(0, task.counter.intValue());
		underlying.advanceTimeBy(15, TimeUnit.HOURS);
		Assert.assertEquals(1, task.counter.intValue());
		underlying.advanceTimeBy(23, TimeUnit.HOURS);
		Assert.assertEquals(2, task.counter.intValue());
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
	public void testUnSubscribeForRxScheduler() throws ParseException, InterruptedException {
		final AtomicInteger countReceived = new AtomicInteger();
		final AtomicInteger countGenerated = new AtomicInteger();
		final SafeObservableSubscription s = new SafeObservableSubscription();
		final CountDownLatch latch = new CountDownLatch(1);
		final Scheduler underlying = Schedulers.newThread();
		final RxCronForwardingScheduler scheduler = new RxCronForwardingScheduler(underlying);
		s.wrap(Observable.create(OperationCron.cron(new CronExpression("* * * ? * *"), null, scheduler))
				.map(new Func1<Long, Long>() {
					@Override
					public Long call(Long aLong) {
						System.out.println("generated " + aLong);
						countGenerated.incrementAndGet();
						return aLong;
					}
				})
				.subscribeOn(scheduler)
				.observeOn(scheduler)
				.subscribe(new Observer<Long>() {
					@Override
					public void onCompleted() {
						System.out.println("--- completed");
					}

					@Override
					public void onError(Throwable e) {
						System.out.println("--- onError");
					}

					@Override
					public void onNext(Long args) {
						if (countReceived.incrementAndGet() == 2) {
							s.unsubscribe();
							latch.countDown();
						}
						System.out.println("==> Received " + args);
					}
				}));
		latch.await(10000, TimeUnit.MILLISECONDS);
		System.out.println("----------- it thinks it is finished ------------------ ");
		Thread.sleep(5000);
		assertEquals(2, countGenerated.get());
	}
}

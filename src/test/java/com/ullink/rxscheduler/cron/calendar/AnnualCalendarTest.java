/*
 * Copyright 2001-2009 Terracotta, Inc.
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.ullink.rxscheduler.cron.calendar;

import java.util.Calendar;
import junit.framework.TestCase;

/**
 * Unit test for AnnualCalendar serialization backwards compatibility.
 */
public class AnnualCalendarTest extends TestCase
{

    /**
     * Tests if method <code>setDaysExcluded</code> protects the property daysExcluded against nulling.
     * See: QUARTZ-590
     */
    public void testDaysExcluded()
    {
        AnnualCalendar annualCalendar = new AnnualCalendar();

        annualCalendar.setDaysExcluded(null);

        assertNotNull("Annual calendar daysExcluded property should have been set to empty ArrayList, not null.", annualCalendar.getDaysExcluded());
    }

    /**
     * Tests the parameter <code>exclude</code> in a method <code>setDaysExcluded</code> of class <code>org.quartz.impl.calendar.AnnualCalendar</code>
     */
    public void testExclude()
    {
        AnnualCalendar annualCalendar = new AnnualCalendar();
        Calendar day = Calendar.getInstance();

        day.set(Calendar.MONTH, 9);
        day.set(Calendar.DAY_OF_MONTH, 15);
        annualCalendar.setDayExcluded(day, false);

        assertTrue("The day 15 October is not expected to be excluded but it is", !annualCalendar.isDayExcluded(day));

        day.set(Calendar.MONTH, 9);
        day.set(Calendar.DAY_OF_MONTH, 15);
        annualCalendar.setDayExcluded((Calendar) day.clone(), true);

        day.set(Calendar.MONTH, 10);
        day.set(Calendar.DAY_OF_MONTH, 12);
        annualCalendar.setDayExcluded((Calendar) day.clone(), true);

        day.set(Calendar.MONTH, 8);
        day.set(Calendar.DAY_OF_MONTH, 1);
        annualCalendar.setDayExcluded((Calendar) day.clone(), true);

        assertTrue("The day 15 October is expected to be excluded but it is not", annualCalendar.isDayExcluded(day));

        day.set(Calendar.MONTH, 9);
        day.set(Calendar.DAY_OF_MONTH, 15);
        annualCalendar.setDayExcluded((Calendar) day.clone(), false);

        assertTrue("The day 15 October is not expected to be excluded but it is", !annualCalendar.isDayExcluded(day));
    }

    /**
     * QUARTZ-679 Test if the annualCalendar works over years
     */
    public void testDaysExcludedOverTime()
    {
        AnnualCalendar annualCalendar = new AnnualCalendar();
        Calendar day = Calendar.getInstance();

        day.set(Calendar.MONTH, Calendar.JUNE);
        day.set(Calendar.YEAR, 2005);
        day.set(Calendar.DAY_OF_MONTH, 23);

        annualCalendar.setDayExcluded((Calendar) day.clone(), true);

        day.set(Calendar.YEAR, 2008);
        day.set(Calendar.MONTH, Calendar.FEBRUARY);
        day.set(Calendar.DAY_OF_MONTH, 1);
        annualCalendar.setDayExcluded((Calendar) day.clone(), true);

        assertTrue("The day 1 February is expected to be excluded but it is not", annualCalendar.isDayExcluded(day));
    }

    /**
     * Part 2 of the tests of QUARTZ-679
     */
    public void testRemoveInTheFuture()
    {
        AnnualCalendar annualCalendar = new AnnualCalendar();
        Calendar day = Calendar.getInstance();

        day.set(Calendar.MONTH, Calendar.JUNE);
        day.set(Calendar.YEAR, 2005);
        day.set(Calendar.DAY_OF_MONTH, 23);

        annualCalendar.setDayExcluded((Calendar) day.clone(), true);

        // Trying to remove the 23th of June
        day.set(Calendar.MONTH, Calendar.JUNE);
        day.set(Calendar.YEAR, 2008);
        day.set(Calendar.DAY_OF_MONTH, 23);
        annualCalendar.setDayExcluded((Calendar) day.clone(), false);

        assertTrue("The day 23 June is not expected to be excluded but it is", !annualCalendar.isDayExcluded(day));
    }

}

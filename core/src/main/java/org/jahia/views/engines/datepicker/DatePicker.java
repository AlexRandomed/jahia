/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 * 
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.views.engines.datepicker;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.apache.log4j.Logger;

public class DatePicker {
    
    private static final transient Logger logger = Logger
            .getLogger(DatePicker.class);

    private static DatePicker datePicker;
    private Map listeners = new HashMap();

    private DatePicker(){}

    public static synchronized DatePicker getInstance(){
        if ( datePicker == null ){
            datePicker = new DatePicker();
        }
        return datePicker;
    }

    public void addListener(String name, DatePickerEventListener listener){
        if ( name != null && listener != null ){
            listeners.put(name, listener);
        }
    }

    public DatePickerEventListener getListener(String name){
        return (DatePickerEventListener)listeners.get(name);
    }

    public void removeListener(String name){
        listeners.remove(name);
    }

    public void wakeupListener(String methodName, DatePickerEvent ev){
        Iterator iterator = listeners.values().iterator();
        while ( iterator.hasNext() ){
            DatePickerEventListener listener =
                    (DatePickerEventListener)iterator.next();
            try {
                Class lClass = listener.getClass();
                Class eClass = ev.getClass();
                java.lang.reflect.Method method =
                        lClass.getMethod( methodName, new Class[] { eClass } );
                if (method != null) {
                    method.invoke( listener,
                    new Object[] { (org.jahia.views.engines.datepicker.DatePickerEvent)ev } );
                }
            } catch (NoSuchMethodException nsme)  {
                logger.error(nsme.getMessage(), nsme);
            } catch (InvocationTargetException ite)  {
                logger.error(ite.getMessage(), ite);
            } catch (IllegalAccessException iae)  {
                logger.error(iae.getMessage(), iae);
            }
        }
    }

    /**
     * Returns a readable date format for a given long value.
     *
     * @param dateValue
     * @return
     */
    public static String getDate(long date){

        List months = new ArrayList();
        months.add("January");
        months.add("February");
        months.add("March");
        months.add("April");
        months.add("May");
        months.add("June");
        months.add("July");
        months.add("August");
        months.add("September");
        months.add("October");
        months.add("November");
        months.add("December");

        String vData = "DD MONTH YYYY";
        long t1 = date;

        TimeZone tz = TimeZone.getTimeZone("UTC");
        Calendar cal = Calendar.getInstance(tz);
        cal.setTime(new Date(t1));
        int year = cal.get(Calendar.YEAR);
        int mon = cal.get(Calendar.MONTH);
        int mon2 = mon+1;
        int day = cal.get(Calendar.DAY_OF_MONTH);
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int min = cal.get(Calendar.MINUTE);

        String vMonth = new String();
        vMonth = String.valueOf(1 + mon);
        vMonth = (vMonth.length() < 2) ? "0" + vMonth : vMonth;
        String vMon = ((String)months.get(mon)).substring(0,3);
        String vFMon = (String)months.get(mon);
        String vY4 = new String();
        vY4 = String.valueOf(year);
        String vY2 = new String();
        vY2 = vY4.substring(2,4);
        String vDD = new String();
        vDD = String.valueOf(day);
        vDD = (vDD.length() < 2) ? "0" + vDD : vDD;
        String h24 = String.valueOf(hour);
        String h12;
        String vTT;
        if (hour > 12)
        {
            h12 = String.valueOf(hour - 12);
            vTT = "PM";
        }
        else if (hour == 0)
        {
            h12 = String.valueOf(12);
            vTT = "PM";
        }
        else
        {
            h12 = String.valueOf(hour);
            vTT = "AM";
        }
        h24 = (h24.length() < 2) ? "0" + h24 : h24;
        h12 = (h12.length() < 2) ? "0" + h12 : h12;

        String m = String.valueOf(min);
        m = (m.length() < 2) ? "0" + m : m;
        String vT24 = h24 + ":" + m;
        String vT12 = h12 + ":" + m;

        StringBuffer data = new StringBuffer(vData);
        int pos = vData.toUpperCase().indexOf("YYYY");
        if (pos != -1)
        {
            data = data.replace(pos, pos+4, vY4);
            vData = data.toString();
        }
        pos = vData.toUpperCase().indexOf("YY");
        if (pos != -1)
        {
            data = data.replace(pos, pos+2, vY2);
            vData = data.toString();
        }
        pos = vData.toUpperCase().indexOf("MONTH");
        if (pos != -1)
        {
            data = data.replace(pos, pos+5, vFMon);
            vData = data.toString();
        }
        pos = vData.toUpperCase().indexOf("MON");
        if (pos != -1)
        {
            data = data.replace(pos, pos+3, vMon);
            vData = data.toString();
        }
        pos = vData.toUpperCase().indexOf("HH:MM");
        if (pos != -1)
        {
            int pos2 = vData.toUpperCase().indexOf("TT");
            if (pos2 != -1)
            {
                data = data.replace(pos2, pos2+2, vTT);
                data = data.replace(pos, pos+5, vT12);
                vData = data.toString();
            }
            else
            {
                data = data.replace(pos, pos+5, vT24);
                vData = data.toString();
            }

        }

        pos = vData.toUpperCase().indexOf("MM");
        if (pos != -1)
        {
            data = data.replace(pos, pos+2, vMonth);
            vData = data.toString();
        }
        pos = vData.toUpperCase().indexOf("DD");
        if (pos != -1)
        {
            data = data.replace(pos, pos+2, vDD);
            vData = data.toString();
        }
        vData = data.toString();
        return vData;
    }
}
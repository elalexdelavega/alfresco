/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * and Open Source Software ("FLOSS") applications as described in Alfresco's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.module.vti.handler.alfresco;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utils class for alfresco handlers that implement frontpage protocol
 * 
 * @author Dmitry Lazurkin
 */
public class VtiUtils
{
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH);
    private static final SimpleDateFormat propfindDateFormate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH);
    private static final SimpleDateFormat versionDateFormat = new SimpleDateFormat("M/d/yyyy h:mm a", Locale.ENGLISH);
    
    private static Map<String, String> encodingMap = new HashMap<String, String>();

    private static Pattern validNamePattern = Pattern.compile("[^#]+");
    
    static
    {
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        propfindDateFormate.setTimeZone(TimeZone.getTimeZone("GMT"));
        
        encodingMap.put("=", "&#61;");
        encodingMap.put("{", "&#123;");
        encodingMap.put("}", "&#125;");
        encodingMap.put("&", "&#38;");
        encodingMap.put(";", "&#59;");
        encodingMap.put("'", "&#39;");
    }

    /**
     * Convert FrontPageExtension version string to alfresco version label. For FrontPageExtension version string minor number is optional, but for alfresco version label it's
     * required
     * 
     * @param docVersion FrontPageExtension version string
     * @return alfresco version label
     */
    public static String toAlfrescoVersionLabel(String docVersion)
    {
        if (docVersion.indexOf(".") == -1)
        {
            docVersion += ".0"; // add minor number to version label
        }

        return docVersion;
    }

    /**
     * Convert FrontPageExtension lock timeout to Alfresco lock timeout. FrontPageExtension timeout is number of minutes, but Alfresco timeout is number of seconds.
     * 
     * @param timeout FrontPageExtension lock timeout
     * @return Alfresco lock timeout
     */
    public static int toAlfrescoLockTimeout(int timeout)
    {
        return timeout * 60;
    }

    /**
     * Format date
     * 
     * @param date input date
     * @return String formated date
     */
    public static String formatDate(Date date)
    {
        return dateFormat.format(date);
    }

    /**
     * Format version date
     * 
     * @param date input date
     * @return String formated version date
     */
    public static String formatVersionDate(Date date)
    {
        return versionDateFormat.format(date);
    }

    /**
     * Format propfind date
     * 
     * @param date input date
     * @return String formated propfind date
     */
    public static String formatPropfindDate(Date date)
    {
        return propfindDateFormate.format(date);
    }

    /**
     * Compare dates
     * 
     * @param date input date
     * @param dateString input date in string
     * @return <b>true</b> if date and dateString equals, <b>false</b> otherwise
     */
    public static boolean compare(Date date, String dateString)
    {
        return dateString.replaceAll("-0000", "+0000").equals(dateFormat.format(date));
    }
    
    public static boolean hasIllegalCharacter(String value)
    {
        Matcher matcher = validNamePattern.matcher(value);
        return !matcher.matches();
    }
    
    public static String htmlEncode(String value)
    {
        if (value == null)        
        {
            return null;
        }
        
        StringBuilder sb = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i ++)
        {
            char current = value.charAt(i);
            
            String replacement = encodingMap.get("" + current);
            
            if (replacement != null)
            {
                sb.append(replacement);
            }
            else
            {
                sb.append(current);
            }
        }
        return sb.toString();        
    } 
    
}

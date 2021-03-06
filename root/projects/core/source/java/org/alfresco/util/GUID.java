/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.util;

import org.safehaus.uuid.UUIDGenerator;

/**
 * A wrapper class to serve up GUIDs
 *
 * @author kevinr
 */
public final class GUID
{
   /**
    * Private Constructor for GUID.
    */
   private GUID()
   {
   }

//   protected static final char[] s_values = 
//                                            {
//                                               '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e',
//                                               'f'
//                                            };

   /**
    * Generates and returns a new GUID as a string
    *
    * @return String GUID
    */
   public static String generate()
   {
//      return UUIDGenerator.getInstance().generateTimeBasedUUID().toString();
       // Request for random GUIDs: AR-2300
       return UUIDGenerator.getInstance().generateRandomBasedUUID().toString();
   }

// == Not sure if we need this functionality again (derekh) ==
//
//   /**
//    * Convert a string with a guid inside into a byte[16] array
//    * 
//    * @param str - the guid
//    * @return - byte[16] containing the GUID
//    * @throws InvalidGuidFormatException
//    */
//   public static byte[] parseFromString(String str) throws InvalidGuidFormatException
//   {
//      byte[] data    = new byte[16];
//      int    dataPos = 0;
//
//      byte   bVal;
//      int    value   = 0;
//      int    pos     = 0;
//
//      for(int i = 0; i < str.length(); i++)
//      {
//         char thisChar = str.charAt(i);
//
//         int  idx = 0;
//
//         if(thisChar >= '0' && thisChar <= '9')
//         {
//            idx = thisChar - '0';
//            pos++;
//         }
//         else if(thisChar >= 'a' && thisChar <= 'f')
//         {
//            idx = thisChar - 'a' + 10;
//            pos++;
//         }
//         else if(thisChar >= 'a' && thisChar <= 'f')
//         {
//            idx = thisChar - 'A' + 10;
//            pos++;
//         }
//         else if(thisChar == '-' || thisChar == '{' || thisChar == '}')
//         {
//            // Doesn't matter
//         }
//         else
//         {
//            throw new InvalidGuidFormatException();
//         }
//
//         try
//         {
//            if(pos == 1)
//               value = idx;
//            else if(pos == 2)
//            {
//               value = (value * 16) + idx;
//
//               byte b = (byte) value;
//               data[dataPos++] = b;
//
//               pos = 0;
//            }
//         }
//         catch(RuntimeException e)
//         {
//            // May occur if we go off the end of the data index
//            throw new InvalidGuidFormatException();
//         }
//      }
//
//      return data;
//   }
//
//   /**
//    * Convert a byte[16] containing a guid to a string representation
//    * 
//    * @param data - the data 
//    * @return - the string
//    */
//   public static String convertToString(byte[] data)
//   {
//      char[] output = new char[36];
//      int    cPos = 0;
//
//      for(int i = 0; i < 16; i++)
//      {
//         int v = data[i];
//
//         int lowVal = v & 0x000F;
//         int hiVal  = (v & 0x00F0) >> 4;
//
//         output[cPos++] = s_values[hiVal];
//         output[cPos++] = s_values[lowVal];
//
//         if(cPos == 8 || cPos == 13 || cPos == 18 || cPos == 23)
//            output[cPos++] = '-';
//      }
//
//      return new String(output);
//   }
}

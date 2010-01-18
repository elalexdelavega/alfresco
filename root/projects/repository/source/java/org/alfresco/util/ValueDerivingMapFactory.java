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
 * FLOSS exception.  You should have received a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.util;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * A factory for maps that dynamically derive their looked up values from a given source object.
 * 
 * @author dward
 */
public class ValueDerivingMapFactory<O, K, V>
{

    /** A map of value derivers that derive the value of each entry from a given source. */
    private final Map<K, ValueDeriver<O, V>> valueDerivers;

    /**
     * Instantiates a new value deriving map factory.
     * 
     * @param valueDerivers
     *            a map of value derivers that derive the value of each entry from a given source
     */
    public ValueDerivingMapFactory(Map<K, ValueDeriver<O, V>> valueDerivers)
    {
        this.valueDerivers = valueDerivers;
    }

    /**
     * Gets a map that derives its values from the given source.
     * 
     * @param source
     *            the source
     * @return the map
     */
    public Map<K, V> getMap(final O source)
    {
        return new AbstractMap<K, V>()
        {

            @Override
            public V get(Object key)
            {
                ValueDeriver<O, V> valueDeriver = ValueDerivingMapFactory.this.valueDerivers.get(key);
                return valueDeriver == null ? null : valueDeriver.deriveValue(source);
            }

            @Override
            public boolean containsKey(Object key)
            {
                return ValueDerivingMapFactory.this.valueDerivers.containsKey(key);
            }

            @Override
            public Set<K> keySet()
            {
                return ValueDerivingMapFactory.this.valueDerivers.keySet();
            }

            @Override
            public int size()
            {
                return ValueDerivingMapFactory.this.valueDerivers.size();
            }

            @Override
            public Set<Map.Entry<K, V>> entrySet()
            {
                final Set<Map.Entry<K, ValueDeriver<O, V>>> entries = ValueDerivingMapFactory.this.valueDerivers
                        .entrySet();
                return new AbstractSet<Entry<K, V>>()
                {

                    @Override
                    public Iterator<Map.Entry<K, V>> iterator()
                    {
                        final Iterator<Map.Entry<K, ValueDeriver<O, V>>> i = entries.iterator();
                        return new Iterator<Map.Entry<K, V>>()
                        {

                            public boolean hasNext()
                            {
                                return i.hasNext();
                            }

                            public Map.Entry<K, V> next()
                            {
                                final Map.Entry<K, ValueDeriver<O, V>> next = i.next();
                                return new Map.Entry<K, V>()
                                {

                                    public K getKey()
                                    {
                                        return next.getKey();
                                    }

                                    public V getValue()
                                    {
                                        return get(next.getKey());
                                    }

                                    public V setValue(V value)
                                    {
                                        throw new UnsupportedOperationException();
                                    }
                                };
                            }

                            public void remove()
                            {
                                throw new UnsupportedOperationException();
                            }
                        };
                    }

                    @Override
                    public int size()
                    {
                        return entries.size();
                    }
                };
            }
        };
    }

    /**
     * An interface for objects that derive the value for a specific entry in the map.
     */
    public interface ValueDeriver<O, V>
    {

        /**
         * Derives a value from the given source.
         * 
         * @param source
         *            the source
         * @return the derived value
         */
        public V deriveValue(O source);
    }
}
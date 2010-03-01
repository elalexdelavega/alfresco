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

package org.alfresco.repo.attributes;

import java.io.Serializable;

/**
 * Value based implementation of a double attribute.
 * @author britt
 */
public class DoubleAttributeValue extends AttributeValue implements
        DoubleAttribute
{
    private static final long serialVersionUID = 1710813761810342910L;

    private double fData;
    
    public DoubleAttributeValue(double value)
    {
        fData = value;
    }
    
    public DoubleAttributeValue(DoubleAttribute attr)
    {
        super(attr.getAcl());
        fData = attr.getDoubleValue();
    }
    
    public Type getType()
    {
        return Type.DOUBLE;
    }

    public Serializable getRawValue()
    {
        return Double.valueOf(fData);
    }

    @Override
    public double getDoubleValue()
    {
        return fData;
    }

    @Override
    public void setDoubleValue(double value)
    {
        fData = value;
    }

    @Override
    public String toString()
    {
        return Double.toString(fData);
    }
}

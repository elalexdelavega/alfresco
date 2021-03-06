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
package org.alfresco.repo.audit.model;

import org.alfresco.repo.audit.AuditModel;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Attribute;
import org.dom4j.Element;

public abstract class AbstractNamedAuditEntry extends AbstractAuditEntry
{
    private static Log s_logger = LogFactory.getLog(AbstractNamedAuditEntry.class);

    private String name;

    public AbstractNamedAuditEntry()
    {
        super();
    }

    @Override
    void configure(AbstractAuditEntry parent, Element element, NamespacePrefixResolver namespacePrefixResolver)
    {
        Attribute nameAttribute = element.attribute(AuditModel.AT_NAME);
        if (nameAttribute != null)
        {
            name = nameAttribute.getStringValue();
        }
        else
        {
            throw new AuditModelException("The name attribute is mandatory");
        }
        if(s_logger.isDebugEnabled())
        {
            s_logger.debug("Name = "+name);
        }
        
        super.configure(parent, element, namespacePrefixResolver);

    }

    /* package */String getName()
    {
        return name;
    }

}

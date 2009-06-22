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
package org.alfresco.repo.domain.encoding.ibatis;

import org.alfresco.repo.domain.encoding.AbstractEncodingDAOImpl;
import org.alfresco.repo.domain.encoding.EncodingEntity;
import org.alfresco.repo.domain.mimetype.MimetypeEntity;
import org.springframework.orm.ibatis.SqlMapClientTemplate;

/**
 * iBatis-specific implementation of the Mimetype DAO.
 * 
 * @author Derek Hulley
 * @since 3.2
 */
public class EncodingDAOImpl extends AbstractEncodingDAOImpl
{
    private static final String SELECT_ENCODING_BY_ID = "select.EncodingById";
    private static final String SELECT_ENCODING_BY_KEY = "select.EncodingByKey";
    private static final String INSERT_ENCODING = "insert.Encoding";
    
    private SqlMapClientTemplate template;

    public void setSqlMapClientTemplate(SqlMapClientTemplate sqlMapClientTemplate)
    {
        this.template = sqlMapClientTemplate;
    }

    @Override
    protected EncodingEntity getEncodingEntity(Long id)
    {
        EncodingEntity encodingEntity = new EncodingEntity();
        encodingEntity.setId(id);
        encodingEntity = (EncodingEntity) template.queryForObject(SELECT_ENCODING_BY_ID, encodingEntity);
        // Done
        return encodingEntity;
    }

    @Override
    protected EncodingEntity getEncodingEntity(String encoding)
    {
        EncodingEntity encodingEntity = new EncodingEntity();
        encodingEntity.setEncoding(encoding);
        encodingEntity = (EncodingEntity) template.queryForObject(SELECT_ENCODING_BY_KEY, encodingEntity);
        // Could be null
        return encodingEntity;
    }

    @Override
    protected EncodingEntity createEncodingEntity(String encoding)
    {
        EncodingEntity encodingEntity = new EncodingEntity();
        encodingEntity.setVersion(MimetypeEntity.CONST_LONG_ZERO);
        encodingEntity.setEncoding(encoding);
        Long id = (Long) template.insert(INSERT_ENCODING, encodingEntity);
        encodingEntity.setId(id);
        // Done
        return encodingEntity;
    }
}
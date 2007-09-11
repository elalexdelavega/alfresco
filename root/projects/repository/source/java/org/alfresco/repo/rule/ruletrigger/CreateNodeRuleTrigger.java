/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
package org.alfresco.repo.rule.ruletrigger;

import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * We use this specialised trigger for create node beaucse of a problem with the CIFS integration.
 * <p>
 * The create node trigger will only be fired if the object is NOT a sub-type of content.
 * <p>
 * Policy names supported are:
 * <ul>
 *   <li>{@linkplain NodeServicePolicies.OnCreateChildAssociationPolicy}</li>
 *   <li>{@linkplain NodeServicePolicies.BeforeDeleteChildAssociationPolicy}</li>
 *   <li>{@linkplain NodeServicePolicies.OnCreateNodePolicy}</li>
 * </ul>
 * 
 * @author Roy Wetherall
 */
public class CreateNodeRuleTrigger extends RuleTriggerAbstractBase
        implements NodeServicePolicies.OnCreateNodePolicy
{
    /**
     * The logger
     */
    private static Log logger = LogFactory.getLog(CreateNodeRuleTrigger.class);
    
    private static final String POLICY = "onCreateNode";
    
    private boolean isClassBehaviour = false;
	
	public void setIsClassBehaviour(boolean isClassBehaviour)
	{
		this.isClassBehaviour = isClassBehaviour;
	}
    
    DictionaryService dictionaryService;
    
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    /**
	 * @see org.alfresco.repo.rule.ruletrigger.RuleTrigger#registerRuleTrigger()
	 */
	public void registerRuleTrigger()
	{
		if (isClassBehaviour == true)
		{
			this.policyComponent.bindClassBehaviour(
					QName.createQName(NamespaceService.ALFRESCO_URI, POLICY), 
					this, 
					new JavaBehaviour(this, POLICY));
		}
		else
		{
			this.policyComponent.bindAssociationBehaviour(
					QName.createQName(NamespaceService.ALFRESCO_URI, POLICY), 
					this, 
					new JavaBehaviour(this, POLICY));
		}
	}
    
    /**
     * {@inheritDoc}
     */
    public void onCreateNode(ChildAssociationRef childAssocRef)
    {
        // Only fire the rule if the node is question has no potential to contain content
        // TODO we need to find a better way to do this .. how can this be resolved in CIFS??
        boolean triggerRule = false;
        QName type = this.nodeService.getType(childAssocRef.getChildRef());
        ClassDefinition classDefinition = this.dictionaryService.getClass(type);
        if (classDefinition != null)
        {
            for (PropertyDefinition propertyDefinition : classDefinition.getProperties().values())
            {
                if (propertyDefinition.getDataType().getName().equals(DataTypeDefinition.CONTENT) == true)
                {
                    triggerRule = true;
                    break;
                }
            }
        }
        
        if (triggerRule == false)
        {
            if (logger.isDebugEnabled() == true)
            {
                logger.debug(
                        "Create node rule trigger fired for parent node " + 
                        this.nodeService.getType(childAssocRef.getParentRef()).toString() + " " + childAssocRef.getParentRef() + 
                        " and child node " +
                        this.nodeService.getType(childAssocRef.getChildRef()).toString() + " " + childAssocRef.getChildRef());
            }
            
            triggerRules(childAssocRef.getParentRef(), childAssocRef.getChildRef());
        }
        
        // Reguadless of whether the rule is triggered, mark this transaction as having created this node
        AlfrescoTransactionSupport.bindResource(childAssocRef.getChildRef().toString(), childAssocRef.getChildRef().toString());         
    }
}

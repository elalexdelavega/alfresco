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

import java.util.List;

import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.rule.RuleServiceException;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

public class SingleNodeRefPolicyRuleTrigger extends RuleTriggerAbstractBase
{
    private static final String ERR_POLICY_NAME_NOT_SET = "Unable to register rule trigger since policy name has not been set.";
    
    private String policyNamespace = NamespaceService.ALFRESCO_URI;
    
    private String policyName;
    
    private boolean triggerParentRules = true;
    
    public void setPolicyNamespace(String policyNamespace)
    {
        this.policyNamespace = policyNamespace;
    }
    
    public void setPolicyName(String policyName)
    {
        this.policyName = policyName;
    }
    
    public void setTriggerParentRules(boolean triggerParentRules)
    {
        this.triggerParentRules = triggerParentRules;
    }
    
    public void registerRuleTrigger()
    {
        if (policyName == null)
        {
            throw new RuleServiceException(ERR_POLICY_NAME_NOT_SET);
        }
        
        this.policyComponent.bindClassBehaviour(
                QName.createQName(this.policyNamespace, this.policyName), 
                this, 
                new JavaBehaviour(this, "policyBehaviour"));        
    }

    public void policyBehaviour(NodeRef nodeRef)
    {
        if (triggerParentRules == true)
        {
            List<ChildAssociationRef> parentsAssocRefs = this.nodeService.getParentAssocs(nodeRef);
            for (ChildAssociationRef parentAssocRef : parentsAssocRefs)
            {
                triggerRules(parentAssocRef.getParentRef(), nodeRef);
            }
        }
        else
        {
            triggerRules(nodeRef, nodeRef);
        }
    }
}

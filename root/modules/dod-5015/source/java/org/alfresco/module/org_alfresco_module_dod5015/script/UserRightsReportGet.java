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
 * http://www.alfresco.com/legal/licensing
 */
package org.alfresco.module.org_alfresco_module_dod5015.script;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_dod5015.RecordsManagementService;
import org.alfresco.module.org_alfresco_module_dod5015.security.RecordsManagementSecurityService;
import org.alfresco.module.org_alfresco_module_dod5015.security.Role;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.web.scripts.Cache;
import org.alfresco.web.scripts.DeclarativeWebScript;
import org.alfresco.web.scripts.Status;
import org.alfresco.web.scripts.WebScriptRequest;

/**
 * Implementation for Java backed webscript to return user rights report.
 * 
 * @author Gavin Cornwell
 */
public class UserRightsReportGet extends DeclarativeWebScript
{
    protected AuthorityService authorityService;
    protected PersonService personService;
    protected NodeService nodeService;
    protected RecordsManagementService rmService;
    protected RecordsManagementSecurityService rmSecurityService;
    
    /**
     * Sets the AuthorityService instance
     * 
     * @param authorityService AuthorityService instance 
     */
    public void setAuthorityService(AuthorityService authorityService)
    {
        this.authorityService = authorityService;
    }
    
    /**
     * Sets the PersonService instance
     * 
     * @param personService PersonService instance 
     */
    public void setPersonService(PersonService personService)
    {
        this.personService = personService;
    }
    
    /**
     * Sets the NodeService instance
     * 
     * @param nodeService NodeService instance 
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService; 
    }
    
    /**
     * Sets the RecordsManagementService instance
     * 
     * @param rmService The RecordsManagementService instance
     */
    public void setRecordsManagementService(RecordsManagementService rmService)
    {
        this.rmService = rmService;
    }
    
    /**
     * Sets the RecordsManagementSecurityService instance
     * 
     * @param rmSecurityService The RecordsManagementSecurityService instance
     */
    public void setRecordsManagementSecurityService(RecordsManagementSecurityService rmSecurityService)
    {
        this.rmSecurityService = rmSecurityService;
    }
    
    /*
     * @see org.alfresco.web.scripts.DeclarativeWebScript#executeImpl(org.alfresco.web.scripts.WebScriptRequest, org.alfresco.web.scripts.Status, org.alfresco.web.scripts.Cache)
     */
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        // get the RM root nodes in the system
        List<NodeRef> rmRoots = this.rmService.getRecordsManagementRoots();
        
        if (rmRoots == null || rmRoots.size() == 0)
        {
            status.setCode(HttpServletResponse.SC_BAD_REQUEST, 
                        "There are no Records Management root nodes in the system");
            return null;
        }
        
        // construct all the maps etc. needed to build the model 
        Map<String, UserModel> usersMap = new HashMap<String, UserModel>(8);
        Map<String, RoleModel> rolesMap = new HashMap<String, RoleModel>(8);
        Map<String, GroupModel> groupsMap = new HashMap<String, GroupModel>(8);
        
        // TODO: deal with presence of more than one root, for now we know it's only 1
        NodeRef rmRootNode = rmRoots.get(0);
        
        // iterate over all the roles for the file plan and construct models
        Set<Role> roles = this.rmSecurityService.getRoles(rmRootNode);
        for (Role role : roles)
        {
            // get or create the RoleModel object for current role
            String roleName = role.getName();
            RoleModel roleModel = rolesMap.get(roleName);
            if (roleModel == null)
            {
                roleModel = new RoleModel(role);
                rolesMap.put(roleName, roleModel);
            }
            
            // get the users for the current RM role
            String group = role.getRoleGroupName();
            Set<String> users = this.authorityService.getContainedAuthorities(AuthorityType.USER, group, true);
            roleModel.setUsers(users);
            
            // setup a user model object for each user
            for (String userName : users)
            {
                UserModel userModel = usersMap.get(userName);
                if (userModel == null)
                {
                    NodeRef userRef = this.personService.getPerson(userName);
                    userModel = new UserModel(userName, 
                                (String)this.nodeService.getProperty(userRef, ContentModel.PROP_FIRSTNAME), 
                                (String)this.nodeService.getProperty(userRef, ContentModel.PROP_LASTNAME));
                    usersMap.put(userName, userModel);
                }
                
                userModel.addRole(roleName);
            }
            
            // get the groups for the cuurent RM role
            Set<String> groups = this.authorityService.getContainedAuthorities(AuthorityType.GROUP, group, false);
            roleModel.setGroups(groups);
            
            // setup a user model object for each user in each group
            for (String groupName : groups)
            {
                GroupModel groupModel = groupsMap.get(groupName);
                if (groupModel == null)
                {
                    groupModel = new GroupModel(groupName, 
                                this.authorityService.getAuthorityDisplayName(groupName));
                    groupsMap.put(groupName, groupModel);
                }
                
                // get users in each group
                Set<String> groupUsers = this.authorityService.getContainedAuthorities(AuthorityType.USER, groupName, true);
                for (String userName : groupUsers)
                {
                    UserModel userModel = usersMap.get(userName);
                    if (userModel == null)
                    {
                        NodeRef userRef = this.personService.getPerson(userName);
                        userModel = new UserModel(userName, 
                                    (String)this.nodeService.getProperty(userRef, ContentModel.PROP_FIRSTNAME), 
                                    (String)this.nodeService.getProperty(userRef, ContentModel.PROP_LASTNAME));
                        usersMap.put(userName, userModel);
                    }
                    
                    userModel.addGroup(groupName);
                    userModel.addRole(roleName);
                    groupModel.addUser(userName);
                }
            }
        }
        
        // add all the lists data to a Map
        Map<String, Object> reportModel = new HashMap<String, Object>(4);
        reportModel.put("users", usersMap);
        reportModel.put("roles", rolesMap);
        reportModel.put("groups", groupsMap);
        
        // create model object with the lists model
        Map<String, Object> model = new HashMap<String, Object>(1);
        model.put("report", reportModel);
        return model;
    }
    
    /**
     * Class to represent a role for use in a Freemarker template.
     *
     * @author Gavin Cornwell
     */
    public class RoleModel extends Role
    {
        private Set<String> users = new HashSet<String>(8);
        private Set<String> groups = new HashSet<String>(8);
        
        public RoleModel(Role role)
        {
            super(role.getName(), role.getDisplayLabel(), role.getCapabilities(), role.getRoleGroupName());
        }
        
        public void addUser(String username)
        {
            this.users.add(username);
        }
        
        public void addGroup(String groupName)
        {
            this.groups.add(groupName);
        }
        
        public void setUsers(Set<String> users)
        {
            this.users = users;
        }
        
        public void setGroups(Set<String> groups)
        {
            this.groups = groups;
        }
        
        public Set<String> getUsers()
        {
            return this.users;
        }
        
        public Set<String> getGroups()
        {
            return this.groups;
        }
    }
    
    /**
     * Class to represent a user for use in a Freemarker template.
     *
     * @author Gavin Cornwell
     */
    public class UserModel
    {
        private String userName;
        private String firstName;
        private String lastName;
        private Set<String> roles;
        private Set<String> groups;
        
        public UserModel(String userName, String firstName, String lastName)
        {
            this.userName = userName;
            this.firstName = firstName;
            this.lastName = lastName;
            this.roles = new HashSet<String>(2);
            this.groups = new HashSet<String>(2);
        }

        public String getUserName()
        {
            return this.userName;
        }

        public String getFirstName()
        {
            return this.firstName;
        }

        public String getLastName()
        {
            return this.lastName;
        }

        public Set<String> getRoles()
        {
            return this.roles;
        }
        
        public Set<String> getGroups()
        {
            return this.groups;
        }
        
        public void addRole(String roleName)
        {
            this.roles.add(roleName);
        }
        
        public void addGroup(String groupName)
        {
            this.groups.add(groupName);
        }
    }
    
    /**
     * Class to represent a group for use in a Freemarker template.
     *
     * @author Gavin Cornwell
     */
    public class GroupModel
    {
        private String name;
        private String label;
        private Set<String> users;
        
        public GroupModel(String name, String label)
        {
            this.name = name;
            this.label = label;
            this.users = new HashSet<String>(4);
        }

        public String getName()
        {
            return this.name;
        }

        public String getDisplayLabel()
        {
            return this.label;
        }

        public Set<String> getUsers()
        {
            return this.users;
        }
        
        public void addUser(String userName)
        {
            this.users.add(userName);
        }
    }
}
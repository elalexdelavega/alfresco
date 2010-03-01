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
package org.alfresco.service.cmr.security;

import java.util.Map;
import java.util.Set;

import org.alfresco.service.Auditable;
import org.alfresco.service.PublicService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;

/**
 * The public API for a permission service The implementation may be changed in the application configuration
 *
 * @author Andy Hind
 */
@PublicService
public interface PermissionService
{
    /**
     * Prefixes used for authorities of type role. This is intended for external roles, e.g. those set by ACEGI
     * implementations It is only used for admin at the moment - which is done outside the usual permission assignments
     * at the moment. It could be a dynamic authority.
     */
    public static final String ROLE_PREFIX = "ROLE_";

    /**
     * Prefix used for authorities of type group.
     */
    public static final String GROUP_PREFIX = "GROUP_";

    /**
     * The group that contains everyone except guest.
     */
    public static final String ALL_AUTHORITIES = "GROUP_EVERYONE";

    /**
     * The dynamic authority used for ownership
     */
    public static final String OWNER_AUTHORITY = "ROLE_OWNER";

    /**
     * The authority that all owners of WCM stores have.
     */
    public static final String WCM_STORE_OWNER_AUTHORITY = "ROLE_WCM_STORE_OWNER";
    
    /**
     * The dynamic authority used for the ownership of locks.
     */
    public static final String LOCK_OWNER_AUTHORITY = "ROLE_LOCK_OWNER";

    /**
     * The admin authority - currently a role.
     */
    public static final String ADMINISTRATOR_AUTHORITY = "ROLE_ADMINISTRATOR";

    /**
     * The guest authority
     */
    public static final String GUEST_AUTHORITY = "ROLE_GUEST";

    /**
     * The permission for all - not defined in the model. Repsected in the code.
     */
    public static final String ALL_PERMISSIONS = "All";

    // Constants for permissions/permission groups defined in the standard permission model.

    public static final String FULL_CONTROL = "FullControl";

    public static final String READ = "Read";

    public static final String WRITE = "Write";

    public static final String DELETE = "Delete";

    public static final String ADD_CHILDREN = "AddChildren";

    public static final String READ_PROPERTIES = "ReadProperties";

    public static final String READ_CHILDREN = "ReadChildren";

    public static final String WRITE_PROPERTIES = "WriteProperties";

    public static final String DELETE_NODE = "DeleteNode";

    public static final String DELETE_CHILDREN = "DeleteChildren";

    public static final String CREATE_CHILDREN = "CreateChildren";

    public static final String LINK_CHILDREN = "LinkChildren";

    public static final String DELETE_ASSOCIATIONS = "DeleteAssociations";

    public static final String READ_ASSOCIATIONS = "ReadAssociations";

    public static final String CREATE_ASSOCIATIONS = "CreateAssociations";

    public static final String READ_PERMISSIONS = "ReadPermissions";

    public static final String CHANGE_PERMISSIONS = "ChangePermissions";

    public static final String EXECUTE = "Execute";

    public static final String READ_CONTENT = "ReadContent";

    public static final String WRITE_CONTENT = "WriteContent";

    public static final String EXECUTE_CONTENT = "ExecuteContent";

    public static final String TAKE_OWNERSHIP = "TakeOwnership";

    public static final String SET_OWNER = "SetOwner";

    public static final String COORDINATOR = "Coordinator";

    public static final String CONTRIBUTOR = "Contributor";

    public static final String EDITOR = "Editor";

    public static final String CONSUMER = "Consumer";

    public static final String LOCK = "Lock";

    public static final String UNLOCK = "Unlock";

    public static final String CHECK_OUT = "CheckOut";

    public static final String CHECK_IN = "CheckIn";

    public static final String CANCEL_CHECK_OUT = "CancelCheckOut";

    public static final String ASPECTS = "Aspects";

    public static final String PROPERTIES = "Properties";

    public static final String WCM_CONTENT_MANAGER = "ContentManager";

    public static final String WCM_CONTENT_PUBLISHER = "ContentPublisher";

    public static final String WCM_CONTENT_CONTRIBUTOR = "ContentContributor";

    public static final String WCM_CONTENT_REVIEWER = "ContentReviewer";

    public static final String FLATTEN = "Flatten";

    /**
     * Get the Owner Authority
     *
     * @return the owner authority
     */
    @Auditable
    public String getOwnerAuthority();

    /**
     * Get the All Authorities
     *
     * @return the All authorities
     */
    @Auditable
    public String getAllAuthorities();

    /**
     * Get the All Permission
     *
     * @return the All permission
     */
    @Auditable
    public String getAllPermission();

    /**
     * Get all the AccessPermissions that are granted/denied to the current authentication for the given node
     *
     * @param nodeRef -
     *            the reference to the node
     * @return the set of allowed permissions
     */
    @Auditable(key = Auditable.Key.ARG_0, parameters = { "nodeRef" })
    public Set<AccessPermission> getPermissions(NodeRef nodeRef);

    /**
     * Get all the AccessPermissions that are set for anyone for the given node
     *
     * @param nodeRef -
     *            the reference to the node
     * @return the set of allowed permissions
     */
    @Auditable(key = Auditable.Key.ARG_0, parameters = { "nodeRef" })
    public Set<AccessPermission> getAllSetPermissions(NodeRef nodeRef);

    /**
     * Get the permissions that can be set for a given node
     *
     * @param nodeRef
     * @return
     */
    @Auditable(key = Auditable.Key.ARG_0, parameters = { "nodeRef" })
    public Set<String> getSettablePermissions(NodeRef nodeRef);

    /**
     * Get the permissions that can be set for a given type
     *
     * @param type
     * @return - set of permissions
     */
    @Auditable(parameters = { "type" })
    public Set<String> getSettablePermissions(QName type);

    /**
     * Check that the given authentication has a particular permission for the given node. (The default behaviour is to
     * inherit permissions)
     *
     * @param nodeRef
     * @param permission
     * @return - access status
     */
    @Auditable(key = Auditable.Key.ARG_0, parameters = { "nodeRef", "permission" })
    public AccessStatus hasPermission(NodeRef nodeRef, String permission);

    /**
     * Check if a permission is allowed on an acl.
     * @param aclID
     * @param context
     * @param permission
     * @return the access status
     */
    @Auditable(parameters = { "aclID", "context", "permission" })
    public AccessStatus hasPermission(Long aclID, PermissionContext context, String permission);

    /**
     * Delete all the permission assigned to the node
     *
     * @param nodeRef
     */
    @Auditable(key = Auditable.Key.ARG_0, parameters = { "nodeRef" })
    public void deletePermissions(NodeRef nodeRef);

    /**
     * Delete all permission for the given authority.
     *
     * @param nodeRef
     * @param authority
     *            (if null then this will match all authorities)
     */
    @Auditable(key = Auditable.Key.ARG_0, parameters = { "nodeRef", "authority" })
    public void clearPermission(NodeRef nodeRef, String authority);

    /**
     * Find and delete a access control entry by node, authentication and permission. It is possible to delete
     * <ol>
     * <li> a specific permission;
     * <li> all permissions for an authority (if the permission is null);
     * <li> entries for all authorities that have a specific permission (if the authority is null); and
     * <li> all permissions set for the node (if both the permission and authority are null).
     * </ol>
     *
     * @param nodeRef
     *            the node that the entry applies to
     * @param authority
     *            the authority recipient (if null then this will match all authorities)
     * @param permission
     *            the entry permission (if null then this will match all permissions)
     */
    @Auditable(key = Auditable.Key.ARG_0, parameters = { "nodeRef", "authority", "permission" })
    public void deletePermission(NodeRef nodeRef, String authority, String permission);

    /**
     * Set a specific permission on a node.
     *
     * @param nodeRef
     * @param authority
     * @param permission
     * @param allow
     */
    @Auditable(key = Auditable.Key.ARG_0, parameters = { "nodeRef", "authority", "permission", "allow" })
    public void setPermission(NodeRef nodeRef, String authority, String permission, boolean allow);

    /**
     * Set the global inheritance behaviour for permissions on a node.
     *
     * @param nodeRef
     * @param inheritParentPermissions
     */
    @Auditable(key = Auditable.Key.ARG_0, parameters = { "nodeRef", "inheritParentPermissions" })
    public void setInheritParentPermissions(NodeRef nodeRef, boolean inheritParentPermissions);

    /**
     * Return the global inheritance behaviour for permissions on a node.
     *
     * @param nodeRef
     * @return inheritParentPermissions
     */
    @Auditable(key = Auditable.Key.ARG_0, parameters = { "nodeRef" })
    public boolean getInheritParentPermissions(NodeRef nodeRef);

    /**
     * Get all permissions set for the current user.
     *
     * @return - A map of noderefs to permissions set
     * @deprecated
     */
    @Auditable
    public Map<NodeRef, Set<AccessPermission>> getAllSetPermissionsForCurrentUser();

    /**
     * Get all the permissions set for the given authority
     *
     * @param authority
     * @return - A map of noderefs to permissions set
     * @deprecated
     */
    @Auditable(parameters = { "authority" })
    public Map<NodeRef, Set<AccessPermission>> getAllSetPermissionsForAuthority(String authority);

    /**
     * Find all the nodes where the current user has explicitly been assigned the specified permission.
     *
     * @param permission -
     *            the permission to find
     * @param allow
     *            -search for allow (true) or deny
     * @param includeContainingAuthorities -
     *            include permissions for authorities that contain the current user in the list
     * @param includeContainingPermissions -
     *            true; do an exact match: false; search for any permission that woudl imply the one given
     * @return - the set of nodes where the user is assigned the permission
     * @deprecated
     */
    @Auditable(parameters = { "permission", "allow", "includeContainingAuthorities", "includeContainingPermissions" })
    public Set<NodeRef> findNodesByAssignedPermissionForCurrentUser(String permission, boolean allow, boolean includeContainingAuthorities,
            boolean includeContainingPermissions);

    /**
     * Find all the nodes where the current user has explicitly been assigned the specified permission.
     *
     * @param permission -
     *            the permission to find
     * @param allow
     *            -search for allow (true) or deny
     * @param includeContainingAuthorities -
     *            include permissions for authorities that contain the current user in the list
     * @param exactPermissionMatch -
     *            true; do an exact match: false; search for any permission that woudl imply the one given
     * @return - the set of nodes where the user is assigned the permission
     * @deprecated
     */
    @Auditable(parameters = { "authority", "permission", "allow", "includeContainingAuthorities",
            "exactPermissionMatch" })
    public Set<NodeRef> findNodesByAssignedPermission(String authority, String permission, boolean allow,
            boolean includeContainingAuthorities, boolean exactPermissionMatch);
    
    
    /**
     * Add a permission mask to a store
     * 
     * @param storeRef
     * @param authority
     * @param permission
     * @param allow
     */
    @Auditable(key = Auditable.Key.ARG_0, parameters = { "storeRef", "authority", "permission", "allow" })
    public void setPermission(StoreRef storeRef, String authority, String permission, boolean allow);
    
    /**
     * Remove part of a permission mask on a store
     * @param storeRef
     * @param authority
     * @param permission
     */
    @Auditable(key = Auditable.Key.ARG_0, parameters = { "storeRef", "authority", "permission" })
    public void deletePermission(StoreRef storeRef, String authority, String permission);
    
    /**
     * Clear all permission masks for an authority on a store 
     * 
     * @param storeRef
     * @param authority
     */
    @Auditable(key = Auditable.Key.ARG_0, parameters = { "storeRef", "authority" })
    public void clearPermission(StoreRef storeRef, String authority);
    
    /**
     * Remove all permission mask on a store
     * 
     * @param storeRef
     */
    @Auditable(key = Auditable.Key.ARG_0, parameters = { "storeRef" })
    public void deletePermissions(StoreRef storeRef);
    
    
    /**
     * Get all the AccessPermissions that are set for anyone for the given node
     *
     * @param storeRef -
     *            the reference to the store
     * @return the set of allowed permissions
     */
    @Auditable(key = Auditable.Key.ARG_0, parameters = { "storeRef" })
    public Set<AccessPermission> getAllSetPermissions(StoreRef storeRef);
}
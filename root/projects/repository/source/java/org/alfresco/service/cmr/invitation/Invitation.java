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
package org.alfresco.service.cmr.invitation;


/**
 * The invitation request is a command object for who, needs to be added or removed 
 * from which resource with which attributes.
 * 
 * Invitations are processed by the InvitationService
 * 
 * @see org.alfresco.service.cmr.invitation.InvitationService
 *
 * @author mrogers
 */
public interface Invitation 
{
	/**
	 * What sort of Resource   Web Project, Web Site, Node 
	 * (Just Web site for now) 
	 */
	enum ResourceType 
	{
		WEB_SITE
	}
	
	/**
	 * What type of invitation are we? 
	 * (Just Web site for now) 
	 */
	enum InvitationType 
	{
		NOMINATED,
		MODERATED
	}
	
	/**
	 * What sort of resource is it, for example a WEB_SITE?
	 * @return the resource type
	 */
	public ResourceType getResourceType();
	
	/**
	 * What is the resource name ?
	 * @return the name of the resource
	 */
	public String getResourceName();
	
	/**
	 * What is the unique reference for this invitation ?
	 * @return the unique reference for this invitation
	 */
	public String getInviteId();
	
	/**
	 * What sort of invitation is this ?
	 */
	public InvitationType getInvitationType();
	
	
}
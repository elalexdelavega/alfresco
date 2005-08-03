/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version
 * 2.1 of the License, or (at your option) any later version.
 * You may obtain a copy of the License at
 *
 *     http://www.gnu.org/licenses/lgpl.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 *
 * Created on 02-Aug-2005
 */
package org.alfresco.repo.security.permissions.impl.hibernate;

import java.util.HashSet;
import java.util.Set;

import org.alfresco.repo.domain.NodeKey;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;

public class NodePermissionEntryImpl implements NodePermissionEntry
{
    private NodeKey nodeKey;

    private boolean inherits;

    private Set<PermissionEntry> permissionEntries = new HashSet<PermissionEntry>();

    public NodePermissionEntryImpl()
    {
        super();
    }

    public NodeKey getNodeKey()
    {
        return nodeKey;
    }

    public void setNodeKey(NodeKey nodeKey)
    {
        this.nodeKey = nodeKey;
    }

    public NodeRef getNodeRef()
    {
        return new NodeRef(new StoreRef(nodeKey.getProtocol(), nodeKey
                .getIdentifier()), nodeKey.getGuid());
    }

    public boolean getInherits()
    {
        return inherits;
    }

    public void setInherits(boolean inherits)
    {
        this.inherits = inherits;
    }

    public Set<PermissionEntry> getPermissionEntries()
    {
        return permissionEntries;
    }
    
    /* package */ void setPermissionEntries(Set<PermissionEntry> permissionEntries)
    {
        this.permissionEntries = permissionEntries;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof NodePermissionEntryImpl))
        {
            return false;
        }
        NodePermissionEntryImpl other = (NodePermissionEntryImpl) o;

        return this.nodeKey.equals(other.nodeKey)
                && (this.inherits == other.inherits)
                && (this.permissionEntries.equals(other.permissionEntries));
    }

    @Override
    public int hashCode()
    {
        return nodeKey.hashCode();
    }

}

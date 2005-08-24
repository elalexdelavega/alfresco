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
 */
package org.alfresco.repo.webservice.repository;

import java.util.List;

import javax.xml.rpc.handler.MessageContext;

import org.alfresco.repo.webservice.Utils;
import org.alfresco.repo.webservice.types.Reference;
import org.alfresco.repo.webservice.types.ResultSetRow;
import org.alfresco.repo.webservice.types.ResultSetRowNode;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Implementation of a QuerySession that stores the results from a query for parents
 * 
 * @author gavinc
 */
public class ParentsQuerySession extends AbstractQuerySession
{
   private transient static Log logger = LogFactory.getLog(ParentsQuerySession.class);
   
   private Reference node;
   
   /**
    * Constructs a ParentsQuerySession
    * 
    * @param batchSize The batch size to use for this session
    * @param node The node to retrieve the parents
    */
   public ParentsQuerySession(int batchSize, Reference node)
   {
      super(batchSize);
      
      this.node = node;
   }
   
   /**
    * @see org.alfresco.repo.webservice.repository.QuerySession#getNextResultsBatch(org.alfresco.service.cmr.search.SearchService, org.alfresco.service.cmr.repository.NodeService)
    */
   public QueryResult getNextResultsBatch(SearchService searchService, NodeService nodeService)
   {
      QueryResult queryResult = null;
      
      if (this.position != -1)
      {
         if (logger.isDebugEnabled())
            logger.debug("Before getNextResultsBatch: " + toString());
         
         // create the node ref and get the children from the repository
         NodeRef nodeRef = Utils.convertToNodeRef(this.node);
         List<ChildAssociationRef> parents = nodeService.getParentAssocs(nodeRef);
         
         int totalRows = parents.size();
         int lastRow = calculateLastRowIndex(totalRows);
         int currentBatchSize = lastRow - this.position;
         
         if (logger.isDebugEnabled())
            logger.debug("Total rows = " + totalRows + ", current batch size = " + currentBatchSize);
         
         org.alfresco.repo.webservice.types.ResultSet batchResults = new org.alfresco.repo.webservice.types.ResultSet();      
         org.alfresco.repo.webservice.types.ResultSetRow[] rows = new org.alfresco.repo.webservice.types.ResultSetRow[currentBatchSize];
            
         int arrPos = 0;
         for (int x = this.position; x < lastRow; x++)
         {
            ChildAssociationRef assoc = parents.get(x);
            NodeRef parentNodeRef = assoc.getParentRef();
            ResultSetRowNode rowNode = new ResultSetRowNode(parentNodeRef.getId(), nodeService.getType(parentNodeRef).toString(), null);
            ResultSetRow row = new ResultSetRow();
            row.setRowIndex(x);
            row.setNode(rowNode);
            
            // add the row to the overall results
            rows[arrPos] = row;
            arrPos++;
         }
         
         // add the rows to the result set and set the total row count
         batchResults.setRows(rows);
         batchResults.setTotalRowCount(totalRows);
         
         queryResult = new QueryResult(getId(), batchResults);
         
         // move the position on
         updatePosition(totalRows, queryResult);
         
         if (logger.isDebugEnabled())
            logger.debug("After getNextResultsBatch: " + toString());
      }
      
      return queryResult;
   }
   
   /**
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString()
   {
      StringBuilder builder = new StringBuilder(super.toString());
      builder.append(" (id=").append(getId());
      builder.append(" batchSize=").append(this.batchSize);
      builder.append(" position=").append(this.position);
      builder.append(" node-id=").append(this.node.getUuid()).append(")");
      return builder.toString();
   }
}

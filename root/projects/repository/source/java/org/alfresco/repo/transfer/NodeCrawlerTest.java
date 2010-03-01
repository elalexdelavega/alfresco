/*
 * Copyright (C) 2009-2010 Alfresco Software Limited.
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
 * FLOSS exception.  You should have received a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.transfer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.transfer.NodeCrawler;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.BaseAlfrescoSpringTest;
import org.alfresco.util.GUID;

/**
 * Unit test for RepoTransferReceiverImpl
 * 
 * @author Brian Remmington
 */
public class NodeCrawlerTest extends BaseAlfrescoSpringTest
{
    private ServiceRegistry serviceRegistry;
    private NodeRef companyHome;

    /**
     * Called during the transaction setup
     */
    @SuppressWarnings("deprecation")
    protected void onSetUpInTransaction() throws Exception
    {
        super.onSetUpInTransaction();

        // Get the required services
        this.nodeService = (NodeService) this.getApplicationContext().getBean("NodeService");
        this.serviceRegistry = (ServiceRegistry) this.getApplicationContext().getBean("ServiceRegistry");
        ResultSet rs = serviceRegistry.getSearchService().query(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
                SearchService.LANGUAGE_XPATH, "/app:company_home");
        if (rs.length() == 0)
        {
            fail("Could not find company home");
        }
        companyHome = rs.getNodeRef(0);
    }

    public void testContentClassFilter() throws Exception
    {
        NodeRef node1 = makeNode(companyHome, ContentModel.TYPE_BASE);
        NodeRef node2 = makeNode(companyHome, ContentModel.TYPE_CONTENT);
        NodeRef node3 = makeNode(companyHome, ContentModel.TYPE_FOLDER);
        NodeRef node4 = makeNode(companyHome, ContentModel.TYPE_CONTENT);
        NodeRef node5 = makeNode(companyHome, ContentModel.TYPE_THUMBNAIL);

        nodeService.addAspect(node4, ContentModel.ASPECT_REFERENCEABLE, null);

        ContentClassFilter contentFilter = new ContentClassFilter(serviceRegistry);
        contentFilter.setContentClasses(ContentModel.TYPE_BASE);

        assertTrue(contentFilter.accept(node1));
        assertTrue(contentFilter.accept(node2));
        assertTrue(contentFilter.accept(node3));

        contentFilter.setDirectOnly(true);
        assertTrue(contentFilter.accept(node1));
        assertFalse(contentFilter.accept(node2));
        assertFalse(contentFilter.accept(node3));

        contentFilter.setContentClasses(ContentModel.TYPE_BASE, ContentModel.ASPECT_REFERENCEABLE);
        assertTrue(contentFilter.accept(node4));

        contentFilter.setExclude(true);
        contentFilter.setDirectOnly(false);
        contentFilter.setContentClasses(ContentModel.TYPE_CONTENT);
        assertTrue(contentFilter.accept(node1));
        assertFalse(contentFilter.accept(node2));
        assertTrue(contentFilter.accept(node3));
        assertFalse(contentFilter.accept(node5));

        contentFilter.setDirectOnly(true);
        assertTrue(contentFilter.accept(node1));
        assertFalse(contentFilter.accept(node2));
        assertTrue(contentFilter.accept(node3));
        assertTrue(contentFilter.accept(node5));

    }

    public void testChildAssociationFinder()
    {
        makeNode(companyHome, ContentModel.TYPE_BASE);
        makeNode(companyHome, ContentModel.TYPE_CONTENT);
        makeNode(companyHome, ContentModel.TYPE_CONTENT);
        makeNode(companyHome, ContentModel.TYPE_CONTENT);
        makeNode(companyHome, ContentModel.TYPE_CONTENT);
        makeNode(companyHome, ContentModel.TYPE_CONTENT);
        makeNode(companyHome, ContentModel.TYPE_CONTENT);
        NodeRef node8 = makeNode(companyHome, ContentModel.TYPE_FOLDER);
        makeNode(node8, ContentModel.TYPE_FOLDER);
        NodeRef node10 = makeNode(node8, ContentModel.TYPE_FOLDER);
        makeNode(node10, ContentModel.TYPE_FOLDER);
        NodeRef node12 = makeNode(node10, ContentModel.TYPE_FOLDER);
        NodeRef node13 = makeNode(node12, ContentModel.TYPE_FOLDER);
        makeNode(node13, ContentModel.TYPE_CONTENT);
        NodeRef node15 = makeNode(node13, ContentModel.TYPE_THUMBNAIL);

        nodeService.addAspect(node8, ContentModel.ASPECT_THUMBNAILED, null);
        nodeService.addChild(node8, node15, ContentModel.ASSOC_THUMBNAILS, QName.createQName(
                NamespaceService.APP_MODEL_1_0_URI, "temp"));

        ChildAssociatedNodeFinder nodeFinder = new ChildAssociatedNodeFinder(serviceRegistry);
        nodeFinder.setAssociationTypes(ContentModel.ASSOC_CONTAINS);

        Set<NodeRef> results = nodeFinder.findFrom(node8);
        assertEquals(2, results.size());

        nodeFinder.setAssociationTypes(ContentModel.ASSOC_THUMBNAILS);
        results = nodeFinder.findFrom(node8);
        assertEquals(1, results.size());
        assertEquals(node15, new ArrayList<NodeRef>(results).get(0));
    }

    public void testCrawler()
    {
        NodeRef node8 = makeNode(companyHome, ContentModel.TYPE_FOLDER);
        NodeRef node9 = makeNode(node8, ContentModel.TYPE_FOLDER);
        NodeRef node10 = makeNode(node8, ContentModel.TYPE_FOLDER);
        NodeRef node11 = makeNode(node10, ContentModel.TYPE_FOLDER);
        NodeRef node12 = makeNode(node10, ContentModel.TYPE_FOLDER);
        NodeRef node13 = makeNode(node12, ContentModel.TYPE_FOLDER);

        makeNode(node10, ContentModel.TYPE_BASE);
        makeNode(node13, ContentModel.TYPE_CONTENT);
        makeNode(node10, ContentModel.TYPE_CONTENT);
        makeNode(node11, ContentModel.TYPE_CONTENT);
        makeNode(node8, ContentModel.TYPE_CONTENT);
        makeNode(node8, ContentModel.TYPE_CONTENT);
        makeNode(node9, ContentModel.TYPE_CONTENT);
        makeNode(node13, ContentModel.TYPE_CONTENT);
        NodeRef node15 = makeNode(node13, ContentModel.TYPE_THUMBNAIL);

        nodeService.addAspect(node8, ContentModel.ASPECT_THUMBNAILED, null);
        nodeService.addChild(node8, node15, ContentModel.ASSOC_THUMBNAILS, QName.createQName(
                NamespaceService.APP_MODEL_1_0_URI, "temp"));

        NodeCrawler crawler = new StandardNodeCrawlerImpl(serviceRegistry);
        crawler.setNodeFinders(new ChildAssociatedNodeFinder(ContentModel.ASSOC_CONTAINS));

        Set<NodeRef> crawledNodes = crawler.crawl(node8);
        assertEquals(15, crawledNodes.size());

        crawler.setNodeFilters(new ContentClassFilter(ContentModel.TYPE_FOLDER));
        crawledNodes = crawler.crawl(node8);
        assertEquals(6, crawledNodes.size());
    }

    /**
     * @param companyHome2
     * @param nodeType
     * @return
     */
    private NodeRef makeNode(NodeRef parent, QName nodeType)
    {
        String uuid = GUID.generate();
        Map<QName, Serializable> props = new HashMap<QName, Serializable>();
        props.put(ContentModel.PROP_NAME, uuid);
        ChildAssociationRef assoc = nodeService.createNode(parent, ContentModel.ASSOC_CONTAINS, QName.createQName(
                NamespaceService.APP_MODEL_1_0_URI, uuid), nodeType, props);
        return assoc.getChildRef();
    }

}
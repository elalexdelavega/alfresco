/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.repo.jscript;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.ScriptException;
import org.alfresco.service.cmr.repository.ScriptService;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

/**
 * Implementation of the ScriptService using the Rhino JavaScript engine.
 * 
 * @author Kevin Roast
 */
public class RhinoScriptService implements ScriptService
{
    /** The permission-safe node service */
    private NodeService nodeService;
    
    /** The Content Service to use */
    private ContentService contentService;
    
    /**
     * Set the node service
     * 
     * @param nodeService       The permission-safe node service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    /**
     * Set the content service
     * 
     * @param contentService    The ContentService to use
     */
    public void setContentService(ContentService contentService)
    {
        this.contentService = contentService;
    }
    
    /**
     * @see org.alfresco.service.cmr.repository.ScriptService#executeScript(java.lang.String, java.util.Map)
     */
    public Object executeScript(String scriptClasspath, Map<String, Object> model) throws ScriptException
    {
        if (scriptClasspath == null)
        {
            throw new IllegalArgumentException("Script ClassPath is mandatory.");
        }
        
        Reader reader = null;
        try
        {
            InputStream stream = getClass().getClassLoader().getResourceAsStream(scriptClasspath);
            if (stream == null)
            {
                throw new AlfrescoRuntimeException("Unable to load classpath resource: " + scriptClasspath);
            }
            reader = new InputStreamReader(stream);
            
            return executeScriptImpl(reader, model);
        }
        catch (Throwable err)
        {
            throw new ScriptException("Failed to execute script '" + scriptClasspath + "': " + err.getMessage(), err);
        }
        finally
        {
            if (reader != null)
            {
                try {reader.close();} catch (IOException ioErr) {}
            }
        }
    }

    /**
     * @see org.alfresco.service.cmr.repository.ScriptService#executeScript(org.alfresco.service.cmr.repository.NodeRef, java.util.Map)
     */
    public Object executeScript(NodeRef scriptRef, Map<String, Object> model) throws ScriptException
    {
        if (scriptRef == null)
        {
            throw new IllegalArgumentException("Script NodeRef is mandatory.");
        }
        
        Reader reader = null;
        try
        {
            if (this.nodeService.exists(scriptRef) == false)
            {
                throw new AlfrescoRuntimeException("Script Node does not exist: " + scriptRef);
            }
            ContentReader cr = this.contentService.getReader(scriptRef, ContentModel.PROP_CONTENT);
            if (cr == null || cr.exists() == false)
            {
                throw new AlfrescoRuntimeException("Script Node content not found: " + scriptRef);
            }
            reader = new InputStreamReader(cr.getContentInputStream());
            
            return executeScriptImpl(reader, model);
        }
        catch (Throwable err)
        {
            throw new ScriptException("Failed to execute script '" + scriptRef.toString() + "': " + err.getMessage(), err);
        }
        finally
        {
            if (reader != null)
            {
                try {reader.close();} catch (IOException ioErr) {}
            }
        }
    }

    /**
     * @see org.alfresco.service.cmr.repository.ScriptService#executeScriptString(java.lang.String, java.util.Map)
     */
    public Object executeScriptString(String script, Map<String, Object> model) throws ScriptException
    {
        if (script == null || script.length() == 0)
        {
            throw new IllegalArgumentException("Script argument is mandatory.");
        }
        
        Reader reader = null;
        try
        {
            reader = new StringReader(script);
            
            return executeScriptImpl(reader, model);
        }
        catch (Throwable err)
        {
            throw new ScriptException("Failed to execute supplied script: " + err.getMessage(), err);
        }
    }
    
    /**
     * Execute the script content from the supplied Reader. Adds the data model into the default
     * root scope for access by the script.
     * 
     * @param reader        Reader referencing the script to execute.
     * @param model         Data model containing objects to be added to the root scope.
     * 
     * @return result of the script execution, can be null.
     * 
     * @throws AlfrescoRuntimeException
     */
    private Object executeScriptImpl(Reader reader, Map<String, Object> model)
        throws AlfrescoRuntimeException
    {        
        // check that rhino script engine is available
        Context cx = Context.enter();
        try
        {
            // The easiest way to embed Rhino is just to create a new scope this way whenever
            // you need one. However, initStandardObjects is an expensive method to call and it
            // allocates a fair amount of memory.
            Scriptable scope = cx.initStandardObjects();
            
            // insert supplied object model into root of the default scope
            if (model != null)
            {
                for (String key : model.keySet())
                {
                    ScriptableObject.putProperty(scope, key, model.get(key));
                }
            }
            
            // execute the script
            Object result = cx.evaluateReader(scope, reader, "AlfrescoScript", 1, null);
            
            return result;
        }
        catch (Throwable err)
        {
            throw new AlfrescoRuntimeException(err.getMessage(), err);
        }
        finally
        {
            cx.exit();
        }
    }
}

package org.alfresco.module.org_alfresco_module_dod5015.caveat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.repo.jscript.BaseScopableProcessorExtension;
import org.alfresco.service.cmr.security.AuthorityService;

/**
 * Script projection of RM Caveat Config Service
 *
 * @author Mark Rogers
 */
public class ScriptRMCaveatConfigService extends BaseScopableProcessorExtension
{
    private RMCaveatConfigService caveatConfigService;
    private AuthorityService authorityService;

    public void setCaveatConfigService(RMCaveatConfigService rmCaveatConfigService)
    {
        this.caveatConfigService = rmCaveatConfigService;
    }

    public RMCaveatConfigService getRmCaveatConfigService()
    {
        return caveatConfigService;
    }
    
    public void setAuthorityService(AuthorityService authorityService)
    {
        this.authorityService = authorityService;
    }

    public AuthorityService getAuthorityService()
    {
        return authorityService;
    }
    
    public ScriptConstraint getConstraint(String listName)
    {
        //TODO Temporary conversion
        String xxx = listName.replace("_", ":");
        
        RMConstraintInfo info = caveatConfigService.getRMConstraint(xxx);
        
        if(info != null)
        {
            return new ScriptConstraint(info, caveatConfigService, getAuthorityService());
        }
        
        return null;
    }
    
    public ScriptConstraint[] getAllConstraints()
    {
        Set<RMConstraintInfo> values = caveatConfigService.getAllRMConstraints();
        
        List<ScriptConstraint> vals = new ArrayList<ScriptConstraint>(values.size());
        for(RMConstraintInfo value : values)
        {
            ScriptConstraint c = new ScriptConstraint(value, caveatConfigService, getAuthorityService());
            vals.add(c);
        }
        
        return vals.toArray(new ScriptConstraint[vals.size()]);
    }
   
    /**
     * Delete list
     * @param listName

     */
    public void deleteConstraintList(String listName)
    {
        //TODO Temporary conversion
        String xxx = listName.replace("_", ":");
        caveatConfigService.deleteRMConstraint(xxx);
    }
    

    
    /**
     * Update value
     */
    public void updateConstraintValues(String listName, String authorityName, String[]values)
    {
        //TODO Temporary conversion
        String xxx = listName.replace("_", ":");
        
        List<String> vals = new ArrayList<String>();
        caveatConfigService.updateRMConstraintListAuthority(listName, authorityName, vals);
    }
    
    /**
     * Delete the constraint values.   i.e remove an authority from a constraint list
     */
    public void deleteRMConstraintListAuthority(String listName, String authorityName)
    {
        //TODO Temporary conversion
        String xxx = listName.replace("_", ":");
        
        caveatConfigService.removeRMConstraintListAuthority(xxx, authorityName);
    }
    
    /**
     * Delete the constraint values.   i.e remove a value from a constraint list
     */
    public void deleteRMConstraintListValue(String listName, String valueName)
    {
        //TODO Temporary conversion
        String xxx = listName.replace("_", ":");
        
        caveatConfigService.removeRMConstraintListValue(xxx, valueName);

    }
    
    public ScriptConstraint createConstraint(String listName, String title, String[] allowedValues)
    {     
        //TODO Temporary conversion
        if(listName != null)
        {
            listName = listName.replace("_", ":");
        }
        
        RMConstraintInfo info = caveatConfigService.addRMConstraint(listName, title, allowedValues);
        ScriptConstraint c = new ScriptConstraint(info, caveatConfigService, getAuthorityService());
        return c;  
    }

}
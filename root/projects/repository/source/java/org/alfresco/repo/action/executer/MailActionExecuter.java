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
package org.alfresco.repo.action.executer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.template.DateCompareMethod;
import org.alfresco.repo.template.HasAspectMethod;
import org.alfresco.repo.template.I18NMessageMethod;
import org.alfresco.repo.template.TemplateNode;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.TemplateService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PersonService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.validator.EmailValidator;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;

/**
 * Mail action executor implementation.
 * 
 * @author Roy Wetherall
 */
public class MailActionExecuter extends ActionExecuterAbstractBase
    implements InitializingBean, TestModeable
{
    private static Log logger = LogFactory.getLog(MailActionExecuter.class);
    
    /**
     * Action executor constants
     */
    public static final String NAME = "mail";
    public static final String PARAM_TO = "to";
    public static final String PARAM_TO_MANY = "to_many";
    public static final String PARAM_SUBJECT = "subject";
    public static final String PARAM_TEXT = "text";
    public static final String PARAM_FROM = "from";
    public static final String PARAM_TEMPLATE = "template";
       
    /**
     * From address
     */
    private static final String FROM_ADDRESS = "alfresco@alfresco.org";
    
    private static final String REPO_REMOTE_URL = "http://localhost:8080/alfresco";
    
    /**
     * The java mail sender
     */
    private JavaMailSender javaMailSender;
    
    /**
     * The Template service
     */
    private TemplateService templateService;
    
    /**
     * The Person service
     */
    private PersonService personService;
    
    /**
     * The Authentication service
     */
    private AuthenticationService authService;
    
    /**
     * The Node Service
     */
    private NodeService nodeService;
    
    /**
     * The Authority Service
     */
    private AuthorityService authorityService;
    
    /**
     * The Service registry
     */
    private ServiceRegistry serviceRegistry;
    
    /**
     * Mail header encoding scheme
     */
    private String headerEncoding = null;
    
    /**
     * Default from address
     */
    private String fromAddress = null;
    
    /**
     * Default alfresco installation url
     */
    private String repoRemoteUrl = null;
    
    private boolean sendTestMessage = false;
    private String testMessageTo = null;
    private String testMessageSubject = "Test message";
    private String testMessageText = "This is a test message.";

    /**
     * Test mode prevents email messages from being sent.
     * It is used when unit testing when we don't actually want to send out email messages.
     * 
     * MER 20/11/2009 This is a quick and dirty fix. It should be replaced by being 
     * "mocked out" or some other better way of running the unit tests. 
     */
    private boolean testMode = false;
    private MimeMessage lastTestMessage;
    
    /**
     * @param javaMailSender    the java mail sender
     */
    public void setMailService(JavaMailSender javaMailSender) 
    {
        this.javaMailSender = javaMailSender;
    }
    
    /**
     * @param templateService   the TemplateService
     */
    public void setTemplateService(TemplateService templateService)
    {
        this.templateService = templateService;
    }
    
    /**
     * @param personService     the PersonService
     */
    public void setPersonService(PersonService personService)
    {
        this.personService = personService;
    }
    
    /**
     * @param authService       the AuthenticationService
     */
    public void setAuthenticationService(AuthenticationService authService)
    {
        this.authService = authService;
    }
    
    /**
     * @param serviceRegistry   the ServiceRegistry
     */
    public void setServiceRegistry(ServiceRegistry serviceRegistry)
    {
        this.serviceRegistry = serviceRegistry;
    }
    
    /**
     * @param authorityService  the AuthorityService
     */
    public void setAuthorityService(AuthorityService authorityService)
    {
        this.authorityService = authorityService;
    }

    /**
     * @param nodeService       the NodeService to set.
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    /**
     * @param headerEncoding     The mail header encoding to set.
     */
    public void setHeaderEncoding(String headerEncoding)
    {
        this.headerEncoding = headerEncoding;
    }

    /**
     * @param fromAddress   The default mail address.
     */
    public void setFromAddress(String fromAddress)
    {
        this.fromAddress = fromAddress;
    }

    /**
     * 
     * @param repoRemoteUrl The default alfresco installation url
     */
    public void setRepoRemoteUrl(String repoRemoteUrl)
    {
        this.repoRemoteUrl = repoRemoteUrl;
    }
    
    public void setTestMessageTo(String testMessageTo)
    {
        this.testMessageTo = testMessageTo;
    }
    
    public void setTestMessageSubject(String testMessageSubject)
    {
        this.testMessageSubject = testMessageSubject;
    }
    
    public void setTestMessageText(String testMessageText)
    {
        this.testMessageText = testMessageText;
    }

    public void setSendTestMessage(boolean sendTestMessage)
    {
        this.sendTestMessage = sendTestMessage;
    }

    
    @Override
    public void init()
    {
        super.init();
        if (sendTestMessage)
        {
            Map<String, Serializable> params = new HashMap<String, Serializable>();
            params.put(PARAM_TO, testMessageTo);
            params.put(PARAM_SUBJECT, testMessageSubject);
            params.put(PARAM_TEXT, testMessageText);
            
            Action ruleAction = serviceRegistry.getActionService().createAction(NAME, params);
            executeImpl(ruleAction, null);
        }
    }

    /**
     * Initialise bean
     */
    public void afterPropertiesSet() throws Exception
    {
        if (fromAddress == null || fromAddress.length() == 0)
        {
            fromAddress = FROM_ADDRESS;
        }
        
        if (repoRemoteUrl == null || repoRemoteUrl.length() == 0)
        {
            repoRemoteUrl = REPO_REMOTE_URL;
        }
    }
    
    /**
     * Send an email message
     * 
     * @throws AlfrescoRuntimeExeption
     */
    @Override
    protected void executeImpl(
            final Action ruleAction,
            final NodeRef actionedUponNodeRef) 
    {
        // Create the mime mail message
        MimeMessagePreparator mailPreparer = new MimeMessagePreparator()
        {
            @SuppressWarnings("unchecked")
            public void prepare(MimeMessage mimeMessage) throws MessagingException
            {
                if (logger.isDebugEnabled())
                {
                   logger.debug(ruleAction.getParameterValues());
                }
                
                MimeMessageHelper message = new MimeMessageHelper(mimeMessage);
                
                // set header encoding if one has been supplied
                if (headerEncoding != null && headerEncoding.length() != 0)
                {
                    mimeMessage.setHeader("Content-Transfer-Encoding", headerEncoding);
                }
                
                // set recipient
                String to = (String)ruleAction.getParameterValue(PARAM_TO);
                if (to != null && to.length() != 0)
                {
                    message.setTo(to);
                }
                else
                {
                    // see if multiple recipients have been supplied - as a list of authorities
                    Serializable authoritiesValue = ruleAction.getParameterValue(PARAM_TO_MANY);
                    List<String> authorities = null;
                    if (authoritiesValue != null)
                    {
                        if (authoritiesValue instanceof String)
                        {
                            authorities = new ArrayList<String>(1);
                            authorities.add((String)authoritiesValue);
                        }
                        else
                        {
                            authorities = (List<String>)authoritiesValue;
                        }
                    }
                    
                    if (authorities != null && authorities.size() != 0)
                    {
                        List<String> recipients = new ArrayList<String>(authorities.size());
                        for (String authority : authorities)
                        {
                            AuthorityType authType = AuthorityType.getAuthorityType(authority);
                            if (authType.equals(AuthorityType.USER))
                            {
                                if (personService.personExists(authority) == true)
                                {
                                    NodeRef person = personService.getPerson(authority);
                                    String address = (String)nodeService.getProperty(person, ContentModel.PROP_EMAIL);
                                    if (address != null && address.length() != 0 && validateAddress(address))
                                    {
                                        recipients.add(address);
                                    }
                                }
                            }
                            else if (authType.equals(AuthorityType.GROUP))
                            {
                                // else notify all members of the group
                                Set<String> users = authorityService.getContainedAuthorities(AuthorityType.USER, authority, false);
                                for (String userAuth : users)
                                {
                                    if (personService.personExists(userAuth) == true)
                                    {
                                        NodeRef person = personService.getPerson(userAuth);
                                        String address = (String)nodeService.getProperty(person, ContentModel.PROP_EMAIL);
                                        if (address != null && address.length() != 0)
                                        {
                                            recipients.add(address);
                                        }
                                    }
                                }
                            }
                        }
                        
                        message.setTo(recipients.toArray(new String[recipients.size()]));
                    }
                    else
                    {
                        // No recipiants have been specified
                        logger.error("No recipiant has been specified for the mail action");
                    }
                }
                
                // set subject line
                message.setSubject((String)ruleAction.getParameterValue(PARAM_SUBJECT));
                
                // See if an email template has been specified
                String text = null;
                NodeRef templateRef = (NodeRef)ruleAction.getParameterValue(PARAM_TEMPLATE);
                if (templateRef != null)
                {
                    // build the email template model
                    Map<String, Object> model = createEmailTemplateModel(actionedUponNodeRef);
                    
                    // process the template against the model
                    text = templateService.processTemplate("freemarker", templateRef.toString(), model);
                }
                
                // set the text body of the message
                if (text == null)
                {
                    text = (String)ruleAction.getParameterValue(PARAM_TEXT);
                }
                message.setText(text);
                
                // set the from address
                NodeRef person = personService.getPerson(authService.getCurrentUserName());
                
                String fromActualUser = null;
                if (person != null)
                {
                    fromActualUser = (String) nodeService.getProperty(person, ContentModel.PROP_EMAIL);
                }
                if( fromActualUser != null && fromActualUser.length() != 0)
                {
                    message.setFrom(fromActualUser);
                }
                else
                {
                    String from = (String)ruleAction.getParameterValue(PARAM_FROM);
                    if (from == null || from.length() == 0)
                    {
                        message.setFrom(fromAddress);
                    }
                    else
                    {
                        message.setFrom(from);
                    }
                }
            }
        };
                
        try
        {
            // Send the message unless we are in "testMode"
            if(!testMode)
            {
                javaMailSender.send(mailPreparer);
            }
            else
            {
               try {
                  MimeMessage mimeMessage = javaMailSender.createMimeMessage(); 
                  mailPreparer.prepare(mimeMessage);
                  lastTestMessage = mimeMessage;
               } catch(Exception e) {
                  System.err.println(e);
               }
            }
        }
        catch (MailException e)
        {
            String to = (String)ruleAction.getParameterValue(PARAM_TO);
            if (to == null)
            {
               Object obj = ruleAction.getParameterValue(PARAM_TO_MANY);
               if (obj != null)
               {
                  to = obj.toString();
               }
            }
            
            logger.error("Failed to send email to " + to, e);
            
            throw new AlfrescoRuntimeException("Failed to send email to:" + to, e);   
        }
    }
    
    /**
     * Return true if address has valid format
     * @param address
     * @return
     */
    private boolean validateAddress(String address)
    {
        boolean result = false;
        
        EmailValidator emailValidator = EmailValidator.getInstance();
        if (emailValidator.isValid(address))
        {
            result = true;
        }
        else 
        {
            logger.error("Failed to send email to '" + address + "' as the address is incorrectly formatted" );
        }
      
        return result;
    }

   /**
    * @param ref    The node representing the current document ref
    * 
    * @return Model map for email templates
    */
   private Map<String, Object> createEmailTemplateModel(NodeRef ref)
   {
      Map<String, Object> model = new HashMap<String, Object>(8, 1.0f);
      
      NodeRef person = personService.getPerson(authService.getCurrentUserName());
      model.put("person", new TemplateNode(person, serviceRegistry, null));
      model.put("document", new TemplateNode(ref, serviceRegistry, null));
      NodeRef parent = serviceRegistry.getNodeService().getPrimaryParent(ref).getParentRef();
      model.put("space", new TemplateNode(parent, serviceRegistry, null));
      
      // current date/time is useful to have and isn't supplied by FreeMarker by default
      model.put("date", new Date());
      
      // add custom method objects
      model.put("hasAspect", new HasAspectMethod());
      model.put("message", new I18NMessageMethod());
      model.put("dateCompare", new DateCompareMethod());
      model.put("url", new URLHelper(repoRemoteUrl));
      
      return model;
   }
    
    /**
     * Add the parameter definitions
     */
    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList) 
    {
        paramList.add(new ParameterDefinitionImpl(PARAM_TO, DataTypeDefinition.TEXT, false, getParamDisplayLabel(PARAM_TO)));
        paramList.add(new ParameterDefinitionImpl(PARAM_TO_MANY, DataTypeDefinition.ANY, false, getParamDisplayLabel(PARAM_TO_MANY), true));
        paramList.add(new ParameterDefinitionImpl(PARAM_SUBJECT, DataTypeDefinition.TEXT, true, getParamDisplayLabel(PARAM_SUBJECT)));
        paramList.add(new ParameterDefinitionImpl(PARAM_TEXT, DataTypeDefinition.TEXT, false, getParamDisplayLabel(PARAM_TEXT)));
        paramList.add(new ParameterDefinitionImpl(PARAM_FROM, DataTypeDefinition.TEXT, false, getParamDisplayLabel(PARAM_FROM)));
        paramList.add(new ParameterDefinitionImpl(PARAM_TEMPLATE, DataTypeDefinition.NODE_REF, false, getParamDisplayLabel(PARAM_TEMPLATE), false, "ac-email-templates"));
    }

    public void setTestMode(boolean testMode)
    {
        this.testMode = testMode;
    }

    public boolean isTestMode()
    {
        return testMode;
    }

    /**
     * Returns the most recent message that wasn't sent
     *  because TestMode had been enabled.
     */
    public MimeMessage retrieveLastTestMessage()
    {
        return lastTestMessage; 
    }

    public static class URLHelper
    {
        String contextPath;
        String serverPath;
        
        public URLHelper(String repoRemoteUrl)
        {
            String[] parts = repoRemoteUrl.split("/");
            this.contextPath = "/" + parts[parts.length - 1];
            this.serverPath = parts[0] + "//" + parts[2];
        }
        
        public String getContext()
        {
           return this.contextPath;
        }

        public String getServerPath()
        {
           return this.serverPath;
        }
    }
}

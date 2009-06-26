package org.alfresco.repo.imap;

import static org.alfresco.repo.imap.AlfrescoImapConst.BASE_64_ENCODING;
import static org.alfresco.repo.imap.AlfrescoImapConst.CONTENT_TRANSFER_ENCODING;
import static org.alfresco.repo.imap.AlfrescoImapConst.UTF_8;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

import org.alfresco.model.ContentModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.namespace.QName;

public class ContentModelMessage extends AbstractMimeMessage
{

    public ContentModelMessage(FileInfo fileInfo, ServiceRegistry serviceRegistry, boolean generateBody) throws MessagingException
    {
        super(fileInfo, serviceRegistry, generateBody);
    }

    @Override
    public void buildMessageInternal() throws MessagingException
    {
        if (generateBody != false)
        {
            setMessageHeaders();
            buildContentModelMessage();
        }
    }

    /**
     * This method builds {@link MimeMessage} based on {@link ContentModel}
     * 
     * @param fileInfo - Source file information {@link FileInfo}
     * @throws MessagingException
     */
    private void buildContentModelMessage() throws MessagingException
    {
        Map<QName, Serializable> properties = messageFileInfo.getProperties();
        String prop = null;
        setSentDate(messageFileInfo.getModifiedDate());
        // Add FROM address
        Address[] addressList = buildSenderFromAddress();
        addFrom(addressList);
        // Add TO address
        addressList = buildRecipientToAddress();
        addRecipients(RecipientType.TO, addressList);
        prop = (String) properties.get(ContentModel.PROP_TITLE);
        try
        {
            prop = (prop == null) ? messageFileInfo.getName() : prop;
            prop = MimeUtility.encodeText(prop, KOI8R_CHARSET, null);
        }
        catch (UnsupportedEncodingException e)
        {
            // ignore
        }
        setSubject(prop);
        setContent(buildContentModelMultipart());
    }

    /**
     * This method builds {@link Multipart} based on {@link ContentModel}
     * 
     * @param fileInfo - Source file information {@link FileInfo}
     * @throws MessagingException
     */
    private Multipart buildContentModelMultipart() throws MessagingException
    {
        MimeMultipart rootMultipart = new MimeMultipart("alternative");
        // Cite MOB-395: "email agent will be used to select an appropriate template" - we are not able to
        // detect an email agent so we use a default template for all messages.
        // See AlfrescoImapConst to see the possible templates to use.
        String bodyTxt = getEmailBodyText(EmailBodyType.TEXT_PLAIN);
        rootMultipart.addBodyPart(getTextBodyPart(bodyTxt, EmailBodyType.TEXT_PLAIN.getSubtype()));
        String bodyHtml = getEmailBodyText(EmailBodyType.TEXT_HTML);
        rootMultipart.addBodyPart(getTextBodyPart(bodyHtml, EmailBodyType.TEXT_HTML.getSubtype()));
        return rootMultipart;
    }

    private MimeBodyPart getTextBodyPart(String bodyText, String subtype) throws MessagingException
    {
        MimeBodyPart result = new MimeBodyPart();
        result.setText(bodyText, UTF_8, subtype);
        result.addHeader(CONTENT_TRANSFER_ENCODING, BASE_64_ENCODING);
        return result;
    }

}
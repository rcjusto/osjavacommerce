// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   HTMLaltMultipart.java

package org.store.core.mail;

import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import java.util.ArrayList;
import java.util.Iterator;

// Referenced classes of package com.util.mail:
//            GeneralMultipart, GeneralTextPart, FileAttachment, GeneralPart

public class HTMLaltMultipart extends GeneralMultipart {

    public HTMLaltMultipart()
    {
        HTMLText = new GeneralTextPart("Here is the HTML see too the text part", "text/html; charset=utf-8");
        plainText = new GeneralTextPart("This is the text plain part see to the html part");
        includeFiles = new ArrayList();
    }

    public HTMLaltMultipart(String HTML, String plain)
    {
        HTMLText = new GeneralTextPart("Here is the HTML see too the text part", "text/html; charset=utf-8");
        plainText = new GeneralTextPart("This is the text plain part see to the html part");
        includeFiles = new ArrayList();
        setHTMLText(HTML);
        setPlainText(plain);
    }

    public HTMLaltMultipart(GeneralTextPart htmlPart, GeneralTextPart textPart)
    {
        HTMLText = new GeneralTextPart("Here is the HTML see too the text part", "text/html; charset=utf-8");
        plainText = new GeneralTextPart("This is the text plain part see to the html part");
        includeFiles = new ArrayList();
        HTMLText = htmlPart;
        if(!HTMLText.getContentType().equalsIgnoreCase("text/html; charset=utf-8"))
            HTMLText.setContentType("text/html; charset=utf-8");
        plainText = textPart;
    }

    public void setHTMLText(GeneralTextPart HTMLText)
    {
        this.HTMLText = HTMLText;
    }

    public void setHTMLText(String HTMLText)
    {
        this.HTMLText = new GeneralTextPart(HTMLText, "text/html; charset=utf-8");
    }

    public void setPlainText(GeneralTextPart plainText)
    {
        this.plainText = plainText;
    }

    public void setPlainText(String plainText)
    {
        this.plainText = new GeneralTextPart(plainText);
    }

    public void includeFile(FileAttachment _includeFile)
    {
        includeFiles.add(_includeFile);
    }

    public void includeFiles(FileAttachment include_Files[])
    {
        for(int i = 0; i < include_Files.length; i++)
            includeFiles.add(include_Files[i]);

    }

    public GeneralTextPart getHTMLText()
    {
        return HTMLText;
    }

    public GeneralTextPart getPlainText()
    {
        return plainText;
    }

    public String getStringPlainText()
    {
        return plainText.getText();
    }

    public Iterator getIncludeFiles()
    {
        return includeFiles.iterator();
    }

    public Multipart getMultipartContent()
        throws MessagingException
    {
        Multipart alternative = new MimeMultipart("alternative");
        // alternative.addBodyPart(plainText.getAsPart());
        alternative.addBodyPart(HTMLText.getAsPart());
        Multipart multiP;
        if(includeFiles.size() != 0)
        {
            multiP = new MimeMultipart("related; type=\"multipart/alternative\"");
            MimeBodyPart mbp = new MimeBodyPart();
            mbp.setContent(alternative);
            multiP.addBodyPart(mbp);
            Object test[] = includeFiles.toArray();
            for(int i = 0; i < test.length; i++)
                multiP.addBodyPart(((FileAttachment)test[i]).getAsPart());

        } else
        {
            multiP = alternative;
        }
        Iterator itr = getParts();
        Multipart endMP;
        if(itr != null)
        {
            endMP = new MimeMultipart();
            MimeBodyPart mbp = new MimeBodyPart();
            mbp.setContent(multiP);
            endMP.addBodyPart(mbp);
            GeneralPart gpt;
            for(; itr.hasNext(); endMP.addBodyPart(gpt.getAsPart()))
                gpt = (GeneralPart)itr.next();

        } else
        {
            endMP = multiP;
        }
        return endMP;
    }

    private GeneralTextPart HTMLText;
    private GeneralTextPart plainText;
    private ArrayList includeFiles;
}

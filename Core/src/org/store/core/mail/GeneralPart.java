// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   GeneralPart.java

package org.store.core.mail;

import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import java.util.HashMap;
import java.util.Iterator;

public abstract class GeneralPart
{

    public GeneralPart()
    {
        headers = new HashMap();
        disposition = "attachment";
    }

    public String getDisposition()
    {
        return disposition;
    }

    public void setDisposition(String dsp)
    {
        disposition = dsp;
    }

    public void setContentType(String ct)
    {
        headers.put("Content-Type", ct);
    }

    public String getContentType()
    {
        return headers.get("Content-Type") == null ? "text/plain; charset=utf-8" : (String)headers.get("Content-Type");
    }

    public void setHeader(String name, String value)
    {
        if(headers.containsKey(name))
            headers.remove(name);
        headers.put(name, value);
    }

    public Iterator getHeaders()
    {
        return headers.keySet().iterator();
    }

    public boolean containHeader(String header)
    {
        boolean is = false;
        for(Iterator allHeader = headers.keySet().iterator(); allHeader.hasNext() || !is; is = header.equalsIgnoreCase((String)allHeader.next()));
        return is;
    }

    public String getHeaderValue(String head)
    {
        return (String)headers.get(head);
    }

    public void setFileName(String fileN)
    {
        fileName = fileN;
    }

    public String getFileName()
    {
        return fileName;
    }

    public abstract MimeBodyPart getAsPart()
        throws MessagingException;

    protected HashMap headers;
    protected String disposition;
    protected String fileName;
}

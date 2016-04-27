// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   GeneralTextPart.java

package org.store.core.mail;

import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import java.util.Iterator;

// Referenced classes of package com.util.mail:
//            GeneralPart

public class GeneralTextPart extends GeneralPart {

    public GeneralTextPart() {
        text = "";
        disposition = "inline";
        fileName = null;
        disposition = "inline";
    }

    public GeneralTextPart(String stText) {
        text = "";
        disposition = "inline";
        fileName = null;
        setDisposition("inline");
        text = stText;
    }

    public GeneralTextPart(String stText, String contentType) {
        text = "";
        disposition = "inline";
        fileName = null;
        setDisposition("inline");
        text = stText;
        if (contentType != null)
            setContentType(contentType);
    }

    public String getText() {
        return text;
    }

    public void setText(String theText) {
        text = theText;
    }

    public MimeBodyPart getAsPart()
            throws MessagingException {
        String elem = null;
        MimeBodyPart toReturn = new MimeBodyPart();
        toReturn.setContent(text, getContentType());
        for (Iterator allHeader = getHeaders(); allHeader.hasNext(); toReturn.addHeader(elem, getHeaderValue(elem)))
            elem = (String) allHeader.next();

        if (getFileName() != null)
            toReturn.setFileName(getFileName());
        if (getDisposition() != null)
            toReturn.setDisposition(getDisposition());
        else
            toReturn.removeHeader("Content-Disposition");
        return toReturn;
    }

    private String text;
    private String disposition;
    private String fileName;
}

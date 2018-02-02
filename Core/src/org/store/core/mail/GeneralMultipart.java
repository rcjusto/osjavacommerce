// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   GeneralMultipart.java

package org.store.core.mail;

import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.MimeMultipart;
import java.util.ArrayList;
import java.util.Iterator;

// Referenced classes of package com.util.mail:
//            GeneralPart, BasicMultiPart

public class GeneralMultipart implements BasicMultiPart {

    public GeneralMultipart() {
        parts = new ArrayList();
    }

    public void addPart(GeneralPart attach) {
        parts.add(attach);
    }

    public Iterator getParts() {
        if (parts.isEmpty())
            return null;
        else
            return parts.iterator();
    }

    public Multipart getMultipartContent()
            throws MessagingException {
        Multipart multiP = new MimeMultipart();
        GeneralPart gp = null;
        for (int i = 0; i < parts.size(); i++) {
            gp = (GeneralPart) parts.get(i);
            multiP.addBodyPart(gp.getAsPart());
        }

        return multiP;
    }

    public void setPart(Iterator theP)
            throws Exception {
        parts.clear();
        GeneralPart gpt = null;
        for (; theP.hasNext(); parts.add(gpt))
            gpt = (GeneralPart) theP.next();

    }

    private ArrayList parts;
}

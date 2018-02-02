// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   FileAttachment.java

package org.store.core.mail;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import java.io.File;
import java.util.Iterator;

// Referenced classes of package com.util.mail:
//            GeneralPart

public class FileAttachment extends GeneralPart {
    private String contentId;

    public FileAttachment(String cId)
    {
        file = null;
        setFilewithFilename = true;
        contentId = cId;
    }

    public FileAttachment(File f_attach, String cId)
    {
        file = null;
        setFilewithFilename = true;
        file = f_attach;
        contentId = cId;
    }

    public FileAttachment(String fname, String cId)
    {
        file = null;
        setFilewithFilename = true;
        file = new File(fname);
        contentId = cId;
    }

    public File getFile()
    {
        return file;
    }

    public void setFile(String filename)
    {
        file = new File(filename);
    }

    public void setFile(File f)
    {
        file = f;
    }

    public boolean getUseFile_Filename()
    {
        return setFilewithFilename;
    }

    public void setUseFile_Filename(boolean setFilewithFilename)
    {
        this.setFilewithFilename = setFilewithFilename;
    }

    public MimeBodyPart getAsPart()
        throws MessagingException
    {
        MimeBodyPart toReturn = new MimeBodyPart();
        toReturn.setDataHandler(new DataHandler(new FileDataSource(file)));
        String elem = null;
        for(Iterator allHeader = getHeaders(); allHeader.hasNext(); toReturn.addHeader(elem, getHeaderValue(elem)))
            elem = (String)allHeader.next();

        if(setFilewithFilename)
            toReturn.setFileName(file.getName());
        else
        if(getFileName() != null)
            toReturn.setFileName(fileName);
        if(getDisposition() != null)
            toReturn.setDisposition(disposition);
        else
            toReturn.removeHeader("Content-Disposition");
        toReturn.setHeader("Content-ID","<"+contentId+">");
        return toReturn;
    }

    private File file;
    boolean setFilewithFilename;
}

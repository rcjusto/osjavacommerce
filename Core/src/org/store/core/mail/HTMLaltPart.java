// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   HTMLaltPart.java

package org.store.core.mail;

import javax.mail.internet.MimeMultipart;

// Referenced classes of package com.util.mail:
//            FileAttachment

public final class HTMLaltPart extends MimeMultipart
{

    public HTMLaltPart()
    {
        super("related; type=\"multipart/alternative\"");
        html = "<html><body>this is the html</body></html>";
        plainText = "tis is the alt text";
        includeFiles = null;
    }

    public HTMLaltPart(String text, String html, FileAttachment flist)
    {
        this();
    }

    public String getHTML()
    {
        return html;
    }

    public String getPlainText()
    {
        return plainText;
    }

    private String html;
    private String plainText;
    private FileAttachment includeFiles;
}

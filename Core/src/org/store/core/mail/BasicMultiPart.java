// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   BasicMultipart.java

package org.store.core.mail;

import javax.mail.MessagingException;
import javax.mail.Multipart;

// Referenced classes of package com.util.mail:
//            GeneralPart

interface BasicMultiPart
{

    public abstract Multipart getMultipartContent()
        throws MessagingException;

    public abstract void addPart(org.store.core.mail.GeneralPart generalpart);
}

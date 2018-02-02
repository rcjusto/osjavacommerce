// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   UnknownToRecipient.java

package org.store.core.mail;


public final class UnknownToRecipient extends Exception
{

    public UnknownToRecipient(String str)
    {
        super(str);
    }

    public UnknownToRecipient()
    {
        this("mail exception Unknown the recipient of the message");
    }
}

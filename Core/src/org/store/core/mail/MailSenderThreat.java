package org.store.core.mail;

import org.store.core.beans.Mail;
import org.store.core.beans.StoreProperty;
import org.store.core.globals.BaseAction;
import org.store.core.globals.config.Store20Database;
import org.store.core.hibernate.HibernateSessionFactory;
import org.store.core.utils.quartz.JobStoreThread;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.io.File;
import org.apache.commons.io.FilenameUtils;

public class MailSenderThreat extends JobStoreThread {

    private static final String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";
    private Mail onlyMail;
    private Integer priority = 1;

    public MailSenderThreat(String storeCode, Store20Database databaseConfig) {
        this.storeCode = storeCode;
        this.databaseConfig = databaseConfig;
        setName("mail_task_" + String.valueOf(Calendar.getInstance().getTimeInMillis()));
    }

    public void setOnlyMail(Mail onlyMail) {
        this.onlyMail = onlyMail;
    }

    public void setStoreCode(String storeCode) {
        this.storeCode = storeCode;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    private Properties getMailProperties(Session s) {
        Properties result = new Properties();
        List<StoreProperty> l = s.createCriteria(StoreProperty.class).add(Restrictions.eq("inventaryCode", storeCode)).list();
        for (StoreProperty p : l) {
            if (StringUtils.isNotEmpty(p.getCode())) result.setProperty(p.getCode(), StringUtils.isNotEmpty(p.getValue()) ? p.getValue() : "");
        }
        return result;
    }

    @Override
    public void run() {
        sendMails();
    }

    public boolean sendMails() {
        boolean res = false;
        try {
            Session session = HibernateSessionFactory.getNewSession(databaseConfig);
            Transaction tx = session.beginTransaction();
            try {
                setExecutionMessage("Collecting pending messages");
                setExecutionPercent(0d);
                // Buscar propiedades
                Properties properties = getMailProperties(session);
                String host = properties.getProperty(StoreProperty.PROP_MAIL_HOST);
                String port = properties.getProperty(StoreProperty.PROP_MAIL_PORT);
                String ssl = properties.getProperty(StoreProperty.PROP_MAIL_SSL);
                String tls = properties.getProperty(StoreProperty.PROP_MAIL_TLS);
                String user = properties.getProperty(StoreProperty.PROP_MAIL_USER);
                String localhost = properties.getProperty(StoreProperty.PROP_MAIL_LOCALHOST);
                boolean debug = "Y".equalsIgnoreCase(properties.getProperty(StoreProperty.PROP_MAIL_DEBUG));
                String password = properties.getProperty(StoreProperty.PROP_MAIL_PASSWORD);
                Integer attempts = NumberUtils.toInt(properties.getProperty(StoreProperty.PROP_MAIL_ATTEMPTS), 3);

                // Buscar correos a enviar
                List<Mail> mails = new ArrayList<Mail>();
                if (onlyMail != null) {
                    Mail mail = (Mail) session.get(Mail.class, onlyMail.getIdMail());
                    if (mail!=null) mails.add(mail);
                } else {
                    List l = session.createCriteria(Mail.class)
                            .add(Restrictions.eq("inventaryCode", storeCode))
                            .add(Restrictions.ge("priority", priority))
                            .add(Restrictions.between("status", 0, attempts))
                            .addOrder(Order.desc("priority"))
                            .addOrder(Order.asc("idMail"))
                            .list();
                    if (l != null) mails.addAll(l);
                }

                // Enviar correos
                if (!mails.isEmpty()) {
                    int index = 1, totalMails = mails.size();

                    Properties props = new Properties();
                    props.setProperty("mail.transport.protocol", "smtp");
                    props.setProperty("mail.host", host);
                    props.setProperty("mail.smtp.host", host);
                    if (StringUtils.isNotEmpty(localhost)) {
                        props.setProperty("mail.smtp.localhost", localhost);
                    }
                    if ((StringUtils.isNotEmpty(user))) props.setProperty("mail.smtp.auth", "true");
                    //props.setProperty("mail.debug", "true");
                    if (StringUtils.isNotEmpty(port)) props.setProperty("mail.smtp.port", port);
                    if ("Y".equalsIgnoreCase(ssl)) {
                        props.setProperty("mail.smtp.socketFactory.port", port);
                        if ("N".equalsIgnoreCase(tls)) {
                            props.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
                        }
                        props.setProperty("mail.smtp.socketFactory.fallback", "false");
                    }
                    if ("Y".equalsIgnoreCase(tls)) {
                        props.put("mail.smtp.starttls.enable", "true");
                    }
                    for (Mail m : mails) {
                        setExecutionMessage("Sending message " + String.valueOf(index) + "/" + String.valueOf(totalMails));
                        setExecutionPercent(100d * index++ / (totalMails + 1));

                        if (sendMail(props, m, properties)) {
                            m.setStatus(10);
                            res = true;
                        } else {
                            int st = m.getStatus();
                            if (st >= attempts) m.setStatus(-1);
                            else m.setStatus(st + 1);
                        }
                        session.save(m);
                    }

                }

                tx.commit();
            } catch (Exception e) {
                tx.rollback();
                log.error(e.getMessage(), e);
            } finally {
                if (session.isOpen()) session.close();
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return res;
    }

    private boolean sendMail(Properties props, Mail beanMail, Properties properties) {
        String user = properties.getProperty(StoreProperty.PROP_MAIL_USER);
        boolean debug = "Y".equalsIgnoreCase(properties.getProperty(StoreProperty.PROP_MAIL_DEBUG));
        String password = properties.getProperty(StoreProperty.PROP_MAIL_PASSWORD);

        CustomAuthenticator auth = new CustomAuthenticator();
        auth.setUser(user);
        auth.setPassword(password);
        javax.mail.Session mailSession = javax.mail.Session.getInstance(props, auth);
        mailSession.setDebug(debug);

        String reply = properties.getProperty(StoreProperty.PROP_MAIL_REPLY);
        String bcc = properties.getProperty(StoreProperty.PROP_MAIL_BCC);
        String from = properties.getProperty(StoreProperty.PROP_MAIL_FROM);

        try {
            Transport transport = mailSession.getTransport("smtp");
            MimeMessage msg = new MimeMessage(mailSession);
            if (StringUtils.isNotEmpty(beanMail.getFromAddress())) {
                Address fromAddress = new InternetAddress(beanMail.getFromAddress());
                msg.setFrom(fromAddress);
            } else {
                Address fromAddress = new InternetAddress(from);
                msg.setFrom(fromAddress);
            }
            if (StringUtils.isNotEmpty(reply)) {
                msg.setReplyTo(InternetAddress.parse(reply));
            }
            if (StringUtils.isNotEmpty(beanMail.getToAddress())) {
                msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(beanMail.getToAddress()));
            }
            if (StringUtils.isNotEmpty(beanMail.getCcAddress())) {
                msg.setRecipients(Message.RecipientType.CC, InternetAddress.parse(beanMail.getCcAddress()));
            }
            if (StringUtils.isNotEmpty(bcc)) {
                msg.setRecipients(Message.RecipientType.BCC, InternetAddress.parse(bcc));
            }

            msg.setSubject((beanMail.getSubject() != null) ? beanMail.getSubject() : "");

            HTMLaltMultipart content = new HTMLaltMultipart(beanMail.getBody(), beanMail.getBody());
            if (StringUtils.isNotEmpty(beanMail.getAttachment()))
            {
                File f = new File(beanMail.getAttachment());
                if ((f.exists()) && (f.canRead()))
                {
                    FileAttachment fa = new FileAttachment(f, FilenameUtils.getName(f.getName()));
                    content.includeFile(fa);
                }
            }
            msg.setContent(content.getMultipartContent());

            msg.setSentDate(new Date());
            msg.saveChanges();
            Transport.send(msg);

        } catch (MessagingException ex) {
            beanMail.addError(Calendar.getInstance().getTime().toString() + " -> " + ex.toString());
            beanMail.addProperty("LAST_ERROR", ex.toString());
            log.error(ex.getMessage());
            return false;
        }
        return true;
    }

    public static boolean sendPendingMail(Mail mail, BaseAction action) {
        MailSenderThreat threat = new MailSenderThreat(mail.getInventaryCode(), action.getDatabaseConfig());
        threat.setOnlyMail(mail);
        return threat.sendMails();
    }

    public static void asyncSendMail(Mail mail, BaseAction action) {
        MailSenderThreat threat = new MailSenderThreat(mail.getInventaryCode(), action.getDatabaseConfig());
        threat.setOnlyMail(mail);
        threat.start();
    }

}

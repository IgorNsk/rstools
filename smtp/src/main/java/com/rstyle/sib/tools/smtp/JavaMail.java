package com.rstyle.sib.tools.smtp;

import com.sun.mail.smtp.SMTPAddressFailedException;
import com.sun.mail.smtp.SMTPAddressSucceededException;
import com.sun.mail.smtp.SMTPSendFailedException;
import com.sun.mail.smtp.SMTPTransport;
import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Optional;
import java.util.Properties;


public class JavaMail {
    private static final Logger LOG = LoggerFactory.getLogger(JavaMail.class);

    private String to = null;
    private String subject = null;
    private String from = null;
    private String cc = null;
    private String bcc = null;
    private String url = null;

    private String mailhost = null;
    private String mailer = "smtp";
    private String file = null;
    private String protocol = "smtp";
    private String host = null;
    private String user = null;
    private String password = null;
    private String record = null;    // name of folder in which to record mail
    private boolean debug = false;
    private boolean verbose = false;
    private boolean auth = false;
    private String prot = "smtp";
    private String text = null;
    private int stat;
    private int port = 0;
    private String moduleName = null;
    private Object charset;
    private boolean ssl = false;


    public JavaMail(String moduleName) {

        this.moduleName = moduleName;
        LOG.info(moduleName);

        InputStream is = null;

        try {
            Properties propsLog4j = new Properties();
            is = new FileInputStream("log4j.properties");
            propsLog4j.load(is);
            PropertyConfigurator.configure(propsLog4j);

            Properties props = new Properties();
            is = new FileInputStream("smtp.properties");
            props.load(is);

            mailhost = props.getProperty("mailhost");
            from = props.getProperty("from");
            user = props.getProperty("user");
            password = props.getProperty("password");
        } catch (Exception e) {
            LOG.error("Error init", e);
        }

    }

    public int sendSimpleMail() {

        try {
            /*
             * Prompt for To and Subject, if not specified.
             */
            LOG.info("*** Отправка сообщения ***");
            LOG.info("To: " + to);
            LOG.info("Cc: " + cc);
            LOG.info("Subject: " + subject);
            LOG.info("Text: " + text);

            /*
             * Initialize the JavaMail Session.
             */
            Properties props = new Properties(); //System.getProperties();
            Optional.ofNullable(mailhost).ifPresent(mailhost -> props.put("mail." + prot + ".host", mailhost));
            Optional.ofNullable(auth).ifPresent(auth -> props.put("mail." + prot + ".auth", "true"));


            /*
            * Create a Provider representing our extended SMTP transport
            * and set the property to use our provider.
            *
           Provider p = new Provider(Provider.Type.TRANSPORT, prot,
           "smtp$SMTPExtension", "JavaMail demo", "no version");
           props.put("mail." + prot + ".class", "smtp$SMTPExtension");
            */

            // Get a Session object
            Session session = Session.getInstance(props, null);
            if (debug) {
                session.setDebug(true);
            }

            /*
            * Register our extended SMTP transport.
            *
           session.addProvider(p);
            */

            /*
             * Construct the message and send it.
             */
            MimeMessage msg = new MimeMessage(session);
            if (from != null) {
                msg.setFrom(new InternetAddress(from));
            } else {
                msg.setFrom();
            }

            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to, false));
            Optional.ofNullable(cc).ifPresent(cc -> {
                try {
                    msg.setRecipients(Message.RecipientType.CC, InternetAddress.parse(cc, false));
                } catch (Exception e) {
                    LOG.warn("CC address is not correct");
                }
            });
            Optional.ofNullable(bcc).ifPresent(bcc -> {
                try {
                    msg.setRecipients(Message.RecipientType.BCC, InternetAddress.parse(bcc, false));
                } catch (Exception e) {
                    LOG.warn("BCC address is not correct");
                }
            });

            /*
            if (cc != null) {
                msg.setRecipients(Message.RecipientType.CC, InternetAddress.parse(cc, false));
            }

            if (bcc != null) {
                msg.setRecipients(Message.RecipientType.BCC, InternetAddress.parse(bcc, false));
            }
            */

            //String tst = javax.mail.internet.MimeUtility.encodeText(subject);
            //String tst2 = javax.mail.internet.MimeUtility.encodeText(subject,"cp1251","cp1251");


            //msg.setSubject( new String(subject.getBytes("Cp1251"),"Cp1251"));

            //javax.mail.internet.MimeUtility.decodeText(java.lang.String etext);

            //msg.setSubject(subject);
            msg.setSubject(subject, "UTF-8");


            if (file != null) {
                // Attach the specified file.
                // We need a multipart message to hold the attachment.
                LOG.info("File: " + file);

                MimeBodyPart mbp1 = new MimeBodyPart();
                mbp1.setText(text, "UTF-8");
                MimeBodyPart mbp2 = new MimeBodyPart();
                mbp2.attachFile(file);
                //FileDataSource fds = new FileDataSource(file);
                //mbp2.setDataHandler(new DataHandler(fds));
                //mbp2.setFileName(fds.getName());
                //mbp2.setHeader("Content-Transfer-Encoding", "base64");


                MimeMultipart mp = new MimeMultipart();
                mp.addBodyPart(mbp1);
                mp.addBodyPart(mbp2);
                msg.setContent(mp);
            } else {
                // If the desired charset is known, you can use
                // setText(text, charset)
                //msg.setText(text);
                msg.setContent(text, "text/plain;charset=\"UTF-8\"");
            }

            msg.setHeader("X-Mailer", mailer);
            msg.setSentDate(new Date());

            // send the thing off
            /*
            * The simple way to send a message is this:
            *
           Transport.send(msg);
            *
            * But we're going to use some SMTP-specific features for
            * demonstration purposes so we need to manage the Transport
            * object explicitly.
            */
            SMTPTransport t = (SMTPTransport) session.getTransport(prot);

            if (ssl) {
                t.setStartTLS(true);
            }

            try {
                if ((port == 0) && auth) {
                    t.connect(mailhost, user, password);
                } else if ((port != 0) && auth) {
                    t.connect(mailhost, port, user, password);
                } else {
                    t.connect();
                }
                t.sendMessage(msg, msg.getAllRecipients());
            } finally {
                if (verbose) {
                    LOG.info("Response: " + t.getLastServerResponse());
                }
                t.close();
            }

            LOG.info("Mail was sent successfully.\n");

            /*
             * Save a copy of the message, if requested.
             */
            if (record != null) {
                // Get a Store object
                Store store = null;
                if (url != null) {
                    URLName urln = new URLName(url);
                    store = session.getStore(urln);
                    store.connect();
                } else {
                    if (protocol != null) {
                        store = session.getStore(protocol);
                    } else {
                        store = session.getStore();
                    }

                    // Connect
                    if (host != null || user != null || password != null) {
                        store.connect(host, user, password);
                    } else {
                        store.connect();
                    }
                }

                // Get record Folder.  Create if it does not exist.
                Folder folder = store.getFolder(record);
                if (folder == null) {
                    System.err.println("Can't get record folder.");
                    System.exit(1);
                }
                if (!folder.exists())
                    folder.create(Folder.HOLDS_MESSAGES);

                Message[] msgs = new Message[1];
                msgs[0] = msg;
                folder.appendMessages(msgs);

                LOG.info("Mail was recorded successfully.");

            }

            stat = 0;

        } catch (SendFailedException e) {
            /*
             * Handle SMTP-specific exceptions.
             */
            MessagingException sfe = (MessagingException) e;
            if (sfe instanceof SMTPSendFailedException) {
                SMTPSendFailedException ssfe =
                        (SMTPSendFailedException) sfe;
                LOG.error("SMTP SEND FAILED:");
                if (verbose) LOG.error(ssfe.toString());
                LOG.error("  Command: " + ssfe.getCommand());
                LOG.error("  RetCode: " + ssfe.getReturnCode());
                LOG.error("  Response: " + ssfe.getMessage());
            } else {
                if (verbose) LOG.error("Send failed: " + sfe.toString());
            }

            Exception nextException;

            while ((nextException = sfe.getNextException()) != null && nextException instanceof MessagingException) {
                sfe = (MessagingException) nextException;
                if (sfe instanceof SMTPAddressFailedException) {
                    LOG.error("ADDRESS FAILED:");
                    SMTPAddressFailedException ssfe = (SMTPAddressFailedException) sfe;

                    if (verbose) LOG.error(ssfe.toString());
                    LOG.error("  Address: " + ssfe.getAddress());
                    LOG.info("  Command: " + ssfe.getCommand());
                    LOG.info("  RetCode: " + ssfe.getReturnCode());
                    LOG.info("  Response: " + ssfe.getMessage());


                } else if (nextException instanceof SMTPAddressSucceededException) {
                    LOG.error("ADDRESS SUCCEEDED:");
                    SMTPAddressSucceededException ssfe = (SMTPAddressSucceededException) sfe;

                    if (verbose) LOG.error(ssfe.toString());
                    LOG.error("  Address: " + ((SMTPAddressSucceededException) nextException).getAddress());
                    LOG.info("  Command: " + ((SMTPAddressSucceededException) nextException).getCommand());
                    LOG.info("  RetCode: " + ((SMTPAddressSucceededException) nextException).getReturnCode());
                    LOG.info("  Response: " + ((SMTPAddressSucceededException) nextException).getMessage());

                }

            }
            stat = 1;
        } catch (Exception e) {
            LOG.error("Got Exception: ", e);
            if (verbose) {
                e.printStackTrace();
            }
        }

        return stat;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getCc() {
        return cc;
    }

    public void setCc(String cc) {
        this.cc = cc;
    }

    public String getBcc() {
        return bcc;
    }

    public void setBcc(String bcc) {
        this.bcc = bcc;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMailhost() {
        return mailhost;
    }

    public void setMailhost(String mailhost) {
        this.mailhost = mailhost;
    }

    public String getMailer() {
        return mailer;
    }

    public void setMailer(String mailer) {
        this.mailer = mailer;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public boolean isAuth() {
        return auth;
    }

    public void setAuth(boolean auth) {
        this.auth = auth;
    }

    public String getProt() {
        return prot;
    }

    public void setProt(String prot) {
        this.prot = prot;
    }

    public String getRecord() {
        return record;
    }

    public void setRecord(String record) {
        this.record = record;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setSsl(boolean ssl) {
        this.ssl = ssl;
    }

}

package com.example.kuby.security.util.annotations.email;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Hashtable;

public class EmailExistenceValidator implements ConstraintValidator<EmailExists, String> {
    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {
        String domain = email.substring(email.indexOf("@") + 1);
        return hasDNSRecord(domain) && isReachableDomain(domain);
    }

    private static boolean hasDNSRecord(String domain) {
        try {
            Hashtable<String, String> env = new Hashtable<>();
            env.put("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory");
            DirContext ictx = new InitialDirContext(env);
            Attributes attrs = ictx.getAttributes(domain, new String[]{"MX"});
            Attribute attr = attrs.get("MX");
            return attr != null;
        } catch (NamingException e) {
            return false;
        }
    }

    private static boolean isReachableDomain(String domain) {
        try {
            InetAddress address = InetAddress.getByName(domain);
            return address != null;
        } catch (UnknownHostException e) {
            return false;
        }
    }
}
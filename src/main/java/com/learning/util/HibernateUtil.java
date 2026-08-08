package com.learning.util;

import com.learning.model.User;
import com.learning.model.PasswordHistory;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.cfg.Configuration;

public final class HibernateUtil {
    private static final SessionFactory SESSION_FACTORY = createSessionFactory();

    private HibernateUtil() {
    }

    public static SessionFactory getSessionFactory() {
        return SESSION_FACTORY;
    }

    private static SessionFactory createSessionFactory() {
        Configuration configuration = new Configuration();
        configuration.setProperty(AvailableSettings.JAKARTA_JDBC_URL, requiredEnvironmentVariable("DB_URL"));
        configuration.setProperty(AvailableSettings.JAKARTA_JDBC_USER, requiredEnvironmentVariable("DB_USER"));
        configuration.setProperty(AvailableSettings.JAKARTA_JDBC_PASSWORD, requiredEnvironmentVariable("DB_PASSWORD"));
        configuration.setProperty(AvailableSettings.JAKARTA_JDBC_DRIVER, "org.postgresql.Driver");
        configuration.setProperty(AvailableSettings.HBM2DDL_AUTO, "validate");
        configuration.setProperty(AvailableSettings.SHOW_SQL, "false");
        configuration.addAnnotatedClass(User.class);
        configuration.addAnnotatedClass(PasswordHistory.class);
        return configuration.buildSessionFactory();
    }

    private static String requiredEnvironmentVariable(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank() || "CHANGE_ME".equals(value)) {
            throw new IllegalStateException("Missing environment variable " + name);
        }
        return value;
    }
}

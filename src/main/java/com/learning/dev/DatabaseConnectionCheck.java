package com.learning.dev;

import com.learning.util.HibernateUtil;
import org.hibernate.Session;

public final class DatabaseConnectionCheck {
    private DatabaseConnectionCheck() {
    }

    public static void main(String[] args) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String version = session.createNativeQuery("select version()", String.class).getSingleResult();
            System.out.println(version);
        }
    }
}

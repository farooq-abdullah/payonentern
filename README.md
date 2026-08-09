# User Directory

Servlet, JSP, Hibernate, and PostgreSQL web application with user registration, authentication, and a protected registered-user list.

Run `database/01-create-users.sql` once when creating the database.

After registering the administrator account, promote it once in PostgreSQL:

```sql
UPDATE app_users SET role = 'ADMIN' WHERE username = 'your-admin-username';
```

Normal users can change their own passwords. Only an `ADMIN` user sees and can use the edit, delete, and reset-password actions.

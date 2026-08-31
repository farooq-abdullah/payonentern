# User Directory

Servlet, JSP, Hibernate, and PostgreSQL web application with user registration, authentication, and a protected registered-user list.

Run `database/01-create-users.sql` once when creating the database.

Password changes reject the current password and the four most recently replaced passwords. History begins with each user's first password change.

The first account registered in a new database receives the seeded `Administrator` role, which has every system function. Later registrations receive the default `User` role. Create the initial administrator account before exposing registration to other people.

Roles and their functions are stored in the database. The administrator can manage roles and assign a role from the Edit User screen. The application prevents deletion or permission removal that would leave no user with every system function.

The user list is searched, sorted, and paged on the server. Audit records are visible to roles with `VIEW_AUDIT_LOG`, while locked accounts can be unlocked by roles with `UNLOCK_USER`.

Password reset emails require these environment variables in addition to the database variables: `MAIL_HOST`, `MAIL_PORT` (defaults to `587`), `MAIL_FROM`, `MAIL_USERNAME`, `MAIL_PASSWORD`, and `MAIL_STARTTLS` (defaults to `true`). A reset link expires after 15 minutes, is single-use, and only its SHA-256 hash is stored in the database.

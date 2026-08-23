# User Directory

Servlet, JSP, Hibernate, and PostgreSQL web application with user registration, authentication, and a protected registered-user list.

Run `database/01-create-users.sql` once when creating the database.

Password changes reject the current password and the four most recently replaced passwords. History begins with each user's first password change.

The first account registered in a new database receives the seeded `Administrator` role, which has every system function. Later registrations receive the default `User` role. Create the initial administrator account before exposing registration to other people.

Roles and their functions are stored in the database. The administrator can manage roles and assign a role from the Edit User screen. The application prevents deletion or permission removal that would leave no user with every system function.

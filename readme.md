---CoCollab ----

1. Database Connection Flow
   DBConnection.java
   ├─ static block: loads src/config.properties ONCE at class-load time
   │      → reads db.url, db.user, db.password
   └─ getConnection(): opens a fresh short-lived connection per call

   db.url=jdbc:mysql://shuttle.proxy.rlwy.net:46674/railway
   db.user=root
   db.password=<your Railway root password>
   DB is hosted on Railway (cloud MySQL)

2. Login / Register Flow
   a.Register :
     Password is hashed with SHA-256 before storage.
     INSERT INTO users (name, email, password, role, status) VALUES (?, ?, ?, 'user', 'offline')
   b.Login :
   Input password is hashed the same way, then matched against the stored hash.
   SELECT * FROM users WHERE email = ? AND password = ? AND status != 'banned'

3. Seeded Admin Account
    Email:    admin@cocollab.com
    Password: Admin@123
    Role:     admin

4. First-Time Setup (for a new machine / new teammate)
   * Clone/download the repo from GitHub.
   * Open in IntelliJ.
   * Add the MySQL JDBC driver jar:
   * File → Project Structure → Modules → Dependencies → + → JARs → select your local mysql-connector-j-9.7.0.jar.
   * Add src/config.properties
   * db.url=jdbc:mysql://shuttle.proxy.rlwy.net:46674/railway
   * db.user=root
   * db.password=<the current Railway password>
   * Run Main.java.
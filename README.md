# Ticket Management System

A full-stack customer service ticket tracking web application built with **Core Java, JDBC, PostgreSQL, HTML5, CSS3, and JavaScript**.

## Live Frontend Demo
- **Interactive UI Preview:** [https://biswajeetranasingh2023.github.io/ticket-management-system/](https://biswajeetranasingh2023.github.io/ticket-management-system/)  
*(Note: The GitHub Pages link demonstrates the interactive frontend UI. Full PostgreSQL persistence runs locally via the Java JDBC server below.)*

## How to Run Locally in Chrome
1. **Prerequisites:** Java 17+, PostgreSQL 15+, and the PostgreSQL JDBC Driver (`postgresql-42.7.x.jar`).
2. **Database Setup:** Create a PostgreSQL database named `ticket_system` and execute `schema.sql`.
3. **Configure Credentials:** Update `DB_USER` and `DB_PASS` in `src/TicketServer.java`.
4. **Run Backend:** Compile and run `TicketServer.java` with the PostgreSQL JDBC `.jar` on the classpath.
5. **Open in Chrome:** Copy the `http://localhost:<port>` URL printed in the console and open it in Google Chrome.

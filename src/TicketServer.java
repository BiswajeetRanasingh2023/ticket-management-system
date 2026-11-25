import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.*;

public class TicketServer {
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/ticket_system";
    private static final String DB_USER = "postgres";
    private static final String DB_PASS = "Biswajit@2001";

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);

        // Serve HTML Frontend
        server.createContext("/", exchange -> {
            byte[] response = Files.readAllBytes(Paths.get("index.html"));
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, response.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response);
            }
        });

        // API: Get all tickets or Create a ticket
        server.createContext("/api/tickets", exchange -> {
            String method = exchange.getRequestMethod();
            if ("GET".equalsIgnoreCase(method)) {
                handleGetTickets(exchange);
            } else if ("POST".equalsIgnoreCase(method)) {
                handleCreateTicket(exchange);
            } else if ("PUT".equalsIgnoreCase(method)) {
                handleUpdateStatus(exchange);
            }
        });

        server.setExecutor(null);
        System.out.println("Server running at http://localhost:" + server.getAddress().getPort());
        server.start();
    }

    private static void handleGetTickets(HttpExchange exchange) throws IOException {
        StringBuilder json = new StringBuilder("[");
        String query = "SELECT t.ticket_id, c.full_name, c.phone, t.subject, t.priority, t.status " +
                       "FROM tickets t JOIN customers c ON t.customer_id = c.customer_id ORDER BY t.ticket_id DESC";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            boolean first = true;
            while (rs.next()) {
                if (!first) json.append(",");
                first = false;
                json.append(String.format(
                    "{\"id\":%d,\"customer\":\"%s\",\"phone\":\"%s\",\"subject\":\"%s\",\"priority\":\"%s\",\"status\":\"%s\"}",
                    rs.getInt("ticket_id"),
                    rs.getString("full_name"),
                    rs.getString("phone"),
                    rs.getString("subject"),
                    rs.getString("priority"),
                    rs.getString("status")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        json.append("]");
        sendJson(exchange, json.toString());
    }

    private static void handleCreateTicket(HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Map<String, String> params = parseForm(body);

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            // 1. Insert or get Customer
            int customerId = -1;
            String checkSql = "SELECT customer_id FROM customers WHERE email = ?";
            try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
                ps.setString(1, params.get("email"));
                ResultSet rs = ps.executeQuery();
                if (rs.next()) customerId = rs.getInt("customer_id");
            }

            if (customerId == -1) {
                String insertCust = "INSERT INTO customers (full_name, email, phone) VALUES (?, ?, ?) RETURNING customer_id";
                try (PreparedStatement ps = conn.prepareStatement(insertCust)) {
                    ps.setString(1, params.get("name"));
                    ps.setString(2, params.get("email"));
                    ps.setString(3, params.get("phone"));
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) customerId = rs.getInt(1);
                }
            }

            // 2. Insert Ticket
            String insertTicket = "INSERT INTO tickets (customer_id, subject, description, priority, status) VALUES (?, ?, ?, ?, 'Open')";
            try (PreparedStatement ps = conn.prepareStatement(insertTicket)) {
                ps.setInt(1, customerId);
                ps.setString(2, params.get("subject"));
                ps.setString(3, params.get("description"));
                ps.setString(4, params.get("priority"));
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        sendJson(exchange, "{\"message\":\"Ticket Created\"}");
    }

    private static void handleUpdateStatus(HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Map<String, String> params = parseForm(body);
        String sql = "UPDATE tickets SET status = ? WHERE ticket_id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, params.get("status"));
            ps.setInt(2, Integer.parseInt(params.get("id")));
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        sendJson(exchange, "{\"message\":\"Status Updated\"}");
    }

    private static void sendJson(HttpExchange exchange, String response) throws IOException {
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static Map<String, String> parseForm(String data) {
        Map<String, String> map = new HashMap<>();
        for (String pair : data.split("&")) {
            String[] kv = pair.split("=");
            if (kv.length == 2) {
                map.put(kv[0], java.net.URLDecoder.decode(kv[1], StandardCharsets.UTF_8));
            }
        }
        return map;
    }
}
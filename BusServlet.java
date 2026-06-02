package busResv;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/BusServlet")
public class BusServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = request.getReader();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        String jsonString = sb.toString();

        String action = getValueFromJson(jsonString, "action");
        String jsonResponse = "{\"status\":\"failed\",\"message\":\"Action logic failed\"}";

        String dbUrl = "jdbc:mysql://localhost:3306/bus_reservation";
        String dbUser = "root";
        String dbPassword = "root"; 

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);

            // 1. REGISTER OPERATION
            if ("register".equalsIgnoreCase(action)) {
                String user = getValueFromJson(jsonString, "username");
                String pass = getValueFromJson(jsonString, "password");

                String sql = "INSERT INTO users (username, password, role) VALUES (?, ?, 'USER')";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, user);
                pstmt.setString(2, pass);
                int result = pstmt.executeUpdate();
                pstmt.close();

                if (result > 0) {
                    jsonResponse = "{\"status\":\"success\",\"message\":\"Registration Successful! Redirecting to Login.\"}";
                } else {
                    jsonResponse = "{\"status\":\"failed\",\"message\":\"Registration DB Error!\"}";
                }
            }
            
            // 2. LOGIN OPERATION
            else if ("login".equalsIgnoreCase(action)) {
                String user = getValueFromJson(jsonString, "username");
                String pass = getValueFromJson(jsonString, "password");
                String role = getValueFromJson(jsonString, "role");

                String sql = "SELECT * FROM users WHERE username=? AND password=? AND role=?";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, user);
                pstmt.setString(2, pass);
                pstmt.setString(3, role);
                ResultSet rs = pstmt.executeQuery();

                if (rs.next()) {
                    jsonResponse = "{\"status\":\"success\",\"role\":\"" + role + "\",\"message\":\"Login Success!\"}";
                } else {
                    jsonResponse = "{\"status\":\"failed\",\"message\":\"Invalid Credentials! Please try again.\"}";
                }
                rs.close();
                pstmt.close();
            }
            
            // 3. BOOKING OPERATION
            else if ("book".equalsIgnoreCase(action)) {
                String passengerName = getValueFromJson(jsonString, "passengerName");
                String email = getValueFromJson(jsonString, "passengerEmail");
                String busNo = getValueFromJson(jsonString, "busNo");
                String source = getValueFromJson(jsonString, "startingPoint");
                String destination = getValueFromJson(jsonString, "destination");
                String travelDate = getValueFromJson(jsonString, "date");

                String sql = "INSERT INTO bookings (passenger_name, bus_number, source, destination, travel_date, email) VALUES (?, ?, ?, ?, ?, ?)";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, passengerName);
                pstmt.setString(2, busNo);
                pstmt.setString(3, source);
                pstmt.setString(4, destination);
                pstmt.setString(5, travelDate);
                pstmt.setString(6, email);
                int result = pstmt.executeUpdate();
                pstmt.close();

                if (result > 0) {
                   
                    try {
                        String mailSubject = "Bus Booking Confirmed - Ticket ID Alert";
                        String mailBody = "Hello " + passengerName + ",\n\nYour Ticket Booking from " + source + " to " + destination + " on " + travelDate + " is fully confirmed.";
                        EmailSender.sendEmail(email, mailSubject, mailBody);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    jsonResponse = "{\"status\":\"success\",\"message\":\"Booking Successful! Ticket Confirmation Email Sent.\"}";
                } else {
                    jsonResponse = "{\"status\":\"failed\",\"message\":\"Booking Write Error!\"}";
                }
            }
            
            // 4. CANCELLATION OPERATION
            else if ("cancel".equalsIgnoreCase(action)) {
                String bookingNo = getValueFromJson(jsonString, "bookingNo");

                String sql = "DELETE FROM bookings WHERE id=?";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, bookingNo);
                int result = pstmt.executeUpdate();
                pstmt.close();

                if (result > 0) {
                    jsonResponse = "{\"status\":\"success\",\"message\":\"Ticket Cancelled Successfully! Code nodes updated.\"}";
                } else {
                    jsonResponse = "{\"status\":\"failed\",\"message\":\"Invalid Booking ID! Code reference not found.\"}";
                }
            }

            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
            jsonResponse = "{\"status\":\"error\",\"message\":\"Server Core Crash: " + e.getMessage() + "\"}";
        }

        out.print(jsonResponse);
        out.flush();
    }

    private String getValueFromJson(String json, String key) {
        try {
            String searchPattern = "\"" + key + "\":\"";
            int startIdx = json.indexOf(searchPattern);
            if (startIdx == -1) {
                searchPattern = "\"" + key + "\":";
                startIdx = json.indexOf(searchPattern);
                if (startIdx == -1) return "";
                int endIdx = json.indexOf(",", startIdx);
                if (endIdx == -1) endIdx = json.indexOf("}", startIdx);
                return json.substring(startIdx + searchPattern.length(), endIdx).trim().replace("\"", "");
            }
            startIdx += searchPattern.length();
            int endIdx = json.indexOf("\"", startIdx);
            return json.substring(startIdx, endIdx);
        } catch (Exception e) {
            return "";
        }
    }
}

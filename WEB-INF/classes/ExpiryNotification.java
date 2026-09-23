import java.io.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.mail.*;
import javax.mail.internet.*;

public class ExpiryNotification extends HttpServlet {

    private static final String SENDER_EMAIL = "nagamounikavadlamudi@gmail.com";
    // REPLACE THIS WITH YOUR NEW GOOGLE APP PASSWORD (No spaces)
    private static final String APP_PASSWORD = "mizimlhpwtonrwqw"; 

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        out.println("<html><head><title>Expiry Notifications</title>");
        out.println("<style>");
        out.println("body{font-family:Arial,sans-serif;background:#f4f6f9;padding:30px;}");
        out.println("h2{text-align:center;color:#333;}");
        out.println(".box{background:white;padding:20px;margin:15px auto;width:70%;border-radius:8px;box-shadow:0 2px 6px rgba(0,0,0,0.1);}");
        out.println(".expired{border-left:5px solid #dc3545;color:#dc3545;}");
        out.println(".alert{border-left:5px solid #fd7e14;color:#fd7e14;}");
        out.println(".reminder{border-left:5px solid #0d6efd;color:#0d6efd;}");
        out.println("a{display:block;text-align:center;margin-top:20px;color:#0d6efd;font-weight:bold;}");
        out.println("</style></head><body>");

        out.println("<h2>PharmaMate Expiry Notifications</h2>");

        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            con = DBConnection.getConnection();
            
            // =========================================================
            // Automatically create the table in the correct database
            // =========================================================
            String createTableSQL = "CREATE TABLE IF NOT EXISTS expiry_notifications ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY, "
                    + "medicine_id INT NOT NULL, "
                    + "notification_type VARCHAR(20) NOT NULL, "
                    + "sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                    + "UNIQUE KEY unique_alert (medicine_id, notification_type))";
            try (Statement stmt = con.createStatement()) {
                stmt.execute(createTableSQL);
            }
            // =========================================================

            String query = "SELECT id, name, expiry_date FROM medicines";
            ps = con.prepareStatement(query);
            rs = ps.executeQuery();

            YearMonth currentMonth = YearMonth.now();
            LocalDate today = LocalDate.now();
            boolean found = false;

            while (rs.next()) {
                int medicineId = rs.getInt("id");
                String medicineName = rs.getString("name");
                String expiry = rs.getString("expiry_date");

                if (expiry == null || expiry.trim().isEmpty()) {
                    continue;
                }

                YearMonth expiryMonth;
                if (expiry.length() >= 7) {
                    expiryMonth = YearMonth.parse(expiry.substring(0, 7));
                } else {
                    continue;
                }

                LocalDate expiryDate = expiryMonth.atEndOfMonth();
                long monthsLeft = ChronoUnit.MONTHS.between(currentMonth, expiryMonth);

                String notificationType = null;
                String cssClass = "";
                String displayTitle = "";

                if (expiryDate.isBefore(today) || monthsLeft < 0) {
                    notificationType = "EXPIRED";
                    cssClass = "expired";
                    displayTitle = "EXPIRED: " + medicineName + " (Expired: " + expiry + ")";
                } 
                else if (monthsLeft <= 1) {
                    notificationType = "ONE_MONTH";
                    cssClass = "alert";
                    displayTitle = "ALERT: " + medicineName + " expires within 1 month! (" + expiry + ")";
                } 
                else if (monthsLeft == 6) {
                    notificationType = "SIX_MONTHS";
                    cssClass = "reminder";
                    displayTitle = "REMINDER: " + medicineName + " will expire in 6 months (" + expiry + ")";
                }

                if (notificationType != null) {
                    found = true;
                    out.println("<div class='box " + cssClass + "'>");
                    out.println("<strong>" + displayTitle + "</strong>");

                    if (!isAlreadySent(con, medicineId, notificationType)) {
                        try {
                            // Passed the database connection so the method can fetch all users
                            sendExpiryEmail(con, medicineName, expiry, notificationType);
                            markAsSent(con, medicineId, notificationType);
                            out.println("<p style='color:green;margin:5px 0 0 0;'>&#10003; Alert email dispatched to all employees.</p>");
                        } catch (Exception mailError) {
                            out.println("<p style='color:red;margin:5px 0 0 0;'>Mail delivery failed: " + mailError.getMessage() + "</p>");
                        }
                    } else {
                        out.println("<p style='color:gray;margin:5px 0 0 0;'>Notification already recorded as sent.</p>");
                    }
                    out.println("</div>");
                }
            }

            if (!found) {
                out.println("<div class='box' style='text-align:center;'>No expiry notifications at the moment!</div>");
            }

        } catch (Exception e) {
            out.println("<div class='box expired'><strong>System Error:</strong> " + e.getMessage() + "</div>");
            e.printStackTrace();
        } finally {
            if (rs != null) try { rs.close(); } catch (Exception ignored) {}
            if (ps != null) try { ps.close(); } catch (Exception ignored) {}
            if (con != null) try { con.close(); } catch (Exception ignored) {}
        }

        out.println("<a href='dashboard.html'>&larr; Back to Dashboard</a>");
        out.println("</body></html>");
    }

    private boolean isAlreadySent(Connection con, int medicineId, String type) throws SQLException {
        String sql = "SELECT COUNT(*) FROM expiry_notifications WHERE medicine_id = ? AND notification_type = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, medicineId);
            ps.setString(2, type);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    private void markAsSent(Connection con, int medicineId, String type) throws SQLException {
        String sql = "INSERT INTO expiry_notifications (medicine_id, notification_type) VALUES (?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, medicineId);
            ps.setString(2, type);
            ps.executeUpdate();
        }
    }

    private void sendExpiryEmail(Connection con, String medicineName, String expiry, String type) throws Exception {
        List<String> employeeEmails = new ArrayList<>();

        // Fetch all user emails from the database
        String emailQuery = "SELECT email FROM users";
        try (PreparedStatement psEmails = con.prepareStatement(emailQuery);
             ResultSet rsEmails = psEmails.executeQuery()) {
            while (rsEmails.next()) {
                String email = rsEmails.getString("email");
                if (email != null && !email.trim().isEmpty()) {
                    employeeEmails.add(email);
                }
            }
        }

        if (employeeEmails.isEmpty()) {
            throw new Exception("No registered employees found in the database to receive this email.");
        }

        Properties properties = new Properties();
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.port", "465");
        properties.put("mail.smtp.ssl.enable", "true");
        properties.put("mail.smtp.socketFactory.port", "465");
        properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");

        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SENDER_EMAIL, APP_PASSWORD);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(SENDER_EMAIL, "PharmaMate Alerts"));
        
        // Add all employees to the BCC field so they don't see each other's emails
        for (String email : employeeEmails) {
            message.addRecipient(Message.RecipientType.BCC, new InternetAddress(email));
        }

        String subject;
        String body;

        switch (type) {
            case "SIX_MONTHS":
                subject = "PharmaMate - Medicine Expiry in 6 Months";
                body = "Hello Team,\n\nMedicine Expiry Alert from PharmaMate.\n\n"
                     + "Medicine: " + medicineName + "\n"
                     + "Expiry Period: " + expiry + "\n\n"
                     + "This medicine will expire in approximately 6 months. Please audit your stock.\n\n"
                     + "Regards,\nPharmaMate Team";
                break;
            case "ONE_MONTH":
                subject = "URGENT: PharmaMate - Medicine Expiry in 1 Month";
                body = "Hello Team,\n\nMedicine Expiry Alert from PharmaMate.\n\n"
                     + "Medicine: " + medicineName + "\n"
                     + "Expiry Period: " + expiry + "\n\n"
                     + "This medicine will expire within 1 month. Plan usage or replacement immediately.\n\n"
                     + "Regards,\nPharmaMate Team";
                break;
            default:
                subject = "CRITICAL: PharmaMate - Medicine Expired";
                body = "Hello Team,\n\nCRITICAL ALERT from PharmaMate.\n\n"
                     + "Medicine: " + medicineName + "\n"
                     + "Expiry Period: " + expiry + "\n\n"
                     + "This medicine has EXPIRED. Please immediately discard or remove it from circulation.\n\n"
                     + "Regards,\nPharmaMate Team";
                break;
        }

        message.setSubject(subject);
        message.setText(body);
        Transport.send(message);
    }
}
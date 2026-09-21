import java.sql.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.mail.*;
import javax.mail.internet.*;

public class ExpiryScheduler implements ServletContextListener {

    private ScheduledExecutorService scheduler;

    private static final String SENDER_EMAIL = "nagamounikavadlamudi@gmail.com";
    private static final String APP_PASSWORD = "mizimlhpwtonrwqw";

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        scheduler = Executors.newSingleThreadScheduledExecutor();
        
        // This runs the check immediately when Tomcat starts, then repeats once every 24 hours
        scheduler.scheduleAtFixedRate(this::checkExpirations, 0, 24, TimeUnit.HOURS);
        System.out.println("PharmaMate Automated Email Scheduler Started.");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (scheduler != null) {
            scheduler.shutdownNow();
        }
    }

    private void checkExpirations() {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT id, name, expiry_date FROM medicines");
             ResultSet rs = ps.executeQuery()) {

            YearMonth currentMonth = YearMonth.now();
            LocalDate today = LocalDate.now();

            while (rs.next()) {
                int medicineId = rs.getInt("id");
                String medicineName = rs.getString("name");
                String expiry = rs.getString("expiry_date");

                if (expiry == null || expiry.trim().isEmpty() || expiry.length() < 7) continue;

                YearMonth expiryMonth = YearMonth.parse(expiry.substring(0, 7));
                LocalDate expiryDate = expiryMonth.atEndOfMonth();
                long monthsLeft = ChronoUnit.MONTHS.between(currentMonth, expiryMonth);

                String type = null;
                
                // Determine which email to send
                if (expiryDate.isBefore(today) || monthsLeft < 0) {
                    type = "EXPIRED";
                } else if (monthsLeft <= 1) {
                    type = "ONE_MONTH";
                } else if (monthsLeft == 6) {
                    type = "SIX_MONTHS";
                }

                // If it meets a condition and hasn't been sent yet, send the email
                if (type != null && !isAlreadySent(con, medicineId, type)) {
                    // Pass the database connection so the email method can look up users
                    sendEmail(con, medicineName, expiry, type);
                    markAsSent(con, medicineId, type);
                    System.out.println("Automated email successfully sent to all employees for: " + medicineName);
                }
            }
        } catch (Exception e) {
            System.err.println("Background Scheduler Error: " + e.getMessage());
        }
    }

    private boolean isAlreadySent(Connection con, int id, String type) throws SQLException {
        String sql = "SELECT COUNT(*) FROM expiry_notifications WHERE medicine_id = ? AND notification_type = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setString(2, type);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    private void markAsSent(Connection con, int id, String type) throws SQLException {
        String sql = "INSERT INTO expiry_notifications (medicine_id, notification_type) VALUES (?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setString(2, type);
            ps.executeUpdate();
        }
    }

    private void sendEmail(Connection con, String name, String expiry, String type) throws Exception {
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
            System.out.println("No registered employees found in the database. Email skipped.");
            return;
        }

        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SENDER_EMAIL, APP_PASSWORD);
            }
        });

        Message msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(SENDER_EMAIL, "PharmaMate Automation"));
        
        // BCC all registered employees
        for (String email : employeeEmails) {
            msg.addRecipient(Message.RecipientType.BCC, new InternetAddress(email));
        }

        String subject = "";
        String body = "Hello Team,\n\nMedicine Expiry Alert from PharmaMate.\n\nMedicine: " + name + "\nExpiry Period: " + expiry + "\n\n";

        if (type.equals("SIX_MONTHS")) {
            subject = "PharmaMate - Medicine Expiry in 6 Months";
            body += "This medicine will expire in approximately 6 months. Please audit your stock.";
        } else if (type.equals("ONE_MONTH")) {
            subject = "URGENT: PharmaMate - Medicine Expiry in 1 Month";
            body += "This medicine will expire within 1 month. Plan usage or replacement immediately.";
        } else {
            subject = "CRITICAL: PharmaMate - Medicine Expired";
            body += "This medicine has EXPIRED. Please immediately discard or remove it from circulation.";
        }

        body += "\n\nRegards,\nPharmaMate Team";

        msg.setSubject(subject);
        msg.setText(body);
        Transport.send(msg);
    }
}
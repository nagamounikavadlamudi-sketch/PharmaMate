import java.io.*;
import java.sql.*;
import java.util.Properties;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import javax.mail.*;
import javax.mail.internet.*;

public class RegisterServlet extends HttpServlet {

    // 1. Mee verified sender email (Brevo lo account create chesina email)
    private static final String SENDER_EMAIL = "nagamounikavadlamudi@gmail.com";
    
    // 2. Screenshot lo unna mee Brevo Login ID
    private static final String BREVO_LOGIN_ID = "b9ede4001@smtp-brevo.com"; 
    
    // 3. Ippudu generate chesina kotha SMTP key ikkada paste cheyandi (Quotes madhyalo)
    private static final String BREVO_SMTP_KEY = "xsmtpsib-c383c552291357d8550f4fe6302f31bea172ccf811aac393827b1929718dcd8f-SeycEO3PrtGqyvTc"; 

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String email = request.getParameter("userEmail");
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        if (email == null || username == null || password == null || email.trim().isEmpty()) {
            response.getWriter().println("All fields are required.");
            return;
        }

        try (Connection con = DBConnection.getConnection()) {
            // Ensure table exists
            String createTable = "CREATE TABLE IF NOT EXISTS users ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY, "
                    + "username VARCHAR(50) NOT NULL, "
                    + "email VARCHAR(100) NOT NULL UNIQUE, "
                    + "password VARCHAR(100) NOT NULL)";
            
            try (Statement stmt = con.createStatement()) {
                stmt.execute(createTable);
            }

            // Insert new user
            String sql = "INSERT INTO users (username, email, password) VALUES (?, ?, ?)";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, username);
                ps.setString(2, email);
                ps.setString(3, password);
                ps.executeUpdate();
            }

            // FAST FIX: Email ni background thread lo run chesthunnam
            final String finalEmail = email;
            final String finalUser = username;
            final String finalPass = password;
            
            new Thread(() -> {
                try {
                    sendWelcomeEmail(finalEmail, finalUser, finalPass);
                    System.out.println("Welcome email sent successfully to: " + finalEmail);
                } catch (Exception e) {
                    System.out.println("Welcome email failed: " + e.getMessage());
                    e.printStackTrace();
                }
            }).start();
            
            // Ventane login page ki redirect ayipothundi!
            response.sendRedirect("index.html");

        } catch (SQLIntegrityConstraintViolationException e) {
            response.getWriter().println("<html><body><h3>Error: That email is already registered.</h3><a href='register.html'>Try again</a></body></html>");
        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().println("Database Error: " + e.getMessage());
        }
    }

    private void sendWelcomeEmail(String toEmail, String user, String pass) throws Exception {
        Properties props = new Properties();
        
        // BREVO PORT 2525 SETTINGS
        props.put("mail.smtp.host", "smtp-relay.brevo.com");
        props.put("mail.smtp.port", "2525");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(BREVO_LOGIN_ID, BREVO_SMTP_KEY);
            }
        });

        Message msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(SENDER_EMAIL, "PharmaMate"));
        msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
        msg.setSubject("Welcome to PharmaMate!");
        msg.setText("Hello " + user + ",\n\nYour account has been successfully created.\n\n"
                  + "Username: " + user + "\nPassword: " + pass + "\n\n"
                  + "You can now log in to the PharmaMate dashboard.");
        Transport.send(msg);
    }
}
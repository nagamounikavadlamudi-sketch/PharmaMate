import java.io.*;
import java.sql.*;
import java.util.Properties;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import javax.mail.*;
import javax.mail.internet.*;

public class RegisterServlet extends HttpServlet {

    private static final String SENDER_EMAIL = "nagamounikavadlamudi@gmail.com";
    
    private static final String APP_PASSWORD = 
        (System.getenv("PHARMAMATE_EMAIL_PASSWORD") != null) 
            ? System.getenv("PHARMAMATE_EMAIL_PASSWORD") 
            : "mizimlhpwtonrwqw"; // <-- PUT YOUR 16-LETTER PASSWORD HERE

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
                // The DROP TABLE line has been removed from here!
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

            // Send confirmation email
            sendWelcomeEmail(email, username, password);
            
            // Redirect to login page on success
            response.sendRedirect("index.html");

        } catch (SQLIntegrityConstraintViolationException e) {
            response.getWriter().println("<html><body><h3>Error: That email is already registered.</h3><a href='register.html'>Try again</a></body></html>");
        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().println("Database or Email Error: " + e.getMessage());
        }
    }

    private void sendWelcomeEmail(String toEmail, String user, String pass) throws Exception {
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
        msg.setFrom(new InternetAddress(SENDER_EMAIL, "PharmaMate"));
        msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
        msg.setSubject("Welcome to PharmaMate!");
        msg.setText("Hello " + user + ",\n\nYour account has been successfully created.\n\n"
                  + "Username: " + user + "\nPassword: " + pass + "\n\n"
                  + "You can now log in to the PharmaMate dashboard.");
        Transport.send(msg);
    }
}
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class SendOTPServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        String email = request.getParameter("email");

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        // Check email
        if (email == null || email.trim().isEmpty()) {
            out.println("<h3>Please enter your email.</h3>");
            return;
        }

        email = email.trim();

        // Generate 6-digit OTP
        int otp = 100000 + (int)(Math.random() * 900000);

        // Email configuration
        String senderEmail = System.getenv("SMTP_USER");
        String senderPassword = System.getenv("SMTP_PASSWORD");

        if (senderEmail == null || senderPassword == null) {
            out.println("<h3>Email configuration is missing.</h3>");
            return;
        }

        try {

            Properties props = new Properties();

            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.port", "587");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");

            Session mailSession = Session.getInstance(
                props,
                new Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(
                            senderEmail,
                            senderPassword
                        );
                    }
                }
            );

            // Create email
            Message message = new MimeMessage(mailSession);

            message.setFrom(new InternetAddress(senderEmail));
            message.setRecipients(
                Message.RecipientType.TO,
                InternetAddress.parse(email)
            );

            message.setSubject("PharmaMate - Email Verification OTP");

            message.setText(
                "Hello,\n\n" +
                "Your PharmaMate verification OTP is: " + otp + "\n\n" +
                "This OTP is valid for 5 minutes.\n\n" +
                "Please do not share this OTP with anyone.\n\n" +
                "Regards,\n" +
                "PharmaMate Team"
            );

            // Send email
            Transport.send(message);

            // Store OTP in session
            HttpSession session = request.getSession();

            session.setAttribute("otp", String.valueOf(otp));
            session.setAttribute("otpEmail", email);
            session.setAttribute(
                "otpTime",
                System.currentTimeMillis()
            );

            out.println("<h2>OTP sent successfully!</h2>");
            out.println("<p>Please check your email.</p>");

        } catch (Exception e) {

            out.println("<h3>Unable to send OTP.</h3>");
            out.println("<p>Please try again later.</p>");

            e.printStackTrace();
        }
    }
}

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class SendOTPServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        String email = request.getParameter("email");

        if (email == null || email.trim().isEmpty()) {
            response.setContentType("text/html");
            response.getWriter().println("<h3>Please enter your email.</h3>");
            return;
        }

        // Generate 6-digit OTP
        int otp = 100000 + (int)(Math.random() * 900000);

        try {
            String smtpUser = System.getenv("SMTP_USER");
            String smtpPassword = System.getenv("SMTP_PASSWORD");

            Properties props = new Properties();
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.port", "587");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");

            Session mailSession = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(smtpUser, smtpPassword);
                    }
                });

            Message message = new MimeMessage(mailSession);

            message.setFrom(new InternetAddress(smtpUser));
            message.setRecipients(
                Message.RecipientType.TO,
                InternetAddress.parse(email)
            );

            message.setSubject("PharmaMate - Email Verification OTP");

            message.setText(
                "Your PharmaMate verification OTP is: " + otp +
                "\n\nThis OTP is valid for a short period." +
                "\n\nDo not share this OTP with anyone."
            );

            Transport.send(message);

            // Store email and OTP in session
            HttpSession session = request.getSession();
            session.setAttribute("registrationEmail", email);
            session.setAttribute("registrationOTP", String.valueOf(otp));

            response.sendRedirect(
                request.getContextPath() + "/verify-otp.html"
            );

        } catch (Exception e) {

            response.setContentType("text/html");

            PrintWriter out = response.getWriter();

            out.println("<h2>Unable to send OTP</h2>");
            out.println("<p>" + e.getMessage() + "</p>");
        }
    }
}

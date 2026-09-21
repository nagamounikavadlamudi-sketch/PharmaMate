import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;

public class TestMailServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html");

        final String senderEmail = "nagamounikavadlamudi@gmail.com";
        final String appPassword = "gdbzminmhelfwebq";

        String receiverEmail = "usanjupriya@gmail.com";

        Properties properties = new Properties();

        properties.put("mail.smtp.host", "smtp.gmail.com");
properties.put("mail.smtp.port", "587");
properties.put("mail.smtp.auth", "true");
properties.put("mail.smtp.starttls.enable", "true");
properties.put("mail.smtp.starttls.required", "true");

        Session session = Session.getInstance(properties,
            new Authenticator() {

                protected PasswordAuthentication
                getPasswordAuthentication() {

                    return new PasswordAuthentication(
                        senderEmail,
                        appPassword
                    );
                }
            });

        try {

            Message message = new MimeMessage(session);

            message.setFrom(
                new InternetAddress(senderEmail)
            );

            message.setRecipients(
                Message.RecipientType.TO,
                InternetAddress.parse(receiverEmail)
            );

            message.setSubject("PharmaMate Test Email");

            message.setText(
                "Hello! PharmaMate email notification is working successfully."
            );

            Transport.send(message);

            response.getWriter().println(
                "<h2>Email sent successfully!</h2>"
            );

        } catch (Exception e) {

            response.getWriter().println(
                "<h2>Error: " + e.getMessage() + "</h2>"
            );

            e.printStackTrace();
        }
    }
}
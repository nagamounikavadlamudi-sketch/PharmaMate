import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class ProfileServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/login.html");
            return;
        }

        String userId = (String) session.getAttribute("userId");
        String name = (String) session.getAttribute("name");
        String role = (String) session.getAttribute("role");
        String email = (String) session.getAttribute("email");

        PrintWriter out = response.getWriter();

        out.println("<!DOCTYPE html>");
        out.println("<html>");
        out.println("<head>");
        out.println("<meta charset='UTF-8'>");
        out.println("<title>PharmaMate - Profile</title>");

        out.println("<style>");
        out.println("body{font-family:Arial;background:#f4f7f6;padding:40px;}");
        out.println(".profile{width:500px;margin:auto;background:white;padding:30px;border-radius:12px;box-shadow:0 4px 12px #ccc;}");
        out.println("h1{text-align:center;}");
        out.println(".info{padding:15px;border-bottom:1px solid #ddd;font-size:18px;}");
        out.println(".label{font-weight:bold;}");
        out.println(".back{display:block;text-align:center;margin-top:25px;text-decoration:none;color:#333;}");
        out.println("</style>");

        out.println("</head>");
        out.println("<body>");

        out.println("<div class='profile'>");

        out.println("<h1>👤 Profile</h1>");

        out.println("<div class='info'><span class='label'>Name:</span> " + name + "</div>");
        out.println("<div class='info'><span class='label'>User ID:</span> " + userId + "</div>");
        out.println("<div class='info'><span class='label'>Role:</span> " + role + "</div>");
        out.println("<div class='info'><span class='label'>Email:</span> " + email + "</div>");

        out.println("<a href='dashboard.html' class='back'> Back to Dashboard</a>");

        out.println("</div>");
        out.println("</body>");
        out.println("</html>");
    }
}
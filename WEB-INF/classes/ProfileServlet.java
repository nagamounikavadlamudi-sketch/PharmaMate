import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class ProfileServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");
        HttpSession session = request.getSession(false);

        // Security check
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/index.html");
            return;
        }

        String username = (String) session.getAttribute("username");
        if (username == null) username = "Pharma User";
        
        String email = (String) session.getAttribute("email");
        if (email == null) email = "Not Available";

        PrintWriter out = response.getWriter();

        out.println("<!DOCTYPE html>");
        out.println("<html>");
        out.println("<head>");
        out.println("<meta charset='UTF-8'>");
        out.println("<title>PharmaMate - Profile</title>");

        out.println("<style>");
        out.println("body { font-family: Arial, sans-serif; background: #e8f5ff; display: flex; justify-content: center; align-items: center; min-height: 100vh; margin: 0; }");
        out.println(".profile-card { background: white; padding: 40px 50px; border-radius: 15px; box-shadow: 0 10px 30px rgba(0,0,0,0.1); width: 400px; }");
        out.println("h2 { text-align: center; color: #555; font-size: 26px; margin-bottom: 30px; }");
        out.println(".field { border-bottom: 1px dashed #ccc; padding: 18px 0; font-size: 16px; color: #333; }");
        out.println(".field:last-of-type { border-bottom: none; }");
        out.println(".field b { font-weight: bold; color: #111; display: inline-block; width: 100px; }");
        out.println(".back-link { display: block; text-align: center; margin-top: 30px; text-decoration: none; color: #666; font-size: 14px; }");
        out.println(".back-link:hover { color: #333; text-decoration: underline; }");
        out.println("</style>");

        out.println("</head>");
        out.println("<body>");

        out.println("<div class='profile-card'>");
        out.println("<h2>Profile</h2>");

        // Displaying only the clean, relevant user details
        out.println("<div class='field'><b>Username:</b> " + username + "</div>");
        out.println("<div class='field'><b>Role:</b> Pharmacy Employee</div>");
        out.println("<div class='field'><b>Email:</b> " + email + "</div>");

        out.println("<a href='dashboard.html' class='back-link'>&larr; Back to Dashboard</a>");

        out.println("</div>");
        out.println("</body>");
        out.println("</html>");
    }
}
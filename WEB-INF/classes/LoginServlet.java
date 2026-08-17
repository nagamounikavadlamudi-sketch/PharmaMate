import java.io.*;
import java.sql.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class LoginServlet extends HttpServlet {
protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
    response.sendRedirect(request.getContextPath() + "/index.html");
}
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
                          throws ServletException, IOException {

        String id = request.getParameter("id");
        String password = request.getParameter("password");

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        try {
            Connection con = DBConnection.getConnection();

            String sql = "SELECT * FROM users WHERE id=? AND password=?";
            PreparedStatement ps = con.prepareStatement(sql);

            ps.setString(1, id);
            ps.setString(2, password);

            ResultSet rs = ps.executeQuery();

               if (rs.next()) {
HttpSession session = request.getSession();

    session.setAttribute("userId", rs.getString("id"));
    session.setAttribute("name", rs.getString("name"));
    session.setAttribute("role", rs.getString("role"));
    session.setAttribute("email", rs.getString("email"));

response.sendRedirect(request.getContextPath() + "/dashboard.html");
    }           else {
                out.println("<h2>Invalid ID or Password</h2>");
            }

            con.close();

        } catch (Exception e) {
            out.println("<h2>Database Error</h2>");
            out.println("<p>" + e.getMessage() + "</p>");
        }
    }
}

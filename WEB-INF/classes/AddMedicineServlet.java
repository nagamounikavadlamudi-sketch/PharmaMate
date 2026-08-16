import java.io.*;
import java.sql.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class AddMedicineServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String name = request.getParameter("name");
        String category = request.getParameter("category");
        String quantity = request.getParameter("quantity");
        String price = request.getParameter("price");
        String expiry = request.getParameter("expiry");

        response.setContentType("text/html;charset=UTF-8");

        try {

            Connection con = DBConnection.getConnection();

            String sql =
                "INSERT INTO medicines " +
                "(name, category, quantity, price, expiry_date) " +
                "VALUES (?, ?, ?, ?, ?)";

            PreparedStatement ps = con.prepareStatement(sql);

            ps.setString(1, name);
            ps.setString(2, category);
            ps.setInt(3, Integer.parseInt(quantity));
            ps.setDouble(4, Double.parseDouble(price));
            ps.setString(5, expiry);

            ps.executeUpdate();

            ps.close();
            con.close();

            // After adding medicine, go to dynamic medicines servlet
            response.sendRedirect(request.getContextPath() + "/medicines");

        } catch (Exception e) {

            PrintWriter out = response.getWriter();

            out.println("<html>");
            out.println("<head>");
            out.println("<title>PharmaMate - Error</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h2>Unable to Add Medicine</h2>");
            out.println("<p>" + e.getMessage() + "</p>");
            out.println("<a href='add-medicine.html'>Back to Add Medicine</a>");
            out.println("</body>");
            out.println("</html>");

            e.printStackTrace();
        }
    }
}
import java.io.*;
import java.sql.*;
import java.time.YearMonth;
import javax.servlet.*;
import javax.servlet.http.*;

public class InventoryServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        try {

            Connection con = DBConnection.getConnection();

            String sql = "SELECT name, quantity, expiry_date FROM medicines ORDER BY name";

            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<meta charset='UTF-8'>");
            out.println("<title>PharmaMate - Inventory</title>");

            out.println("<style>");

            out.println("body {");
            out.println("margin: 0;");
            out.println("font-family: Arial, sans-serif;");
            out.println("background: #f4f7f6;");
            out.println("}");

            out.println(".container {");
            out.println("width: 90%;");
            out.println("max-width: 1000px;");
            out.println("margin: 40px auto;");
            out.println("}");

            out.println(".header {");
            out.println("text-align: center;");
            out.println("margin-bottom: 30px;");
            out.println("}");

            out.println(".header h1 {");
            out.println("margin-bottom: 8px;");
            out.println("}");

            out.println(".header p {");
            out.println("color: #666;");
            out.println("}");

            out.println(".inventory-box {");
            out.println("background: white;");
            out.println("padding: 20px;");
            out.println("border-radius: 12px;");
            out.println("box-shadow: 0 4px 15px rgba(0,0,0,0.1);");
            out.println("overflow-x: auto;");
            out.println("}");

            out.println("table {");
            out.println("width: 100%;");
            out.println("border-collapse: collapse;");
            out.println("}");

            out.println("th, td {");
            out.println("padding: 14px;");
            out.println("text-align: center;");
            out.println("border-bottom: 1px solid #ddd;");
            out.println("}");

            out.println("th {");
            out.println("background: #e8f3f0;");
            out.println("}");

            out.println(".status {");
            out.println("padding: 6px 12px;");
            out.println("border-radius: 15px;");
            out.println("}");

            out.println(".in-stock {");
            out.println("background: #e8f3f0;");
            out.println("}");

            out.println(".low-stock {");
            out.println("background: #ffe5e5;");
            out.println("}");

            out.println(".expiring {");
            out.println("background: #fff3cd;");
            out.println("}");

            out.println(".expired {");
            out.println("background: #ffcccc;");
            out.println("}");

            out.println(".back {");
            out.println("display: block;");
            out.println("text-align: center;");
            out.println("margin-top: 25px;");
            out.println("color: #333;");
            out.println("text-decoration: none;");
            out.println("}");

            out.println("</style>");
            out.println("</head>");

            out.println("<body>");

            out.println("<div class='container'>");

            out.println("<div class='header'>");
            out.println("<h1>Inventory</h1>");
            out.println("<p>Track your pharmacy stock</p>");
            out.println("</div>");

            out.println("<div class='inventory-box'>");

            out.println("<table>");

            out.println("<tr>");
            out.println("<th>Medicine</th>");
            out.println("<th>Available Stock</th>");
            out.println("<th>Expiry Date</th>");
            out.println("<th>Status</th>");
            out.println("</tr>");

            while (rs.next()) {

                String medicineName = rs.getString("name");
                int quantity = rs.getInt("quantity");
                String expiryDate = rs.getString("expiry_date");

                YearMonth expiry = YearMonth.parse(expiryDate);
                YearMonth currentMonth = YearMonth.now();
                YearMonth oneMonthLater = currentMonth.plusMonths(1);

                out.println("<tr>");

                out.println("<td>" + medicineName + "</td>");
                out.println("<td>" + quantity + "</td>");
                out.println("<td>" + expiryDate + "</td>");

                /*
                 * STATUS PRIORITY:
                 * 1. Expired
                 * 2. Expiring Soon
                 * 3. Low Stock
                 * 4. In Stock
                 */

                if (expiry.isBefore(currentMonth)) {

                    out.println("<td>");
                    out.println("<span class='status expired'>Expired</span>");
                    out.println("</td>");

                } else if (!expiry.isAfter(oneMonthLater)) {

                    out.println("<td>");
                    out.println("<span class='status expiring'>Expiring Soon</span>");
                    out.println("</td>");

                } else if (quantity <= 20) {

                    out.println("<td>");
                    out.println("<span class='status low-stock'>Low Stock</span>");
                    out.println("</td>");

                } else {

                    out.println("<td>");
                    out.println("<span class='status in-stock'>In Stock</span>");
                    out.println("</td>");
                }

                out.println("</tr>");
            }

            out.println("</table>");

            out.println("</div>");

            out.println("<a href='dashboard.html' class='back'>Back to Dashboard</a>");

            out.println("</div>");

            out.println("</body>");
            out.println("</html>");

            rs.close();
            ps.close();
            con.close();

        } catch (Exception e) {

            out.println("<h2>Database Error</h2>");
            out.println("<p>" + e.getMessage() + "</p>");

        }
    }
}
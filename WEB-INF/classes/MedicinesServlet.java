import java.io.*;
import java.sql.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class MedicinesServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");

        PrintWriter out = response.getWriter();

        String contextPath = request.getContextPath();

        String search = request.getParameter("search");

        if (search == null) {
            search = "";
        }

        out.println("<!DOCTYPE html>");
        out.println("<html>");
        out.println("<head>");

        out.println("<meta charset='UTF-8'>");
        out.println("<title>PharmaMate - Medicines</title>");

        out.println("<style>");

        out.println("body {");
        out.println("font-family: Arial, sans-serif;");
        out.println("background: #f4f7f6;");
        out.println("margin: 0;");
        out.println("padding: 40px;");
        out.println("}");

        out.println(".container {");
        out.println("width: 90%;");
        out.println("max-width: 1100px;");
        out.println("margin: auto;");
        out.println("}");

        out.println("h1 {");
        out.println("text-align: center;");
        out.println("margin-bottom: 30px;");
        out.println("}");

        out.println(".search-box {");
        out.println("text-align: center;");
        out.println("margin-bottom: 25px;");
        out.println("}");

        out.println(".search-box input {");
        out.println("width: 300px;");
        out.println("padding: 12px;");
        out.println("border: 1px solid #ccc;");
        out.println("border-radius: 6px;");
        out.println("font-size: 15px;");
        out.println("}");

        out.println(".search-box button {");
        out.println("padding: 12px 20px;");
        out.println("margin-left: 5px;");
        out.println("background: #333;");
        out.println("color: white;");
        out.println("border: none;");
        out.println("border-radius: 6px;");
        out.println("cursor: pointer;");
        out.println("}");

        out.println(".search-box button:hover {");
        out.println("background: #555;");
        out.println("}");

        out.println(".table-box {");
        out.println("background: white;");
        out.println("padding: 20px;");
        out.println("border-radius: 12px;");
        out.println("box-shadow: 0 4px 12px #ccc;");
        out.println("}");

        out.println("table {");
        out.println("width: 100%;");
        out.println("border-collapse: collapse;");
        out.println("}");

        out.println("th, td {");
        out.println("padding: 14px;");
        out.println("text-align: center;");
        out.println("border: 1px solid #ddd;");
        out.println("}");

        out.println("th {");
        out.println("background: #e8f3f0;");
        out.println("}");

        out.println(".add-btn {");
        out.println("display: inline-block;");
        out.println("padding: 14px 28px;");
        out.println("background: #333;");
        out.println("color: white;");
        out.println("text-decoration: none;");
        out.println("border-radius: 8px;");
        out.println("font-size: 16px;");
        out.println("}");

        out.println(".add-btn:hover {");
        out.println("background: #555;");
        out.println("}");

        out.println(".delete-btn {");
        out.println("padding: 8px 14px;");
        out.println("background: #b33;");
        out.println("color: white;");
        out.println("text-decoration: none;");
        out.println("border-radius: 5px;");
        out.println("}");

        out.println(".delete-btn:hover {");
        out.println("background: #800;");
        out.println("}");

        out.println(".back {");
        out.println("display: block;");
        out.println("text-align: center;");
        out.println("margin-top: 20px;");
        out.println("color: #333;");
        out.println("text-decoration: none;");
        out.println("}");

        out.println("</style>");

        out.println("</head>");

        out.println("<body>");

        out.println("<div class='container'>");

        out.println("<h1>Medicines</h1>");

        // SEARCH BAR
        out.println("<div class='search-box'>");

        out.println("<form method='get' action='" +
                    contextPath + "/medicines'>");

        out.println("<input type='text' name='search' " +
                    "placeholder='Search medicine name...' " +
                    "value='" + search + "'>");

        out.println("<button type='submit'>Search</button>");

        out.println("</form>");

        out.println("</div>");

        out.println("<div class='table-box'>");

        out.println("<table>");

        out.println("<thead>");
        out.println("<tr>");
        out.println("<th>Medicine Name</th>");
        out.println("<th>Category</th>");
        out.println("<th>Quantity</th>");
        out.println("<th>Price</th>");
        out.println("<th>Expiry Date</th>");
        out.println("<th>Action</th>");
        out.println("</tr>");
        out.println("</thead>");

        out.println("<tbody>");

        try {

            Connection con = DBConnection.getConnection();

            String sql;

            PreparedStatement ps;

            if (search.trim().isEmpty()) {

                sql = "SELECT id, name, category, quantity, price, expiry_date " +
                      "FROM medicines";

                ps = con.prepareStatement(sql);

            } else {

                sql = "SELECT id, name, category, quantity, price, expiry_date " +
                      "FROM medicines WHERE name LIKE ?";

                ps = con.prepareStatement(sql);

                ps.setString(1, "%" + search.trim() + "%");
            }

            ResultSet rs = ps.executeQuery();

            boolean found = false;

            while (rs.next()) {

                found = true;

                int id = rs.getInt("id");

                out.println("<tr>");

                out.println("<td>");
                out.println(rs.getString("name"));
                out.println("</td>");

                out.println("<td>");
                out.println(rs.getString("category"));
                out.println("</td>");

                out.println("<td>");
                out.println(rs.getInt("quantity"));
                out.println("</td>");

                out.println("<td>");
                out.println(rs.getDouble("price"));
                out.println("</td>");

                out.println("<td>");
                out.println(rs.getString("expiry_date"));
                out.println("</td>");

                // DELETE BUTTON
                out.println("<td>");

                out.println("<a class='delete-btn' " +
                        "href='" + contextPath +
                        "/deleteMedicine?id=" + id +
                        "' onclick=\"return confirm('Are you sure you want to delete this medicine?');\">");

                out.println("Delete");

                out.println("</a>");

                out.println("</td>");

                out.println("</tr>");
            }

            if (!found) {

                out.println("<tr>");

                out.println("<td colspan='6'>");
                out.println("No medicines found.");
                out.println("</td>");

                out.println("</tr>");
            }

            rs.close();
            ps.close();
            con.close();

        } catch (Exception e) {

            out.println("<tr>");

            out.println("<td colspan='6'>");

            out.println("Database Error: " + e.getMessage());

            out.println("</td>");

            out.println("</tr>");
        }

        out.println("</tbody>");

        out.println("</table>");

        out.println("</div>");

        // ADD MEDICINE BUTTON
        out.println("<div style='text-align:center; margin:30px;'>");

        out.println("<a href='" + contextPath +
                    "/add-medicine.html' class='add-btn'>");

        out.println("+ Add Medicine");

        out.println("</a>");

        out.println("</div>");

        // BACK TO DASHBOARD
        out.println("<a href='" + contextPath +
                    "/dashboard.html' class='back'>");

        out.println("Back to Dashboard");

        out.println("</a>");

        out.println("</div>");

        out.println("</body>");

        out.println("</html>");
    }
}
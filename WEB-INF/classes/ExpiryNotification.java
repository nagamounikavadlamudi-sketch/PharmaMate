import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class ExpiryNotification {

    public static void main(String[] args) {

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            Connection con = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/pharmamate",
                "root",
                ""
            );

            String query = "SELECT medicine_name, expiry_date FROM medicines";

            PreparedStatement ps = con.prepareStatement(query);
            ResultSet rs = ps.executeQuery();

            LocalDate today = LocalDate.now();

            while (rs.next()) {

                String medicineName = rs.getString("medicine_name");
                LocalDate expiryDate = rs.getDate("expiry_date").toLocalDate();

                long daysLeft = ChronoUnit.DAYS.between(today, expiryDate);

                if (daysLeft < 0) {
                    System.out.println("EXPIRED: " + medicineName);
                }
                else if (daysLeft <= 30) {
                    System.out.println("ALERT: " + medicineName +
                            " will expire within 1 month!");
                }
                else if (daysLeft <= 183) {
                    System.out.println("REMINDER: " + medicineName +
                            " will expire within 6 months!");
                }
            }

            con.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
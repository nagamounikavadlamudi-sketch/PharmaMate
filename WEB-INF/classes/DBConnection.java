import java.sql.*;

public class DBConnection {

    public static Connection getConnection() throws Exception {
Class.forName("com.mysql.cj.jdbc.Driver");
        return DriverManager.getConnection(
            "jdbc:mysql://localhost:3306/PharmaMate",
            "root",
            "Mounika@02"
        );
    }
}
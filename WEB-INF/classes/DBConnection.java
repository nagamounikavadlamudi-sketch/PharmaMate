import java.sql.Connection;
import java.sql.DriverManager;

public class DBConnection {

    public static Connection getConnection() throws Exception {

        Class.forName("com.mysql.cj.jdbc.Driver");

        String url = "jdbc:mysql://localhost:3306/pharmamate";
        String user = "root";
        String password = "Mounika@02";

        return DriverManager.getConnection(url, user, password);
    }
}
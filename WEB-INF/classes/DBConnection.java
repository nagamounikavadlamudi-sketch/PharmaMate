import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    public static Connection getConnection() throws Exception {
        // Load the MySQL/TiDB driver
        Class.forName("com.mysql.cj.jdbc.Driver");

        // Pull the credentials from your Render environment variables
        String url = System.getenv("DB_URL");
        String user = System.getenv("DB_USER");
        String password = System.getenv("DB_PASSWORD");

        // Fallback for local testing on your laptop
        if (url == null || url.trim().isEmpty()) {
            url = "jdbc:mysql://localhost:3306/pharmamate";
            user = "root";
            password = "Mounika@02"; 
        }

        // Establish and return the connection
        return DriverManager.getConnection(url, user, password);
    }
}
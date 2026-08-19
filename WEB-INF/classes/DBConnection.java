import java.sql.Connection;
import java.sql.DriverManager;

public class DBConnection {
    public static Connection getConnection() throws Exception {
        Class.forName("com.mysql.cj.jdbc.Driver");

        String url = "jdbc:mysql://gateway01.ap-southeast-1.prod.aws.tidbcloud.com:4000/pharmamate?useSSL=true&sslMode=PREFERRED&allowPublicKeyRetrieval=true";
        String user = "YOUR_TIDB_USERNAME_HERE";
        String password = "YOUR_TIDB_PASSWORD_HERE";

        return DriverManager.getConnection(url, user, password);
    }
}

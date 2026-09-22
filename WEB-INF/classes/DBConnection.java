import java.sql.Connection;
import java.sql.DriverManager;

public class DBConnection {
    public static Connection getConnection() throws Exception {
        Class.forName("com.mysql.cj.jdbc.Driver");
        String url = "jdbc:mysql://gateway01.ap-southeast-1.prod.aws.tidbcloud.com:4000/pharmamate?sslMode=REQUIRED&tlsVersions=TLSv1.2";
        String user = "3ZkjQeCyhScxtC3.root"; // e.g., 2a3b4c5d.root
        String password = "j4GlrWjwqeOt2a11";
        return DriverManager.getConnection(url, user, password);
    }
}
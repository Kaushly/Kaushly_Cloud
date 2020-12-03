import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DataBaseConfig {
    private static DataBaseConfig dbConnectImpl;

    static {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Driver not found.");
        }
    }

    public static Connection getConnection() {
        try {
            return DriverManager.getConnection("jdbc:postgresql://localhost:5432/UsersOfCloud", "postgres", "postgres");
        } catch (SQLException e) {
            throw new RuntimeException("Driver registration error.");
        }
    }

    public static DataBaseConfig getInstance() {
        if (dbConnectImpl == null) {
            dbConnectImpl = new DataBaseConfig();
        }
        return dbConnectImpl;
    }
}

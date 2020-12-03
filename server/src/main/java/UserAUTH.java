import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserAUTH {

    PreparedStatement ps;

    public boolean checkUser(String login, String password) throws SQLException {
        ResultSet rs = null;
        try {
            try {
                String select = String.format("SELECT name, password FROM users WHERE (name = '%s' AND password ='%s')", login, password);
                ps = DataBaseConfig.getConnection()
                        .prepareStatement(select);
                rs = ps.executeQuery();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            try {
                return rs.next();
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        } finally {
            ps.close();
        }
    }

    public boolean registerUser(String login, String password) throws SQLException {
        ResultSet rs;
        try {
            try {
                String select = String.format("SELECT name, password FROM users WHERE (name = '%s' AND password ='%s')", login, password);
                ps = DataBaseConfig.getConnection()
                        .prepareStatement(select);
                rs = ps.executeQuery();
                if (rs.next()) {
                    return false;
                }
                String sql = String.format("INSERT INTO users (name, password) VALUES ('%s', '%s')", login, password);
                ps = DataBaseConfig.getConnection()
                        .prepareStatement(sql);
                return ps.execute();
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        } finally {
            ps.close();
        }
    }
}

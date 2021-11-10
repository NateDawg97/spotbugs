package ghIssues;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Issue1293 {
    public String controlTestConnection(String url, String username, String password) throws Exception {
        Connection connection = DriverManager.getConnection(url, username, password);
        PreparedStatement pstmt = connection.prepareStatement("SELECT count(1) from tab");
        ResultSet rs = pstmt.executeQuery();
        String myResult = "NO RESULT";

        while (rs.next()) {
            myResult = rs.getString(1);
        }

        return myResult;
    }

    public String abstractedTestConnection(String url, String username, String password) throws Exception {
        Connection connection = getConnection(url, username, password);    // not detected
        PreparedStatement pstmt = getStatement(connection); // not detected
        ResultSet rs = pstmt.executeQuery();
        String myResult = "NO RESULT";

        while (rs.next()) {
            myResult = rs.getString(1);
        }

        return myResult;
    }

    private ResultSet getResultSet(Statement statement) throws SQLException {
        return statement.executeQuery("select * from emp");
    }

    private PreparedStatement getStatement(Connection connection) throws SQLException {
        return connection.prepareStatement("SELECT count(1) from tab");
    }

    private Connection getConnection(String url, String username, String password) throws SQLException{
        return DriverManager.getConnection(url, username, password);
    }

    private void getConnectionToo(String url, String username, String password) throws SQLException{
        Connection connection = DriverManager.getConnection(url, username, password);
        System.out.println(connection);
    }

    private Connection getConnectionTree(String url, String username, String password) throws SQLException{
        Connection connection = DriverManager.getConnection(url, username, password);
        return connection;
    }
}

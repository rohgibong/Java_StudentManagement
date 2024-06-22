package kr.co.ync.project.dao.factory;

import kr.co.ync.project.dao.MemberDao;
import kr.co.ync.project.dao.MemberDaoImpl;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Mysql extends DaoFactory{
    private static String URL = "jdbc:mysql://127.0.0.1:3306/";
    private static String DATABASE = "db_java";
    private static String DRIVER = "com.mysql.cj.jdbc.Driver";
    private static String USER = "javauser";
    private static String PASSWORD = "Javauser123!@#";

    @Override
    public Connection openConnection() {
        try {
            Class.forName(DRIVER).newInstance();
            Connection connection = DriverManager.getConnection(URL + DATABASE, USER, PASSWORD);
            return connection;
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception ex){
        }
        return null;
    }

    @Override
    public MemberDao getMemberDao() {
        return new MemberDaoImpl();
    }
}

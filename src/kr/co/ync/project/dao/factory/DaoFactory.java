package kr.co.ync.project.dao.factory;

import kr.co.ync.project.dao.MemberDao;

import java.sql.Connection;

// ID : javauser
// PW : Javauser123!@#

public abstract class DaoFactory {
    public abstract Connection openConnection();

    public abstract MemberDao getMemberDao();

    public static DaoFactory getDatabase() {
        return new Mysql();
    }
}

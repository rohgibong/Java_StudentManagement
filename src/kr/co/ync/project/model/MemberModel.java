package kr.co.ync.project.model;

import kr.co.ync.project.dao.MemberDao;
import kr.co.ync.project.dao.factory.DaoFactory;

import java.sql.SQLException;
import java.util.List;

public class MemberModel {
    private static final MemberModel instance = new MemberModel();

    public MemberModel() {

    }

    public static MemberModel getInstance() {
        return instance;
    }

    public List<Member> allMembers() throws SQLException {
        return memberDao().all();
    }

    public void register(Member member) throws SQLException {
        memberDao().insert(member);
    }

    private static MemberDao memberDao() {
        return DaoFactory.getDatabase().getMemberDao();
    }

    public void update(Member member) throws SQLException {
        memberDao().update(member);
    }

    public void delete(Long memberId) throws SQLException {
        memberDao().delete(memberId);
    }

    public boolean checkDuplicate(String type, String value, Long memberId) throws SQLException {
        String sql = "";
        if ("email".equals(type)) {
            sql = "SELECT id FROM tb_members WHERE email = ?";
        } else if ("phone".equals(type)) {
            sql = "SELECT id FROM tb_members WHERE phone = ?";
        }
        return memberDao().checkDuplicate(value, sql, memberId);
    }


}

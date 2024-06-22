package kr.co.ync.project.dao;

import kr.co.ync.project.dao.factory.DaoFactory;
import kr.co.ync.project.model.Member;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MemberDaoImpl implements MemberDao {

    private static final String ALL = "SELECT * FROM tb_members";
    private static final String INSERT = "INSERT INTO tb_members (email, name, phone, birth, reg_date) VALUES (?, ?, ?, ?, ?)";
    private static final String UPDATE = "UPDATE tb_members SET email = ?, name = ?, phone = ?, birth = ? WHERE id = ?";
    private static final String DELETE = "DELETE FROM tb_members WHERE id = ?";
    private static final String CHECK_EMAIL = "SELECT id FROM tb_members WHERE email = ?";
    private static final String CHECK_PHONE = "SELECT id FROM tb_members WHERE phone = ?";

    @Override
    public List<Member> all() throws SQLException {
        ArrayList<Member> members = new ArrayList<Member>();

        Connection connection = DaoFactory.getDatabase().openConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(ALL); // sql 문을 실행하기 위해
        ResultSet resultSet = preparedStatement.executeQuery(); // SELECT 문과 같은 쿼리문을 실행할 때 사용

        while (resultSet.next()) {
            members.add(createUser(resultSet));
        }

        resultSet.close();
        preparedStatement.close();
        connection.close();

        return members;
    }

    @Override
    public Member insert(Member member) throws SQLException {
        LocalDateTime now = LocalDateTime.now();
        Connection c = DaoFactory.getDatabase().openConnection();
        PreparedStatement pstmt = c.prepareStatement(INSERT, PreparedStatement.RETURN_GENERATED_KEYS); // 자동생성된 PK 값 리턴 여부

        // email, name, phone, birth, reg_date
        pstmt.setString(1, member.getEmail());
        pstmt.setString(2, member.getName());
        pstmt.setString(3, member.getPhone());
        pstmt.setDate(4, Date.valueOf(member.getBirth()));
        pstmt.setTimestamp(5, Timestamp.valueOf(now));

        pstmt.executeUpdate();

        ResultSet rset = pstmt.getGeneratedKeys();

        if (rset.next()) {
            Long idGenerated = rset.getLong(1);
            member.setId(idGenerated);
            member.setRegDate(now);
        }

        rset.close();
        pstmt.close();
        c.close();

        return member;
    }

    @Override
    public void update(Member member) throws SQLException {
        Connection c = DaoFactory.getDatabase().openConnection();
        PreparedStatement pstmt = c.prepareStatement(UPDATE, PreparedStatement.RETURN_GENERATED_KEYS);

        pstmt.setString(1, member.getEmail());  // email
        pstmt.setString(2, member.getName());   // name
        pstmt.setString(3, member.getPhone());  // phone
        pstmt.setDate(4, Date.valueOf(member.getBirth()));  // birth
        pstmt.setLong(5, member.getId());  // id

        pstmt.executeUpdate();
    }

    @Override
    public void delete(Long memberId) throws SQLException {
        Connection c = DaoFactory.getDatabase().openConnection();
        PreparedStatement pstmt = c.prepareStatement(DELETE);

        pstmt.setLong(1, memberId);
        pstmt.executeUpdate();
        pstmt.close();
        c.close();
    }

    public boolean checkDuplicate(String value, String sql, Long memberId) throws SQLException{
        Connection c = DaoFactory.getDatabase().openConnection();
        PreparedStatement pstmt = c.prepareStatement(sql);
        pstmt.setString(1, value);

        ResultSet rs = pstmt.executeQuery();
        boolean exists = rs.next();
        if (exists && (memberId == null || rs.getLong("id") != memberId)) {
            exists = true;
        } else {
            exists = false;
        }

        rs.close();
        pstmt.close();
        c.close();

        return exists;
    }


    private Member createUser(ResultSet resultSet) throws SQLException {
        Member member = new Member();
        member.setId(resultSet.getLong("id"));
        member.setEmail(resultSet.getString("email"));
        member.setName(resultSet.getString("name"));
        member.setPhone(resultSet.getString("phone"));
        member.setBirth(resultSet.getObject("birth", LocalDate.class));
        member.setRegDate(resultSet.getObject("reg_date", LocalDateTime.class));
        return member;
    }
}

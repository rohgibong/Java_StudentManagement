package kr.co.ync.project.dao;

import kr.co.ync.project.model.Member;

import java.sql.SQLException;
import java.util.List;

public interface MemberDao {
    List<Member> all() throws SQLException;

    Member insert(Member member) throws SQLException;

    void update(Member member) throws SQLException;

    void delete(Long memberId) throws SQLException;

    boolean checkDuplicate(String field, String value, Long id) throws SQLException;
}
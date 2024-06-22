package kr.co.ync.project.controller.member;

import kr.co.ync.project.controller.member.enums.MemberEventType;
import kr.co.ync.project.controller.member.listeners.MemberEvent;
import kr.co.ync.project.controller.member.listeners.MemberListener;
import kr.co.ync.project.model.Member;
import kr.co.ync.project.model.MemberModel;

import javax.swing.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MemberController {
    private List<MemberListener> memberListeners = new ArrayList<>();

    private static final MemberController instance = new MemberController();

    private MemberController() {

    }

    public static MemberController getInstance(){
        return instance;
    }

    public synchronized void addMemberListener(MemberListener memberListener){
        if (!memberListeners.contains(memberListener)) {
            memberListeners.add(memberListener);
        }
    }

    public List<Member> allMembers() throws SQLException{
        return MemberModel.getInstance().allMembers();
    }

    public Member save(Member member) throws SQLException {
        if (member != null) {
            if (MemberModel.getInstance().checkDuplicate("email", member.getEmail(), member.getId())) {
                JOptionPane.showMessageDialog(null, "이미 존재하는 이메일입니다.", "경고", JOptionPane.WARNING_MESSAGE);
                return null;
            }

            if (MemberModel.getInstance().checkDuplicate("phone", member.getPhone(), member.getId())) {
                JOptionPane.showMessageDialog(null, "이미 존재하는 전화번호입니다.", "경고", JOptionPane.WARNING_MESSAGE);
                return null;
            }

            try {
                MemberModel.getInstance().register(member);
                notifyListeners(new MemberEvent<>(member, MemberEventType.REGISTER));
            } catch (SQLException e) {

            }
        }
        return member;
    }

    private void notifyListeners(MemberEvent<Member> memberEvent) {
        memberListeners.forEach(listener -> {
            switch(memberEvent.getMemberEventType()){
                case REGISTER -> listener.register(memberEvent);
                case MODIFY -> listener.update(memberEvent);
                case REMOVE -> listener.delete(memberEvent);
            }
        });

    }

    public void update(Member member) {
        if (member != null) {
            try {
                MemberModel.getInstance().update(member); // 데이터베이스에서 업데이트 수행
                notifyListeners(new MemberEvent<>(member, MemberEventType.MODIFY));
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void delete(Long memberId) {
        if (memberId != null) {
            try {
                MemberModel.getInstance().delete(memberId);
                notifyListeners(new MemberEvent<>(memberId, MemberEventType.REMOVE));
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}

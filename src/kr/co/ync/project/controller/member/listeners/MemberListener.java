package kr.co.ync.project.controller.member.listeners;

public interface MemberListener {
    void register(MemberEvent memberEvent);
    void update(MemberEvent memberEvent);
    void delete(MemberEvent memberEvent);
}

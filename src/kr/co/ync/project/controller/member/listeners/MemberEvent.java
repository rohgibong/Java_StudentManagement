package kr.co.ync.project.controller.member.listeners;

import kr.co.ync.project.controller.member.enums.MemberEventType;

import java.util.EventObject;

public class MemberEvent<T> extends EventObject {
    private MemberEventType memberEventType;

    public MemberEvent(Object source, MemberEventType memberEventType) {
        super(source);
        this.memberEventType = memberEventType;
    }

    public T getSource(){
        return (T) super.getSource();
    }

    public MemberEventType getMemberEventType() {
        return this.memberEventType;
    }
}

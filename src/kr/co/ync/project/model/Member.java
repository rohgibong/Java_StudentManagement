package kr.co.ync.project.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Member {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private LocalDate birth;
    private LocalDateTime regDate;

    public Member() {

    }

    public Member(String email, String name, String phone, LocalDate birth, LocalDateTime regDate) {
        this.email = email;
        this.name = name;
        this.phone = phone;
        this.birth = birth;
        this.regDate = regDate;
    }

    public Member(String email, String name, String phone, LocalDate birth) {
        this(email, name, phone, birth, LocalDateTime.now());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public LocalDate getBirth() {
        return birth;
    }

    public void setBirth(LocalDate birth) {
        this.birth = birth;
    }

    public LocalDateTime getRegDate() {
        return regDate;
    }

    public void setRegDate(LocalDateTime regDate) {
        this.regDate = regDate;
    }

    public String[] toArray() {
        return new String[]{
                this.id.toString(), this.email, this.name, this.phone, this.birth.toString(), this.regDate.toString()
        };
    }
}

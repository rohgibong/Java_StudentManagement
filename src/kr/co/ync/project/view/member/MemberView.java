package kr.co.ync.project.view.member;

import kr.co.ync.project.controller.member.MemberController;
import kr.co.ync.project.controller.member.listeners.MemberEvent;
import kr.co.ync.project.controller.member.listeners.MemberListener;
import kr.co.ync.project.model.Member;
import kr.co.ync.project.util.Util;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.regex.Pattern;

public class MemberView extends JFrame implements MemberListener {

    //상수 SIZE - 창의 크기를 정의하는 상수
    public static final Dimension SIZE = new Dimension(1000, 500);
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);;
    private static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-Z가-힣\\s]+$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\d{3}-\\d{3,4}-\\d{4}$");


    private final String[] labelTexts = {"이메일", "이름", "전화번호", "생년월일"};

    private JTextField[] fields;

    private JButton regButton;

    private JButton updateButton;

    private JButton deleteButton;

    private DefaultTableModel defaultTableModel;

    private JTable jTable;


    //생성자 MemberView - 창의 제목을 설정
    public MemberView(String title) {
        super(title);
        JPanel jPanel = new JPanel(new GridLayout(1, 2));

        jPanel.add(createLeftPanel());
        jPanel.add(createRightPanel());
        add(jPanel);
        MemberController.getInstance().addMemberListener(this);
        registerListeners();
        updateListeners();
        deleteListeners();
        loadMembers();
    }

    private void registerListeners() {
        regButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try{
                    if (!validateFields()) {
                        return; // 필드 검증에 실패하면 작업 중단
                    }

                    Member member = new Member(
                            fields[0].getText(),
                            fields[1].getText(),
                            fields[2].getText(),
                            Util.strToLocalDate(fields[3].getText())
                    );
                    MemberController.getInstance().save(member);
                } catch(Exception exception){
                    exception.printStackTrace();
                    JOptionPane.showMessageDialog(null, "회원 정보를 저장하는 중 오류가 발생했습니다.", "오류", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }

    private boolean isValidName(String name) {
        return NAME_PATTERN.matcher(name).matches();
    }

    private boolean isValidEmail(String email) {
        return EMAIL_PATTERN.matcher(email).find();
    }

    private void updateListeners() {
        updateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    int row = jTable.getSelectedRow();
                    if (row < 0) {
                        JOptionPane.showMessageDialog(null, "수정할 회원을 선택하세요.", "경고", JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                    Long memberId = Long.parseLong(jTable.getValueAt(row, 0).toString());
                    if (memberId == null) {
                        JOptionPane.showMessageDialog(null, "유효한 회원을 선택하세요.", "경고", JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                    if (!validateFields()) {
                        return; // 필드 검증에 실패하면 작업 중단
                    }

                    Member member = new Member(
                            fields[0].getText(),
                            fields[1].getText(),
                            fields[2].getText(),
                            Util.strToLocalDate(fields[3].getText())
                    );

                    member.setId(memberId);

                    MemberController.getInstance().update(member);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(null, "회원 정보를 수정하는 중 오류가 발생했습니다.", "오류", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }

    private boolean validateFields() {
        // 필드 비어 있는지 확인
        if (fields[0].getText().isEmpty() || fields[1].getText().isEmpty() ||
                fields[2].getText().isEmpty() || fields[3].getText().isEmpty()) {
            JOptionPane.showMessageDialog(null, "모든 필드를 입력하세요.", "경고", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        // 이메일 형식 검증
        if (!isValidEmail(fields[0].getText())) {
            JOptionPane.showMessageDialog(null, "유효한 이메일 주소를 입력하세요.", "경고", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        // 이름 형식 검증
        if (!isValidName(fields[1].getText())) {
            JOptionPane.showMessageDialog(null, "이름에는 문자만 포함될 수 있습니다.", "경고", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        // 전화번호 형식 검증
        if (!isValidPhoneNumber(fields[2].getText())) {
            JOptionPane.showMessageDialog(null, "유효한 전화번호를 입력하세요. (예: 000-0000-0000 또는 000-000-0000)", "경고", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        // 날짜 형식 검증
        if (!isValidDate(fields[3].getText())) {
            JOptionPane.showMessageDialog(null, "유효한 날짜 형식을 입력하세요. (예: yyyy-MM-dd)", "경고", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        return true;
    }


    private boolean isValidPhoneNumber(String phone) {
        return PHONE_PATTERN.matcher(phone).matches();
    }

    private boolean isValidDate(String dateStr) {
        try {
            LocalDate.parse(dateStr);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    private void deleteListeners() {
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    int row = jTable.getSelectedRow();
                    if (row < 0) {
                        JOptionPane.showMessageDialog(null, "삭제할 회원을 선택하세요.", "경고", JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                    Long memberId = Long.parseLong(jTable.getValueAt(row, 0).toString());
                    if (memberId == null) {
                        JOptionPane.showMessageDialog(null, "유효한 회원을 선택하세요.", "경고", JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                    MemberController.getInstance().delete(memberId);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(null, "회원 정보를 삭제하는 중 오류가 발생했습니다.", "오류", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }

    private void loadMembers() {
        try {
            defaultTableModel.setRowCount(0);
            for (Member member:MemberController.getInstance().allMembers()){
                defaultTableModel.insertRow(0, member.toArray());
            }
        } catch (SQLException e) {

        }
    }

    private JPanel createRightPanel() {
        JPanel jPanel = new JPanel();
        jPanel.setBorder(
                BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("회원목록"),
                        BorderFactory.createEmptyBorder(5, 5, 5, 5)
                )
        );

        jPanel.setLayout(
                new BorderLayout()
        );

        JScrollPane jScrollPane = new JScrollPane();

        defaultTableModel = new DefaultTableModel(
                new String[]{
                        "No", "이메일", "이름", "전화번호", "생년월일", "가입일"
                }, 0
        );
        jTable = new JTable(defaultTableModel);
        jTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        jScrollPane.setViewportView(jTable);
        jScrollPane.setBorder(
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        );
        jTable.setFillsViewportHeight(true);
        jPanel.add(jScrollPane, BorderLayout.CENTER);

        jTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int row = jTable.getSelectedRow();
                if (row >= 0) {
                    fields[0].setText(jTable.getValueAt(row, 1).toString());
                    fields[1].setText(jTable.getValueAt(row, 2).toString());
                    fields[2].setText(jTable.getValueAt(row, 3).toString());
                    fields[3].setText(jTable.getValueAt(row, 4).toString());
                }
            }
        });


        return jPanel;
    }

    private JPanel createLeftPanel() {
        fields = new JTextField[labelTexts.length];
        JPanel jPanel = new JPanel();
        //null - 배치관리자 x
        jPanel.setLayout(null);

        JPanel fieldPanel = new JPanel();
        fieldPanel.setBounds(15, 5, 450, 180);
        fieldPanel.setLayout(
                new GridLayout(4, 2, 5, 5)
        );
        fieldPanel.setBorder(
                BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("회원등록"),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
                )
        );

        //init
        for (int i = 0; i < fields.length; i++){
            fields[i] = new JTextField();
            JLabel jLabel = new JLabel(labelTexts[i], SwingConstants.LEFT);
            fieldPanel.add(jLabel);
            fieldPanel.add(fields[i]);
        }

        regButton = new JButton("등록");
        regButton.setBounds(15, 186, 450, 40);
        jPanel.add(regButton);

        updateButton = new JButton("수정");
        updateButton.setBounds(15, 236, 220, 40);
        jPanel.add(updateButton);

        deleteButton = new JButton("삭제");
        deleteButton.setBounds(245, 236, 220, 40);
        jPanel.add(deleteButton);

        jPanel.add(fieldPanel);
        return jPanel;
    }

    //static 메소드 createAndShowGUI - GUI를 생성하고 보여줌
    public static void createAndShowGUI(){
        JFrame frame = new MemberView("1906125 회원관리 프로그램");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setMinimumSize(SIZE);
        //null주면 창이 화면 중앙에 배치됨
        frame.setLocationRelativeTo(null);
        //창의 크기와 레이아웃을 패킹하여 컴포넌트들이 알맞게 보이도록 설정
        frame.pack();
        frame.setVisible(true);
    }


    @Override
    public void register(MemberEvent memberEvent) {
        Member member = (Member) memberEvent.getSource();
        defaultTableModel.insertRow(0, member.toArray());
        clearMemberFields();
    }

    @Override
    public void update(MemberEvent memberEvent) {
        Member member = (Member) memberEvent.getSource();
        Long updatedMemberId = member.getId();

        for (int i = 0; i < jTable.getRowCount(); i++) {
            Long tableMemberId = Long.parseLong(jTable.getValueAt(i, 0).toString());
            if (tableMemberId.equals(updatedMemberId)) {
                jTable.setValueAt(member.getEmail(), i, 1);
                jTable.setValueAt(member.getName(), i, 2);
                jTable.setValueAt(member.getPhone(), i, 3);
                jTable.setValueAt(member.getBirth().toString(), i, 4);
                break;
            }
        }
        clearMemberFields();
    }

    @Override
    public void delete(MemberEvent memberEvent) {
        Long memberId = (Long) memberEvent.getSource();

        for (int i = 0; i < jTable.getRowCount(); i++) {
            Long tableMemberId = Long.parseLong(jTable.getValueAt(i, 0).toString());
            if (tableMemberId.equals(memberId)) {
                defaultTableModel.removeRow(i);
                break; // 삭제했으므로 루프 종료
            }
        }
        clearMemberFields(); // 입력 필드를 초기화
    }



    private void clearMemberFields() {
        Arrays.stream(fields).forEach(
                field -> field.setText("")
        );
    }
}

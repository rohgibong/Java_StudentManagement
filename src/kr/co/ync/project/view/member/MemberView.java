package kr.co.ync.project.view.member;

import kr.co.ync.project.controller.member.MemberController;
import kr.co.ync.project.controller.member.listeners.MemberEvent;
import kr.co.ync.project.controller.member.listeners.MemberListener;
import kr.co.ync.project.model.Member;
import kr.co.ync.project.util.Util;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.regex.Pattern;

public class MemberView extends JFrame implements MemberListener {

    // 상수 SIZE - 창의 크기를 정의하는 상수
    public static final Dimension SIZE = new Dimension(1000, 500);
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
    private static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-Z가-힣\\s]+$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\d{3}-\\d{3,4}-\\d{4}$");

    private final String[] labelTexts = {"이메일", "이름", "전화번호", "생년월일"};

    private JTextField[] fields;
    private JButton regButton;
    private JButton updateButton;
    private JButton deleteButton;
    private DefaultTableModel defaultTableModel;
    private JTable jTable;
    private JPanel instructionsPanel;
    private Long selectedMemberId;
    private JPanel fieldPanel;


    // 생성자 MemberView - 창의 제목을 설정
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
                // 초기 상태인지 체크
                if (updateButton.isVisible() || deleteButton.isVisible()) {
                    resetToInitialState(); // 초기 상태로 돌아가기
                } else {
                    int response = JOptionPane.showConfirmDialog(
                            null,
                            "회원 정보를 등록하시겠습니까?",
                            "등록 확인",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE
                    );

                    if (response == JOptionPane.YES_OPTION) {
                        try {
                            if (!validateFields()) {
                                return;
                            }

                            Member member = new Member(
                                    fields[0].getText(),
                                    fields[1].getText(),
                                    fields[2].getText(),
                                    Util.strToLocalDate(fields[3].getText())
                            );
                            Member savedMember = MemberController.getInstance().save(member);
                            if (savedMember != null) {
                                loadMembers();
                                clearMemberFields();
                                resetToInitialState();
                            }

                            JOptionPane.showMessageDialog(
                                    null,
                                    "회원 등록이 완료되었습니다.",
                                    "등록 완료",
                                    JOptionPane.INFORMATION_MESSAGE
                            );
                        } catch (Exception exception) {
                            exception.printStackTrace();
                            JOptionPane.showMessageDialog(null, "회원 정보를 저장하는 중 오류가 발생했습니다.", "오류", JOptionPane.ERROR_MESSAGE);
                        }
                    }
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
                int response = JOptionPane.showConfirmDialog(
                        null,
                        "회원 정보를 수정하시겠습니까?",
                        "수정 확인",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE
                );
                if (response == JOptionPane.YES_OPTION) {
                    try {
                        int row = jTable.getSelectedRow();
                        if (row < 0) {
                            JOptionPane.showMessageDialog(null, "수정할 회원을 선택하세요.", "경고", JOptionPane.WARNING_MESSAGE);
                            return;
                        }

                        Long memberId = selectedMemberId;
                        if (memberId == null) {
                            JOptionPane.showMessageDialog(null, "유효한 회원을 선택하세요.", "경고", JOptionPane.WARNING_MESSAGE);
                            return;
                        }

                        if (!validateFields()) {
                            return;
                        }

                        if (isEmailDuplicate(fields[0].getText(), memberId)) {
                            JOptionPane.showMessageDialog(null, "이미 존재하는 이메일입니다.", "경고", JOptionPane.WARNING_MESSAGE);
                            return;
                        }

                        if (isPhoneDuplicate(fields[2].getText(), memberId)) {
                            JOptionPane.showMessageDialog(null, "이미 존재하는 전화번호입니다.", "경고", JOptionPane.WARNING_MESSAGE);
                            return;
                        }

                        Member member = new Member(
                                fields[0].getText(),
                                fields[1].getText(),
                                fields[2].getText(),
                                Util.strToLocalDate(fields[3].getText())
                        );

                        member.setId(memberId);

                        MemberController.getInstance().update(member);
                        loadMembers();
                        clearMemberFields();
                        resetToInitialState();

                        JOptionPane.showMessageDialog(
                                null,
                                "회원 수정이 완료되었습니다.",
                                "수정 완료",
                                JOptionPane.INFORMATION_MESSAGE
                        );
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(null, "회원 정보를 수정하는 중 오류가 발생했습니다.", "오류", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
    }

    private boolean isEmailDuplicate(String email, Long memberId) {
        for (int i = 0; i < jTable.getRowCount(); i++) {
            Long tableMemberId = Long.parseLong(jTable.getValueAt(i, 0).toString());
            if (!tableMemberId.equals(memberId) && jTable.getValueAt(i, 1).toString().equals(email)) {
                return true;
            }
        }
        return false;
    }

    private boolean isPhoneDuplicate(String phone, Long memberId) {
        for (int i = 0; i < jTable.getRowCount(); i++) {
            Long tableMemberId = Long.parseLong(jTable.getValueAt(i, 0).toString());
            if (!tableMemberId.equals(memberId) && jTable.getValueAt(i, 3).toString().equals(phone)) {
                return true;
            }
        }
        return false;
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
            JOptionPane.showMessageDialog(null, "유효한 이름을 입력하세요.", "경고", JOptionPane.WARNING_MESSAGE);
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
                int response = JOptionPane.showConfirmDialog(
                        null,
                        "회원 정보를 삭제하시겠습니까?",
                        "삭제 확인",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE
                );

                if (response == JOptionPane.YES_OPTION) {
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
                        loadMembers();
                        clearMemberFields();
                        resetToInitialState();

                        JOptionPane.showMessageDialog(
                                null,
                                "회원 삭제가 완료되었습니다.",
                                "삭제 완료",
                                JOptionPane.INFORMATION_MESSAGE
                        );
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(null, "회원 정보를 삭제하는 중 오류가 발생했습니다.", "오류", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
    }

    private void loadMembers() {
        try {
            defaultTableModel.setRowCount(0);
            for (Member member : MemberController.getInstance().allMembers()) {
                defaultTableModel.insertRow(0, member.toArray());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private JPanel createRightPanel() {
        JPanel jPanel = new JPanel();
        jPanel.setBorder(
                BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("회원목록"),
                        BorderFactory.createEmptyBorder(5, 5, 5, 5)
                )
        );

        jPanel.setLayout(new BorderLayout());

        JLabel sortingHintLabel = new JLabel("각 열의 제목을 클릭하면 정렬이 가능합니다.", JLabel.CENTER);
        Font defaultFont = sortingHintLabel.getFont();
        sortingHintLabel.setFont(defaultFont.deriveFont(11f));
        sortingHintLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        jPanel.add(sortingHintLabel, BorderLayout.NORTH);

        JScrollPane jScrollPane = new JScrollPane();

        defaultTableModel = new DefaultTableModel(
                new String[]{
                        "No", "이메일", "이름", "전화번호", "생년월일", "가입일"
                }, 0
        );
        jTable = new JTable(defaultTableModel);
        jTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(defaultTableModel);
        jTable.setRowSorter(sorter);

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
                    selectedMemberId = Long.parseLong(jTable.getValueAt(row, 0).toString());

                    // 항목을 클릭하면 수정 및 삭제 버튼을 보이게 함
                    updateButton.setVisible(true);
                    deleteButton.setVisible(true);
                    regButton.setText("새로 등록하기");

                    // 설명 패널 변경
                    updateInstructionsPanel(false);

                    updatePanelTitle("회원수정/삭제");
                }
            }
        });
        return jPanel;
    }

    private JPanel createLeftPanel() {
        fields = new JTextField[labelTexts.length];
        JPanel jPanel = new JPanel();
        jPanel.setLayout(null);

        fieldPanel = new JPanel();
        fieldPanel.setBounds(15, 5, 450, 180);
        fieldPanel.setLayout(new GridLayout(4, 2, 5, 5));
        fieldPanel.setBorder(
                BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("회원등록"),
                        BorderFactory.createEmptyBorder(5, 5, 5, 5)
                )
        );

        // init
        for (int i = 0; i < fields.length; i++) {
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
        updateButton.setVisible(false); // 초기에는 숨김
        jPanel.add(updateButton);

        deleteButton = new JButton("삭제");
        deleteButton.setBounds(245, 236, 220, 40);
        deleteButton.setVisible(false); // 초기에는 숨김
        jPanel.add(deleteButton);

        jPanel.add(fieldPanel);

        // 설명 패널 추가 및 필드에 저장
        instructionsPanel = createInstructionsPanel(true);
        instructionsPanel.setBounds(0, 270, 490, 170);
        jPanel.add(instructionsPanel);

        return jPanel;
    }



    private JPanel createInstructionsPanel(boolean isInitialMode) {
        JPanel instructionsPanel = new JPanel();
        instructionsPanel.setLayout(new BoxLayout(instructionsPanel, BoxLayout.Y_AXIS));
        instructionsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel instructionsLabel;

        if (isInitialMode) {
            instructionsLabel = new JLabel("<html>" +
                    "<h3>프로그램 사용 방법</h3>" +
                    "<p>회원 등록: 회원 정보를 입력한 후 '등록' 버튼을 클릭하여 새 회원을 등록합니다.</p>" +
                    "<p>입력 형식: 모든 필드는 올바른 형식으로 입력해야 합니다.</p>" +
                    "<p>ex) 이메일 (example@domain.com), 전화번호 (000-0000-0000 또는 000-000-0000), 생년월일 (yyyy-MM-dd).</p>" +
                    "<p>수정 또는 삭제를 원하는 회원을 선택 후 수정/삭제를 할 수 있습니다.</p>" +
                    "</html>");
        } else {
            instructionsLabel = new JLabel("<html>" +
                    "<h3>프로그램 사용 방법</h3>" +
                    "<p>새로 등록하기: 등록 모드로 돌아가려면 '새로 등록하기' 버튼을 클릭하여 돌아갑니다.</p>" +
                    "<p>회원 수정: 회원을 선택한 후, 필요한 정보를 수정하고 '수정' 버튼을 클릭하여 변경 내용을 저장합니다.</p>" +
                    "<p>회원 삭제: 회원을 선택한 후 '삭제' 버튼을 클릭하여 해당 회원을 삭제합니다.</p>" +
                    "<p>입력 형식: 모든 필드는 올바른 형식으로 입력해야 합니다.</p>" +
                    "<p>ex) 이메일 (example@domain.com), 전화번호 (000-0000-0000 또는 000-000-0000), 생년월일 (yyyy-MM-dd).</p>" +
                    "</html>");
        }

        instructionsPanel.add(instructionsLabel);
        return instructionsPanel;
    }

    private void updateInstructionsPanel(boolean isInitialMode) {
        JPanel parentPanel = (JPanel) instructionsPanel.getParent();
        parentPanel.remove(instructionsPanel);
        instructionsPanel = createInstructionsPanel(isInitialMode);
        instructionsPanel.setBounds(0, 270, 490, 170);
        parentPanel.add(instructionsPanel); // 새로운 설명 패널 추가
        revalidate();
        repaint();
    }

    // static 메소드 createAndShowGUI - GUI를 생성하고 보여줌
    public static void createAndShowGUI() {
        JFrame frame = new MemberView("1906125 회원관리 프로그램");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setMinimumSize(SIZE);
        frame.setLocationRelativeTo(null);
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
                break;
            }
        }
        clearMemberFields();
    }

    private void updatePanelTitle(String title) {
        CompoundBorder compoundBorder = (CompoundBorder)fieldPanel.getBorder();
        Border outsideBorder = compoundBorder.getOutsideBorder();
        TitledBorder titledBorder = (TitledBorder) outsideBorder;
        titledBorder.setTitle(title);
        fieldPanel.repaint();
    }


    // 초기 상태로 되돌리는 메서드
    private void resetToInitialState() {
        clearMemberFields();
        updateButton.setVisible(false);
        deleteButton.setVisible(false);
        regButton.setText("등록");

        updateInstructionsPanel(true);

        updatePanelTitle("회원등록");
    }




    private void clearMemberFields() {
        Arrays.stream(fields).forEach(
                field -> field.setText("")
        );
    }
}

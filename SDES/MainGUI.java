import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

/**
 * S-DES算法GUI主界面 - 支持多对明密文暴力破解
 */
public class MainGUI extends JFrame {
    private JTabbedPane tabbedPane;

    // 加密组件
    private JTextField encryptPlaintextField;
    private JTextField encryptKeyField;
    private JTextField encryptResultField;
    private JComboBox<String> encryptInputType;
    private JComboBox<String> encryptOutputType;

    // 解密组件
    private JTextField decryptCiphertextField;
    private JTextField decryptKeyField;
    private JTextField decryptResultField;
    private JComboBox<String> decryptInputType;
    private JComboBox<String> decryptOutputType;

    // 暴力破解组件
    private JTable bruteForceTable;
    private DefaultTableModel tableModel;
    private JTextArea bruteForceResultArea;
    private JProgressBar progressBar;
    private JLabel statusLabel;

    // 暴力破解处理器
    private BruteForceProcessor bruteForceProcessor;

    public MainGUI() {
        initComponents();
        setupLayout();
        setupListeners();
    }

    private void initComponents() {
        setTitle("S-DES算法工具");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 750);
        setLocationRelativeTo(null);

        tabbedPane = new JTabbedPane();

        // 初始化加密面板组件
        encryptPlaintextField = new JTextField(20);
        encryptKeyField = new JTextField(20);
        encryptResultField = new JTextField(20);
        encryptResultField.setEditable(false);

        String[] inputTypes = {"二进制", "ASCII字符"};
        encryptInputType = new JComboBox<>(inputTypes);
        encryptOutputType = new JComboBox<>(inputTypes);

        // 初始化解密面板组件
        decryptCiphertextField = new JTextField(20);
        decryptKeyField = new JTextField(20);
        decryptResultField = new JTextField(20);
        decryptResultField.setEditable(false);
        decryptInputType = new JComboBox<>(inputTypes);
        decryptOutputType = new JComboBox<>(inputTypes);

        // 初始化暴力破解面板组件
        initBruteForceComponents();

        // 初始化暴力破解处理器
        bruteForceProcessor = new BruteForceProcessor();
    }

    private void initBruteForceComponents() {
        // 创建表格模型
        String[] columnNames = {"序号", "明文 (8位)", "密文 (8位)", "状态"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // 只有明文和密文列可编辑，序号和状态列不可编辑
                return column == 1 || column == 2;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return String.class;
            }
        };

        bruteForceTable = new JTable(tableModel);
        bruteForceTable.setRowHeight(25);
        bruteForceTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        bruteForceTable.getColumnModel().getColumn(1).setPreferredWidth(150);
        bruteForceTable.getColumnModel().getColumn(2).setPreferredWidth(150);
        bruteForceTable.getColumnModel().getColumn(3).setPreferredWidth(80);

        bruteForceResultArea = new JTextArea(8, 50);
        bruteForceResultArea.setEditable(false);
        bruteForceResultArea.setFont(new Font("Consolas", Font.PLAIN, 12));

        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);

        statusLabel = new JLabel("就绪");
        statusLabel.setBorder(BorderFactory.createLoweredBevelBorder());
    }

    // 二进制输入过滤器 - 只允许输入0和1
    private class BinaryDocumentFilter extends DocumentFilter {
        private int maxLength;

        public BinaryDocumentFilter(int maxLength) {
            this.maxLength = maxLength;
        }

        @Override
        public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
                throws BadLocationException {
            if (string == null) return;

            String newString = filterString(string);
            if (newString.length() > 0) {
                String currentText = fb.getDocument().getText(0, fb.getDocument().getLength());
                if (currentText.length() + newString.length() <= maxLength) {
                    super.insertString(fb, offset, newString, attr);
                }
            }
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                throws BadLocationException {
            if (text == null) return;

            String newString = filterString(text);
            if (newString.length() > 0) {
                String currentText = fb.getDocument().getText(0, fb.getDocument().getLength());
                int newLength = currentText.length() - length + newString.length();
                if (newLength <= maxLength) {
                    super.replace(fb, offset, length, newString, attrs);
                }
            }
        }

        private String filterString(String input) {
            StringBuilder filtered = new StringBuilder();
            for (int i = 0; i < input.length(); i++) {
                char c = input.charAt(i);
                if (c == '0' || c == '1') {
                    filtered.append(c);
                }
            }
            return filtered.toString();
        }
    }

    // ASCII输入过滤器 - 允许输入任何字符，但限制长度
    private class AsciiDocumentFilter extends DocumentFilter {
        private int maxLength;

        public AsciiDocumentFilter(int maxLength) {
            this.maxLength = maxLength;
        }

        @Override
        public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
                throws BadLocationException {
            if (string == null) return;

            String currentText = fb.getDocument().getText(0, fb.getDocument().getLength());
            if (currentText.length() + string.length() <= maxLength) {
                super.insertString(fb, offset, string, attr);
            }
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                throws BadLocationException {
            if (text == null) return;

            String currentText = fb.getDocument().getText(0, fb.getDocument().getLength());
            int newLength = currentText.length() - length + text.length();
            if (newLength <= maxLength) {
                super.replace(fb, offset, length, text, attrs);
            }
        }
    }

    private boolean isValidBinaryString(String input, int expectedLength) {
        if (input == null || input.length() != expectedLength) {
            return false;
        }

        // 手动检查每个字符，避免使用正则表达式
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c != '0' && c != '1') {
                return false;
            }
        }
        return true;
    }

    private boolean isValidAsciiChar(String input) {
        if (input == null || input.length() != 1) {
            return false;
        }
        char c = input.charAt(0);
        return c >= 0 && c <= 127; // ASCII范围是0-127
    }

    private String asciiToBinary(String asciiChar) {
        if (asciiChar.length() != 1) {
            throw new IllegalArgumentException("ASCII输入必须为单个字符");
        }
        int asciiValue = (int) asciiChar.charAt(0);
        if (asciiValue > 255) {
            throw new IllegalArgumentException("字符超出1字节ASCII范围");
        }
        return String.format("%8s", Integer.toBinaryString(asciiValue & 0xFF)).replace(' ', '0');
    }

    private String binaryToAscii(String binary) {
        if (binary.length() != 8) {
            throw new IllegalArgumentException("二进制输入必须为8位");
        }
        int value = Integer.parseInt(binary, 2);
        return String.valueOf((char) value);
    }

    private void setupLayout() {
        // 加密面板
        JPanel encryptPanel = createEncryptPanel();
        tabbedPane.addTab("加密", encryptPanel);

        // 解密面板
        JPanel decryptPanel = createDecryptPanel();
        tabbedPane.addTab("解密", decryptPanel);

        // 暴力破解面板
        JPanel bruteForcePanel = createBruteForcePanel();
        tabbedPane.addTab("暴力破解", bruteForcePanel);

        add(tabbedPane);
    }

    private JPanel createEncryptPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 标题
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 3;
        JLabel titleLabel = new JLabel("S-DES 加密 - 支持二进制和ASCII输入", JLabel.CENTER);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
        panel.add(titleLabel, gbc);

        // 明文输入类型
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
        panel.add(new JLabel("明文输入类型:"), gbc);

        gbc.gridx = 1; gbc.gridy = 1; gbc.gridwidth = 2;
        panel.add(encryptInputType, gbc);

        // 明文输入
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1;
        panel.add(new JLabel("明文:"), gbc);

        gbc.gridx = 1; gbc.gridy = 2; gbc.gridwidth = 2;
        panel.add(encryptPlaintextField, gbc);

        // 密钥输入
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 1;
        panel.add(new JLabel("10位二进制密钥:"), gbc);

        gbc.gridx = 1; gbc.gridy = 3; gbc.gridwidth = 2;
        panel.add(encryptKeyField, gbc);

        // 输出类型
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 1;
        panel.add(new JLabel("输出类型:"), gbc);

        gbc.gridx = 1; gbc.gridy = 4; gbc.gridwidth = 2;
        panel.add(encryptOutputType, gbc);

        // 加密按钮
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 3;
        JButton encryptButton = new JButton("执行加密");
        encryptButton.setBackground(new Color(70, 130, 180));
        encryptButton.setForeground(Color.WHITE);
        panel.add(encryptButton, gbc);

        // 结果标签
        gbc.gridx = 0; gbc.gridy = 6;
        gbc.gridwidth = 1;
        panel.add(new JLabel("加密结果:"), gbc);

        gbc.gridx = 1; gbc.gridy = 6; gbc.gridwidth = 2;
        encryptResultField.setBackground(new Color(240, 240, 240));
        panel.add(encryptResultField, gbc);

        gbc.gridx = 0; gbc.gridy = 7; gbc.gridwidth = 3;
        JButton clearEncryptButton = new JButton("清除所有输入");
        clearEncryptButton.setBackground(new Color(169, 169, 169));
        clearEncryptButton.setForeground(Color.WHITE);
        panel.add(clearEncryptButton, gbc);

        // 按钮事件
        encryptButton.addActionListener(e -> performEncryption());
        clearEncryptButton.addActionListener(e -> clearEncryptFields());

        return panel;
    }

    private JPanel createDecryptPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 标题
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 3;
        JLabel titleLabel = new JLabel("S-DES 解密 - 支持二进制和ASCII输入", JLabel.CENTER);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
        panel.add(titleLabel, gbc);

        // 密文输入类型
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
        panel.add(new JLabel("密文输入类型:"), gbc);

        gbc.gridx = 1; gbc.gridy = 1; gbc.gridwidth = 2;
        panel.add(decryptInputType, gbc);

        // 密文输入
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1;
        panel.add(new JLabel("密文:"), gbc);

        gbc.gridx = 1; gbc.gridy = 2; gbc.gridwidth = 2;
        panel.add(decryptCiphertextField, gbc);

        // 密钥输入
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 1;
        panel.add(new JLabel("10位二进制密钥:"), gbc);

        gbc.gridx = 1; gbc.gridy = 3; gbc.gridwidth = 2;
        panel.add(decryptKeyField, gbc);

        // 输出类型
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 1;
        panel.add(new JLabel("输出类型:"), gbc);

        gbc.gridx = 1; gbc.gridy = 4; gbc.gridwidth = 2;
        panel.add(decryptOutputType, gbc);

        // 解密按钮
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 3;
        JButton decryptButton = new JButton("执行解密");
        decryptButton.setBackground(new Color(70, 130, 180));
        decryptButton.setForeground(Color.WHITE);
        panel.add(decryptButton, gbc);

        // 结果标签
        gbc.gridx = 0; gbc.gridy = 6;
        gbc.gridwidth = 1;
        panel.add(new JLabel("解密结果:"), gbc);

        gbc.gridx = 1; gbc.gridy = 6; gbc.gridwidth = 2;
        decryptResultField.setBackground(new Color(240, 240, 240));
        panel.add(decryptResultField, gbc);

        // 清除按钮
        gbc.gridx = 0; gbc.gridy = 7; gbc.gridwidth = 3;
        JButton clearDecryptButton = new JButton("清除所有输入");
        clearDecryptButton.setBackground(new Color(169, 169, 169));
        clearDecryptButton.setForeground(Color.WHITE);
        panel.add(clearDecryptButton, gbc);

        // 按钮事件
        decryptButton.addActionListener(e -> performDecryption());
        clearDecryptButton.addActionListener(e -> clearDecryptFields());

        return panel;
    }

    private JPanel createBruteForcePanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // 顶部标题
        JLabel titleLabel = new JLabel("S-DES 暴力破解 - 多对明密文支持", JLabel.CENTER);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // 中央面板 - 表格和控制按钮
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));

        // 表格面板
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder("明密文对列表 (仅支持二进制)"));

        JScrollPane tableScrollPane = new JScrollPane(bruteForceTable);
        tablePanel.add(tableScrollPane, BorderLayout.CENTER);

        // 表格控制按钮面板
        JPanel tableControlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton addRowButton = new JButton("添加一对");
        JButton deleteRowButton = new JButton("删除选中");
        JButton clearAllButton = new JButton("清空所有");
        JButton generateTestButton = new JButton("生成测试数据");

        tableControlPanel.add(addRowButton);
        tableControlPanel.add(deleteRowButton);
        tableControlPanel.add(clearAllButton);
        tableControlPanel.add(generateTestButton);

        tablePanel.add(tableControlPanel, BorderLayout.SOUTH);

        centerPanel.add(tablePanel, BorderLayout.CENTER);

        // 底部面板 - 进度和结果
        JPanel bottomPanel = new JPanel(new BorderLayout(10, 10));

        // 进度面板
        JPanel progressPanel = new JPanel(new BorderLayout());
        progressPanel.setBorder(BorderFactory.createTitledBorder("进度"));
        progressPanel.add(progressBar, BorderLayout.CENTER);
        progressPanel.add(statusLabel, BorderLayout.EAST);

        // 控制按钮面板
        JPanel controlPanel = new JPanel(new FlowLayout());
        JButton bruteForceButton = new JButton("开始暴力破解");
        bruteForceButton.setBackground(new Color(220, 20, 60));
        bruteForceButton.setForeground(Color.WHITE);
        controlPanel.add(bruteForceButton);

        // 结果面板
        JPanel resultPanel = new JPanel(new BorderLayout());
        resultPanel.setBorder(BorderFactory.createTitledBorder("破解结果"));
        JScrollPane resultScrollPane = new JScrollPane(bruteForceResultArea);
        resultPanel.add(resultScrollPane, BorderLayout.CENTER);

        bottomPanel.add(progressPanel, BorderLayout.NORTH);
        bottomPanel.add(controlPanel, BorderLayout.CENTER);
        bottomPanel.add(resultPanel, BorderLayout.SOUTH);

        centerPanel.add(bottomPanel, BorderLayout.SOUTH);

        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // 按钮事件
        addRowButton.addActionListener(e -> addTableRow());
        deleteRowButton.addActionListener(e -> deleteSelectedRows());
        clearAllButton.addActionListener(e -> clearAllRows());
        generateTestButton.addActionListener(e -> generateTestData());
        bruteForceButton.addActionListener(e -> performBruteForce());

        // 添加初始行
        addTableRow();

        return mainPanel;
    }

    private void setupListeners() {
        // 输入验证监听器
        addInputValidationListeners();

        // 为输入类型选择添加监听器，动态改变输入限制
        encryptInputType.addActionListener(e -> updateInputRestrictions());
        decryptInputType.addActionListener(e -> updateInputRestrictions());

        // 为表格添加数据变化监听器
        tableModel.addTableModelListener(e -> {
            int row = e.getFirstRow();
            if (row >= 0 && row < tableModel.getRowCount()) {
                // 使用 SwingUtilities.invokeLater 避免立即触发
                SwingUtilities.invokeLater(() -> {
                    updateRowStatus(row);
                });
            }
        });

        // 添加表格焦点监听器，在失去焦点时验证
        bruteForceTable.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusLost(java.awt.event.FocusEvent evt) {
                SwingUtilities.invokeLater(() -> {
                    validateAllRows();
                });
            }
        });

        // 初始设置输入限制
        updateInputRestrictions();
    }

    private void updateInputRestrictions() {
        // 更新加密面板输入限制
        String encryptInputTypeStr = (String) encryptInputType.getSelectedItem();
        if ("二进制".equals(encryptInputTypeStr)) {
            setDocumentFilter(encryptPlaintextField, new BinaryDocumentFilter(8), 8);
            encryptPlaintextField.setToolTipText("请输入8位二进制数（只允许0和1）");
        } else {
            setDocumentFilter(encryptPlaintextField, new AsciiDocumentFilter(1), 1);
            encryptPlaintextField.setToolTipText("请输入单个ASCII字符");
        }

        // 更新解密面板输入限制
        String decryptInputTypeStr = (String) decryptInputType.getSelectedItem();
        if ("二进制".equals(decryptInputTypeStr)) {
            setDocumentFilter(decryptCiphertextField, new BinaryDocumentFilter(8), 8);
            decryptCiphertextField.setToolTipText("请输入8位二进制数（只允许0和1）");
        } else {
            setDocumentFilter(decryptCiphertextField, new AsciiDocumentFilter(1), 1);
            decryptCiphertextField.setToolTipText("请输入单个ASCII字符");
        }
    }

    private void addInputValidationListeners() {
        // 密钥字段始终使用二进制过滤器
        setDocumentFilter(encryptKeyField, new BinaryDocumentFilter(10), 10);
        setDocumentFilter(decryptKeyField, new BinaryDocumentFilter(10), 10);

        // 暴力破解表格的单元格编辑器也使用二进制过滤器
        setupTableEditors();
    }

    private void setupTableEditors() {
        // 为表格创建自定义的单元格编辑器
        bruteForceTable.setDefaultEditor(String.class, new DefaultCellEditor(new JTextField()) {
            private JTextField textField;
            private String originalValue;

            @Override
            public Component getTableCellEditorComponent(JTable table, Object value,
                                                         boolean isSelected, int row, int column) {
                textField = (JTextField) super.getTableCellEditorComponent(table, value, isSelected, row, column);
                originalValue = (value != null) ? value.toString() : "";

                // 清除所有现有的动作监听器
                for (ActionListener al : textField.getActionListeners()) {
                    textField.removeActionListener(al);
                }

                // 只有明文和密文列使用二进制过滤器
                if (column == 1 || column == 2) {
                    setDocumentFilter(textField, new BinaryDocumentFilter(8), 8);
                }

                // 设置文本选择，方便用户编辑
                textField.selectAll();
                return textField;
            }

            @Override
            public Object getCellEditorValue() {
                // 返回编辑后的值，不进行任何自动复制
                return textField.getText();
            }

            @Override
            public boolean stopCellEditing() {
                // 停止编辑时，只保存当前单元格的值
                try {
                    fireEditingStopped();
                } catch (Exception e) {
                    // 忽略异常
                }
                return true;
            }

            @Override
            public void cancelCellEditing() {
                // 取消编辑时恢复原始值
                textField.setText(originalValue);
                super.cancelCellEditing();
            }
        });

        // 设置表格属性，避免不必要的选择行为
        bruteForceTable.setRowSelectionAllowed(true);
        bruteForceTable.setColumnSelectionAllowed(false);
        bruteForceTable.setCellSelectionEnabled(true);
        bruteForceTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        bruteForceTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
    }

    private void setDocumentFilter(JTextField field, DocumentFilter filter, int maxLength) {
        javax.swing.text.Document doc = field.getDocument();
        if (doc instanceof javax.swing.text.PlainDocument) {
            ((javax.swing.text.PlainDocument) doc).setDocumentFilter(filter);
        }
    }

    // 表格操作方法
    private void addTableRow() {
        int rowCount = tableModel.getRowCount();
        tableModel.addRow(new Object[]{
                String.valueOf(rowCount + 1),
                "",
                "",
                "待输入"
        });
        updateRowStatus(rowCount);
    }

    private void deleteSelectedRows() {
        int[] selectedRows = bruteForceTable.getSelectedRows();
        if (selectedRows.length == 0) {
            JOptionPane.showMessageDialog(this, "请选择要删除的行", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        for (int i = selectedRows.length - 1; i >= 0; i--) {
            tableModel.removeRow(selectedRows[i]);
        }

        // 更新序号
        updateRowNumbers();
    }

    private void clearAllRows() {
        int rowCount = tableModel.getRowCount();
        if (rowCount == 0) return;

        int result = JOptionPane.showConfirmDialog(this,
                "确定要清空所有数据吗？", "确认清空", JOptionPane.YES_NO_OPTION);
        if (result == JOptionPane.YES_OPTION) {
            tableModel.setRowCount(0);
            addTableRow(); // 添加一个空行
        }
    }

    private void generateTestData() {
        // 清空现有数据
        tableModel.setRowCount(0);

        // 添加一些测试数据
        String[][] testData = {
                {"00000001", "10010111"},
                {"11010110", "01110101"},
                {"11111111", "00010010"},
                {"00000000", "10001010"}
        };

        for (int i = 0; i < testData.length; i++) {
            tableModel.addRow(new Object[]{
                    String.valueOf(i + 1),
                    testData[i][0],
                    testData[i][1],
                    "有效"
            });
        }

        bruteForceResultArea.setText("已生成测试数据，请点击'开始暴力破解'进行测试。\n");
    }

    private void updateRowNumbers() {
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            tableModel.setValueAt(String.valueOf(i + 1), i, 0);
        }
    }

    private void updateRowStatus(int row) {
        String plaintext = ((String) tableModel.getValueAt(row, 1)).trim();
        String ciphertext = ((String) tableModel.getValueAt(row, 2)).trim();
        String currentStatus = (String) tableModel.getValueAt(row, 3);

        // 使用新的验证方法，避免正则表达式
        boolean plaintextValid = isValidBinaryString(plaintext, 8);
        boolean ciphertextValid = isValidBinaryString(ciphertext, 8);

        String newStatus;
        if (plaintext.isEmpty() && ciphertext.isEmpty()) {
            newStatus = "待输入";
        } else if (plaintextValid && ciphertextValid) {
            newStatus = "有效";
        } else {
            newStatus = "无效";
        }

        // 只有当状态真正改变时才更新，避免不必要的触发
        if (!newStatus.equals(currentStatus)) {
            tableModel.setValueAt(newStatus, row, 3);
        }
    }

    // 添加一个验证所有行的方法，在暴力破解前调用
    private boolean validateAllRows() {
        boolean allValid = true;
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String plaintext = ((String) tableModel.getValueAt(i, 1)).trim();
            String ciphertext = ((String) tableModel.getValueAt(i, 2)).trim();
            String currentStatus = (String) tableModel.getValueAt(i, 3);

            boolean plaintextValid = isValidBinaryString(plaintext, 8);
            boolean ciphertextValid = isValidBinaryString(ciphertext, 8);

            // 只有当两个字段都有内容时才检查有效性
            if (!plaintext.isEmpty() && !ciphertext.isEmpty()) {
                String newStatus = plaintextValid && ciphertextValid ? "有效" : "无效";

                // 只有当状态改变时才更新
                if (!newStatus.equals(currentStatus)) {
                    tableModel.setValueAt(newStatus, i, 3);
                }

                if (!plaintextValid || !ciphertextValid) {
                    allValid = false;
                }
            } else {
                // 如果有一个字段为空，标记为待输入
                String newStatus = "待输入";
                if (!newStatus.equals(currentStatus)) {
                    tableModel.setValueAt(newStatus, i, 3);
                }
                allValid = false; // 有空字段也不允许破解
            }
        }
        return allValid;
    }

    // 核心功能方法
    private void performEncryption() {
        try {
            String plaintext = encryptPlaintextField.getText().trim();
            String key = encryptKeyField.getText().trim();
            String inputType = (String) encryptInputType.getSelectedItem();
            String outputType = (String) encryptOutputType.getSelectedItem();

            // 验证密钥
            if (!validateInput(key, 10, "密钥")) {
                return;
            }

            String binaryPlaintext;
            // 根据输入类型处理明文
            if ("ASCII字符".equals(inputType)) {
                if (!isValidAsciiChar(plaintext)) {
                    JOptionPane.showMessageDialog(this,
                            "ASCII输入必须为单个字符(0-127)",
                            "输入错误", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                binaryPlaintext = asciiToBinary(plaintext);
            } else {
                if (!validateInput(plaintext, 8, "明文")) {
                    return;
                }
                binaryPlaintext = plaintext;
            }

            // 执行加密
            String binaryCiphertext = SDES.encryptString(binaryPlaintext, key);

            // 根据输出类型处理结果
            String result;
            if ("ASCII字符".equals(outputType)) {
                result = binaryToAscii(binaryCiphertext);
            } else {
                result = binaryCiphertext;
            }

            encryptResultField.setText(result);
            encryptResultField.setBackground(new Color(220, 255, 220));

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "加密错误: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void performDecryption() {
        try {
            String ciphertext = decryptCiphertextField.getText().trim();
            String key = decryptKeyField.getText().trim();
            String inputType = (String) decryptInputType.getSelectedItem();
            String outputType = (String) decryptOutputType.getSelectedItem();

            // 验证密钥
            if (!validateInput(key, 10, "密钥")) {
                return;
            }

            String binaryCiphertext;
            // 根据输入类型处理密文
            if ("ASCII字符".equals(inputType)) {
                if (!isValidAsciiChar(ciphertext)) {
                    JOptionPane.showMessageDialog(this,
                            "ASCII输入必须为单个字符(0-127)",
                            "输入错误", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                binaryCiphertext = asciiToBinary(ciphertext);
            } else {
                if (!validateInput(ciphertext, 8, "密文")) {
                    return;
                }
                binaryCiphertext = ciphertext;
            }

            // 执行解密
            String binaryPlaintext = SDES.decryptString(binaryCiphertext, key);

            // 根据输出类型处理结果
            String result;
            if ("ASCII字符".equals(outputType)) {
                result = binaryToAscii(binaryPlaintext);
            } else {
                result = binaryPlaintext;
            }

            decryptResultField.setText(result);
            decryptResultField.setBackground(new Color(220, 255, 220));

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "解密错误: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void performBruteForce() {
        if (!validateAllRows()) {
            JOptionPane.showMessageDialog(this,
                    "存在无效的明密文对，请检查输入",
                    "输入错误", JOptionPane.WARNING_MESSAGE);
            return;
        }
        // 收集有效的明密文对
        List<String[]> validPairs = new ArrayList<>();

        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String plaintext = ((String) tableModel.getValueAt(i, 1)).trim();
            String ciphertext = ((String) tableModel.getValueAt(i, 2)).trim();
            String status = (String) tableModel.getValueAt(i, 3);

            if ("有效".equals(status)) {
                validPairs.add(new String[]{plaintext, ciphertext});
            }
        }

        if (validPairs.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "没有有效的明密文对，请至少输入一对有效的8位二进制明密文",
                    "输入错误", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 设置进度回调
        bruteForceProcessor.setProgressCallback(new BruteForceProcessor.ProgressCallback() {
            @Override
            public void onProgress(String message, int progress) {
                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText(message);
                    progressBar.setValue(progress);
                });
            }

            @Override
            public void onComplete(List<String> foundKeys, int pairCount) {
                SwingUtilities.invokeLater(() -> {
                    displayBruteForceResults(foundKeys, pairCount);
                });
            }
        });

        // 执行暴力破解
        bruteForceProcessor.startBruteForce(validPairs);
    }

    private void displayBruteForceResults(List<String> foundKeys, int pairCount) {
        StringBuilder result = new StringBuilder();
        result.append("暴力破解完成！\n\n");
        result.append("使用的明密文对数量: ").append(pairCount).append("\n");

        if (foundKeys.isEmpty()) {
            result.append("\n未找到匹配的密钥。\n");
            result.append("可能的原因：\n");
            result.append("1. 明密文对数据有误\n");
            result.append("2. 明密文对数量不足\n");
            result.append("3. 存在数据传输错误\n");
        } else {
            result.append("\n找到 ").append(foundKeys.size()).append(" 个可能的密钥：\n");
            result.append("=".repeat(50)).append("\n");

            for (int i = 0; i < foundKeys.size(); i++) {
                String key = foundKeys.get(i);
                result.append(String.format("密钥 %d: %s\n", i + 1, key));
                result.append("\n");
            }

            result.append("=".repeat(50)).append("\n");
            result.append("提示：使用更多明密文对可以减少候选密钥数量。\n");
        }

        bruteForceResultArea.setText(result.toString());
        bruteForceResultArea.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        progressBar.setValue(0);
        statusLabel.setText("就绪");
    }

    private boolean validateInput(String input, int expectedLength, String fieldName) {
        if (input.length() != expectedLength) {
            JOptionPane.showMessageDialog(this,
                    fieldName + "必须是" + expectedLength + "位二进制数",
                    "输入错误", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        if (!input.matches("[01]+")) {
            JOptionPane.showMessageDialog(this,
                    fieldName + "只能包含0和1",
                    "输入错误", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        return true;
    }

    // 清除加密面板的所有输入
    private void clearEncryptFields() {
        encryptPlaintextField.setText("");
        encryptKeyField.setText("");
        encryptResultField.setText("");
        encryptResultField.setBackground(Color.WHITE);
    }

    // 清除解密面板的所有输入
    private void clearDecryptFields() {
        decryptCiphertextField.setText("");
        decryptKeyField.setText("");
        decryptResultField.setText("");
        decryptResultField.setBackground(Color.WHITE);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            new MainGUI().setVisible(true);
        });
    }
}
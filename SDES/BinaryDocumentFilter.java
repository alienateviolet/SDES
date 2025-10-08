class BinaryDocumentFilter extends javax.swing.text.DocumentFilter {
    @Override
    public void insertString(FilterBypass fb, int offset, String string, javax.swing.text.AttributeSet attr)
            throws javax.swing.text.BadLocationException {
        if (string.matches("[01]*")) {
            super.insertString(fb, offset, string, attr);
        }
    }

    @Override
    public void replace(FilterBypass fb, int offset, int length, String text, javax.swing.text.AttributeSet attrs)
            throws javax.swing.text.BadLocationException {
        if (text.matches("[01]*")) {
            super.replace(fb, offset, length, text, attrs);
        }
    }
}
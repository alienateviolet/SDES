/**
 * 工具类
 * 提供各种辅助功能
 */
public class Utils {
    
    /**
     * 将二进制字符串转换为位数组
     * @param binaryString 二进制字符串，如 "10101010"
     * @return 位数组
     */
    public static int[] binaryStringToArray(String binaryString) {
        int[] bits = new int[binaryString.length()];
        for (int i = 0; i < binaryString.length(); i++) {
            bits[i] = Character.getNumericValue(binaryString.charAt(i));
        }
        return bits;
    }
    
    /**
     * 将位数组转换为二进制字符串
     * @param bits 位数组
     * @return 二进制字符串
     */
    public static String arrayToBinaryString(int[] bits) {
        StringBuilder sb = new StringBuilder();
        for (int bit : bits) {
            sb.append(bit);
        }
        return sb.toString();
    }
    
    /**
     * 执行异或操作
     * @param a 第一个位数组
     * @param b 第二个位数组
     * @return 异或结果
     */
    public static int[] xor(int[] a, int[] b) {
        if (a.length != b.length) {
            throw new IllegalArgumentException("异或操作的数组长度必须相同");
        }
        
        int[] result = new int[a.length];
        for (int i = 0; i < a.length; i++) {
            result[i] = a[i] ^ b[i];
        }
        return result;
    }
    
    /**
     * 验证输入是否为有效的二进制字符串
     * @param input 输入字符串
     * @param expectedLength 期望长度
     * @return 是否有效
     */
    public static boolean isValidBinary(String input, int expectedLength) {
        if (input == null || input.length() != expectedLength) {
            return false;
        }
        return input.matches("[01]+");
    }
    
    /**
     * 分割数组为左右两部分
     * @param input 输入数组
     * @return 包含左右两部分的二维数组
     */
    public static int[][] split(int[] input) {
        int mid = input.length / 2;
        int[] left = new int[mid];
        int[] right = new int[mid];
        
        System.arraycopy(input, 0, left, 0, mid);
        System.arraycopy(input, mid, right, 0, mid);
        
        return new int[][]{left, right};
    }
    
    /**
     * 合并左右两部分数组
     * @param left 左半部分
     * @param right 右半部分
     * @return 合并后的数组
     */
    public static int[] mergeArrays(int[] left, int[] right) {
        int[] merged = new int[left.length + right.length];
        System.arraycopy(left, 0, merged, 0, left.length);
        System.arraycopy(right, 0, merged, left.length, right.length);
        return merged;
    }
}
/**
 * 置换工具类
 * 提供S-DES算法中所有的置换操作
 */
public class Permutation {
    
    // 初始置换表 (IP)
    public static final int[] IP = {2, 6, 3, 1, 4, 8, 5, 7};
    
    // 逆初始置换表 (IP^-1)
    public static final int[] IP_INV = {4, 1, 3, 5, 7, 2, 8, 6};
    
    // 扩展置换表 (EP) - 将4位扩展为8位
    public static final int[] EP = {4, 1, 2, 3, 2, 3, 4, 1};
    
    // P4置换表
    public static final int[] P4 = {2, 4, 3, 1};
    
    // P8置换表 - 用于从10位密钥生成8位子密钥
    public static final int[] P8 = {6, 3, 7, 4, 8, 5, 10, 9};
    
    // P10置换表 - 用于初始密钥处理
    public static final int[] P10 = {3, 5, 2, 7, 4, 10, 1, 9, 8, 6};
    
    /**
     * 通用置换函数
     * @param input 输入位数组
     * @param table 置换表
     * @return 置换后的位数组
     */
    public static int[] permute(int[] input, int[] table) {
        int[] output = new int[table.length];
        for (int i = 0; i < table.length; i++) {
            // 置换表的值表示输入中的位置(从1开始计数)，所以需要减1
            output[i] = input[table[i] - 1];
        }
        return output;
    }
    
    /**
     * 执行初始置换
     * @param input 8位输入
     * @return 置换后的8位输出
     */
    public static int[] initialPermutation(int[] input) {
        return permute(input, IP);
    }
    
    /**
     * 执行逆初始置换
     * @param input 8位输入
     * @return 置换后的8位输出
     */
    public static int[] inverseInitialPermutation(int[] input) {
        return permute(input, IP_INV);
    }
    
    /**
     * 执行扩展置换
     * @param input 4位输入
     * @return 扩展后的8位输出
     */
    public static int[] expansionPermutation(int[] input) {
        return permute(input, EP);
    }
}
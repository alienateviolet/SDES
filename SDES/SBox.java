/**
 * S盒处理类
 * 实现S-DES算法的S盒替换功能
 */
public class SBox {
    
    // S0盒定义
    private static final int[][] S0 = {
        {1, 0, 3, 2},
        {3, 2, 1, 0},
        {0, 2, 1, 3},
        {3, 1, 3, 2}
    };
    
    // S1盒定义
    private static final int[][] S1 = {
        {0, 1, 2, 3},
        {2, 0, 1, 3},
        {3, 0, 1, 0},
        {2, 1, 0, 3}
    };
    
    /**
     * S盒替换函数
     * @param input 8位输入，将分为两个4位部分分别进入S0和S1
     * @return 4位输出
     */
    public static int[] sBoxSubstitution(int[] input) {
        if (input.length != 8) {
            throw new IllegalArgumentException("S盒输入必须为8位");
        }
        
        // 将8位输入分为左右各4位
        int[] left = {input[0], input[1], input[2], input[3]};
        int[] right = {input[4], input[5], input[6], input[7]};
        
        // 对左4位进行S0盒替换
        int s0Output = sBoxLookup(left, S0);
        
        // 对右4位进行S1盒替换
        int s1Output = sBoxLookup(right, S1);
        
        // 将两个2位输出合并为4位
        return mergeTo4Bits(s0Output, s1Output);
    }
    
    /**
     * 单个S盒查找
     * @param input 4位输入
     * @param sBox 使用的S盒
     * @return 2位输出
     */
    private static int sBoxLookup(int[] input, int[][] sBox) {
        // 行号由第1位和第4位决定
        int row = (input[0] << 1) | input[3];
        
        // 列号由第2位和第3位决定
        int col = (input[1] << 1) | input[2];
        
        return sBox[row][col];
    }
    
    /**
     * 将两个2位数值合并为4位数组
     * @param s0Output S0盒的2位输出
     * @param s1Output S1盒的2位输出
     * @return 4位数组
     */
    private static int[] mergeTo4Bits(int s0Output, int s1Output) {
        int[] output = new int[4];
        
        // S0输出的高位
        output[0] = (s0Output >> 1) & 1;
        output[1] = s0Output & 1;
        
        // S1输出的高位
        output[2] = (s1Output >> 1) & 1;
        output[3] = s1Output & 1;
        
        return output;
    }
}
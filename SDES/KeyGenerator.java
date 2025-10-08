/**
 * 密钥生成器
 * 从10位主密钥生成两个8位子密钥K1和K2
 */
public class KeyGenerator {
    
    /**
     * 生成两个子密钥
     * @param key 10位主密钥
     * @return 包含K1和K2的二维数组
     */
    public static int[][] generateSubKeys(int[] key) {
        if (key.length != 10) {
            throw new IllegalArgumentException("主密钥必须为10位");
        }
        
        // 步骤1: 对10位密钥进行P10置换
        int[] p10Key = Permutation.permute(key, Permutation.P10);
        
        // 步骤2: 将结果分为左右各5位
        int[] left = splitLeft(p10Key);
        int[] right = splitRight(p10Key);
        
        // 步骤3: 对左右部分分别进行左移1位
        left = leftShift(left, 1);
        right = leftShift(right, 1);
        
        // 步骤4: 合并后通过P8置换生成K1
        int[] k1 = Permutation.permute(merge(left, right), Permutation.P8);
        
        // 步骤5: 对左右部分分别再进行左移2位（总共左移3位）
        left = leftShift(left, 2);
        right = leftShift(right, 2);
        
        // 步骤6: 合并后通过P8置换生成K2
        int[] k2 = Permutation.permute(merge(left, right), Permutation.P8);
        
        return new int[][]{k1, k2};
    }
    
    /**
     * 获取左半部分（前5位）
     */
    private static int[] splitLeft(int[] input) {
        int[] left = new int[5];
        System.arraycopy(input, 0, left, 0, 5);
        return left;
    }
    
    /**
     * 获取右半部分（后5位）
     */
    private static int[] splitRight(int[] input) {
        int[] right = new int[5];
        System.arraycopy(input, 5, right, 0, 5);
        return right;
    }
    
    /**
     * 循环左移
     * @param input 5位输入
     * @param shifts 左移位数
     * @return 左移后的5位输出
     */
    private static int[] leftShift(int[] input, int shifts) {
        int[] output = new int[5];
        for (int i = 0; i < 5; i++) {
            output[i] = input[(i + shifts) % 5];
        }
        return output;
    }
    
    /**
     * 合并两个5位数组为10位数组
     */
    private static int[] merge(int[] left, int[] right) {
        int[] merged = new int[10];
        System.arraycopy(left, 0, merged, 0, 5);
        System.arraycopy(right, 0, merged, 5, 5);
        return merged;
    }
}
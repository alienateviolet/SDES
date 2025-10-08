/**
 * S-DES核心算法类
 * 实现加密和解密功能
 */
public class SDES {
    
    /**
     * F函数 - S-DES的核心轮函数
     * @param right 4位右半部分
     * @param subKey 8位子密钥
     * @return 4位输出
     */
    private static int[] fFunction(int[] right, int[] subKey) {
        // 步骤1: 扩展置换 (4位 -> 8位)
        int[] expanded = Permutation.expansionPermutation(right);
        
        // 步骤2: 与子密钥异或
        int[] afterXor = Utils.xor(expanded, subKey);
        
        // 步骤3: S盒替换 (8位 -> 4位)
        int[] afterSBox = SBox.sBoxSubstitution(afterXor);
        
        // 步骤4: P4置换
        return Permutation.permute(afterSBox, Permutation.P4);
    }
    
    /**
     * 加密单个8位数据块
     * @param plaintext 8位明文
     * @param key 10位密钥
     * @return 8位密文
     */
    public static int[] encrypt(int[] plaintext, int[] key) {
        // 生成子密钥K1和K2
        int[][] subKeys = KeyGenerator.generateSubKeys(key);
        int[] k1 = subKeys[0];
        int[] k2 = subKeys[1];

        // 步骤1: 初始置换
        int[] afterIP = Permutation.initialPermutation(plaintext);

        // 步骤2: 分割为左右各4位
        int[][] halves = Utils.split(afterIP);
        int[] left0 = halves[0];  // 初始左半部分
        int[] right0 = halves[1]; // 初始右半部分

        // === 第一轮 ===
        int[] fResult1 = fFunction(right0, k1);
        int[] left1 = right0;                    // 新的左半部分 = 旧的右半部分
        int[] right1 = Utils.xor(left0, fResult1); // 新的右半部分

        // === 第二轮 ===
        int[] fResult2 = fFunction(right1, k2);
        int[] left2 = Utils.xor(left1, fResult2); // 最终左半部分
        int[] right2 = right1;                    // 最终右半部分

        // 步骤3: 最终置换
        return Permutation.inverseInitialPermutation(Utils.mergeArrays(left2, right2));
    }
    
    /**
     * 解密单个8位数据块
     * @param ciphertext 8位密文
     * @param key 10位密钥
     * @return 8位明文
     */
    public static int[] decrypt(int[] ciphertext, int[] key) {
        // 生成子密钥K1和K2
        int[][] subKeys = KeyGenerator.generateSubKeys(key);
        int[] k1 = subKeys[0];
        int[] k2 = subKeys[1];

        // 步骤1: 初始置换
        int[] afterIP = Permutation.initialPermutation(ciphertext);

        // 步骤2: 分割为左右各4位
        int[][] halves = Utils.split(afterIP);
        int[] left0 = halves[0];
        int[] right0 = halves[1];

        // === 第一轮 - 使用K2 ===
        int[] fResult1 = fFunction(right0, k2);
        int[] left1 = right0;
        int[] right1 = Utils.xor(left0, fResult1);

        // === 第二轮 - 使用K1 ===
        int[] fResult2 = fFunction(right1, k1);
        int[] left2 = Utils.xor(left1, fResult2);
        int[] right2 = right1;

        // 步骤3: 最终置换
        return Permutation.inverseInitialPermutation(Utils.mergeArrays(left2, right2));
    }
    
    /**
     * 加密字符串（多数据块）
     * @param plaintext 明文二进制字符串
     * @param key 密钥二进制字符串
     * @return 密文二进制字符串
     */
    public static String encryptString(String plaintext, String key) {
        if (!Utils.isValidBinary(plaintext, 8) || !Utils.isValidBinary(key, 10)) {
            throw new IllegalArgumentException("输入必须是有效的二进制字符串");
        }
        
        int[] plaintextBits = Utils.binaryStringToArray(plaintext);
        int[] keyBits = Utils.binaryStringToArray(key);
        
        int[] ciphertextBits = encrypt(plaintextBits, keyBits);
        return Utils.arrayToBinaryString(ciphertextBits);
    }
    
    /**
     * 解密字符串（多数据块）
     * @param ciphertext 密文二进制字符串
     * @param key 密钥二进制字符串
     * @return 明文二进制字符串
     */
    public static String decryptString(String ciphertext, String key) {
        if (!Utils.isValidBinary(ciphertext, 8) || !Utils.isValidBinary(key, 10)) {
            throw new IllegalArgumentException("输入必须是有效的二进制字符串");
        }
        
        int[] ciphertextBits = Utils.binaryStringToArray(ciphertext);
        int[] keyBits = Utils.binaryStringToArray(key);
        
        int[] plaintextBits = decrypt(ciphertextBits, keyBits);
        return Utils.arrayToBinaryString(plaintextBits);
    }
}
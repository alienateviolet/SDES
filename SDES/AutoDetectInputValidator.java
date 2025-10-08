import java.util.Scanner;

public class AutoDetectInputValidator {

    /**
     * 自动检测输入类型并返回有效输入
     * 无需手动指定模式，通过输入内容自动判断
     * @param scanner Scanner对象
     * @param prompt 提示信息
     * @param expectedBinaryLength 二进制模式下的预期长度
     * @return 有效的输入字符串（ASCII或二进制）
     */
    public static String getValidInput(Scanner scanner, String prompt, int expectedBinaryLength) {
        String input;
        while (true) {
            System.out.print(prompt);
            input = scanner.nextLine().trim();

            // 自动检测输入类型
            if (Utils.isValidAscii(input)) {
                // 检测为ASCII字符
                break;
            } else if (Utils.isValidBinary(input, expectedBinaryLength)) {
                // 检测为符合长度的二进制
                break;
            } else {
                // 输入无效，提示两种可能的格式
                System.out.println("输入无效！请输入以下两种格式之一：");
                System.out.println("1. 单个ASCII字符（0-127范围内）");
                System.out.println("2. " + expectedBinaryLength + "位二进制数（只包含0和1）");
            }
        }
        return input;
    }

    // 判断输入的类型（ASCII或二进制）
    public static int getInputType(String input, int expectedBinaryLength) {
        if (Utils.isValidAscii(input)) {
            return 1; // ASCII类型
        } else if (Utils.isValidBinary(input, expectedBinaryLength)) {
            return 2; // 二进制类型
        } else {
            return -1; // 无效类型
        }
    }

    // 工具类
    public static class Utils {
        // 验证是否为单个ASCII字符（1字节）
        public static boolean isValidAscii(String input) {
            return input != null && input.length() == 1 && input.charAt(0) <= 127;
        }

        // 验证是否为指定长度的二进制字符串
        public static boolean isValidBinary(String input, int expectedLength) {
            if (input == null || input.length() != expectedLength) {
                return false;
            }
            for (int i = 0; i < input.length(); i++) {
                char c = input.charAt(i);
                if (c != '0' && c != '1') {
                    return false;
                }
            }
            return true;
        }

        // ASCII字符转换为8位二进制字符串
        public static String asciiToBinary(String ascii) {
            if (!isValidAscii(ascii)) {
                throw new IllegalArgumentException("无效的ASCII字符");
            }
            return String.format("%8s", Integer.toBinaryString(ascii.charAt(0)))
                    .replace(' ', '0');
        }

        // 8位二进制字符串转换为ASCII字符
        public static String binaryToAscii(String binary) {
            if (!isValidBinary(binary, 8)) {
                throw new IllegalArgumentException("无效的8位二进制字符串");
            }
            int asciiCode = Integer.parseInt(binary, 2);
            return Character.toString((char) asciiCode);
        }
    }
}


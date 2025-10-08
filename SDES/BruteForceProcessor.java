import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 * S-DES暴力破解处理器 - 处理暴力破解的核心逻辑
 */
public class BruteForceProcessor {
    private SwingWorker<Void, ProgressData> worker;
    private ProgressCallback progressCallback;

    public interface ProgressCallback {
        void onProgress(String message, int progress);
        void onComplete(List<String> foundKeys, int pairCount);
    }

    public void setProgressCallback(ProgressCallback callback) {
        this.progressCallback = callback;
    }

    public void startBruteForce(List<String[]> validPairs) {
        if (worker != null && !worker.isDone()) {
            worker.cancel(true);
        }

        worker = new SwingWorker<Void, ProgressData>() {
            private List<String> foundKeys = new ArrayList<>();

            @Override
            protected Void doInBackground() throws Exception {
                publish(new ProgressData("开始暴力破解...", 0));
                foundKeys.clear();

                int totalKeys = 1024; // 2^10 种可能的密钥
                int pairCount = validPairs.size();

                // 遍历所有可能的10位密钥
                for (int i = 0; i < totalKeys; i++) {
                    if (isCancelled()) {
                        break;
                    }

                    // 生成当前测试的密钥二进制字符串
                    String testKey = String.format("%10s", Integer.toBinaryString(i))
                            .replace(' ', '0');

                    boolean keyValid = true;

                    // 用当前密钥测试所有明密文对
                    for (String[] pair : validPairs) {
                        String plaintext = pair[0];
                        String expectedCiphertext = pair[1];

                        try {
                            // 使用当前密钥加密明文
                            String actualCiphertext = SDES.encryptString(plaintext, testKey);

                            // 如果加密结果与已知密文不匹配，则密钥无效
                            if (!actualCiphertext.equals(expectedCiphertext)) {
                                keyValid = false;
                                break;
                            }
                        } catch (Exception e) {
                            keyValid = false;
                            break;
                        }
                    }

                    // 如果密钥通过所有测试，则找到有效密钥
                    if (keyValid) {
                        foundKeys.add(testKey);
                    }

                    int progress = (int) ((double) i / totalKeys * 100);
                    publish(new ProgressData(
                            String.format("测试密钥: %s (%d/%d)", testKey, i + 1, totalKeys),
                            progress
                    ));
                }

                return null;
            }

            @Override
            protected void process(List<ProgressData> chunks) {
                if (!chunks.isEmpty() && progressCallback != null) {
                    ProgressData data = chunks.get(chunks.size() - 1);
                    progressCallback.onProgress(data.message, data.progress);
                }
            }

            @Override
            protected void done() {
                if (progressCallback != null) {
                    progressCallback.onComplete(foundKeys, validPairs.size());
                }
            }
        };

        worker.execute();
    }

    public void stopBruteForce() {
        if (worker != null && !worker.isDone()) {
            worker.cancel(true);
        }
    }

    // 进度数据内部类
    private static class ProgressData {
        String message;
        int progress;

        ProgressData(String message, int progress) {
            this.message = message;
            this.progress = progress;
        }
    }
}
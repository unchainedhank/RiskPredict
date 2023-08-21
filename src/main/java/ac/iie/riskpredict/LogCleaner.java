package ac.iie.riskpredict;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @author a3
 */
@SuppressWarnings("AlibabaUndefineMagicConstant")
@Component
public class LogCleaner {

    @Value("${my.log-path}")
    private String configuredLogPath;
    @Scheduled(cron = "0 0 0 * * ? ")
    public void cleanLogs() throws IOException {
        // 指定日志文件路径
        Path logPath = Paths.get(configuredLogPath);

        // 计算一周前的时间戳
        long oneWeekAgo = LocalDateTime.now().minusWeeks(1).toEpochSecond(ZoneOffset.UTC);

        // 读取日志文件内容并删除一周前的行
        try (BufferedReader reader = Files.newBufferedReader(logPath)) {
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                if (isLineOlderThan(line, oneWeekAgo)) {
                    continue;
                }
                builder.append(line).append("\n");
            }
            // 将新的日志内容写回文件
            Files.copy(new ByteArrayInputStream(builder.toString().getBytes()), logPath,
                    StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private boolean isLineOlderThan(String line, long timestamp) {
        // 解析日志行中的时间戳
        String[] parts = line.split("\\s+", 3);
        if (parts.length < 2) {
            return false;
        }
        try {
            long lineTimestamp = LocalDateTime.parse(parts[0] + "T" + parts[1]).toEpochSecond(ZoneOffset.UTC);
            return lineTimestamp < timestamp;
        } catch (Exception e) {
            return false;
        }
    }
}

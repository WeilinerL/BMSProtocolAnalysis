package log;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AppRunningRecorder {
    private final static String FILENAME = "src/log/app_running_log/app_running_info.txt";
    private static SimpleDateFormat df = new SimpleDateFormat();

    /**
     * 将程序的运行信息写入app_running_info.txt文件中
     * @param message 日志信息
     */
    public static void writeLog(String message) {
        try{
            File file = new File(FILENAME);
            if (!file.exists()){
                file.createNewFile();
            }
            FileWriter fw = new FileWriter(file, true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter pw = new PrintWriter(bw);
            // This will add a new line to the file content
            pw.println(message + "   " + df.format(new Date()));
            pw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

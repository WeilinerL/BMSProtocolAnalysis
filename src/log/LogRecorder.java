package log;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LogRecorder {
	private String fileName = null;
	private RandomAccessFile raf = null;
	private static int count = 0;
	public LogRecorder(String threadName) {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
		synchronized (LogRecorder.class) {
			fileName = threadName+(++count)+df.format(new Date());
		}
	}

	public void writeLog(String message)  {
			try {
				if(raf==null)
					raf = new RandomAccessFile("src/log/polling_thread_log/"+fileName+".txt","rw");
				long offset = raf.length();
				raf.seek(offset);
				raf.writeBytes(message+"\r\n");
			} catch (IOException e) {
				try {
					RandomAccessFile raf = new RandomAccessFile("src/log/logFileErrorLog/"+fileName+".txt","rw");
					raf.writeBytes("Log:"+fileName+" can't be created successfully\r\n"+e.getMessage());
					raf.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
	}

	public static void writePropertiesLog(String message,String fileName) {
		try {
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
			RandomAccessFile raf = new RandomAccessFile("src/log/propertiesErrorLog/"+fileName+df.format(new Date())+".txt","rw");
			raf.writeBytes(message+"\r\n");
			raf.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void close() throws IOException {
		if(raf!=null)
			raf.close();
	}
}

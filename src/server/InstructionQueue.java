package server;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 线程安全队列容器 非阻塞算法 详情见 https://blog.csdn.net/qq_38293564/article/details/80798310
 * 指令队列 FIFO
 * @param <T>
 */
public class InstructionQueue<T> {
	
	private ConcurrentLinkedQueue<T> instructionQueue = new ConcurrentLinkedQueue<T>();

	/**
	 * 	指令入队
	 * @param in
	 */
	public  void add(T in) {
			instructionQueue.add(in);
	}
	
	/**
	 * 指令出队
	 * @return Instruction: 指令
	 */
	public T poll() {
		return instructionQueue.poll();
	}
}

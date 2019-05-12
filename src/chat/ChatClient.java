package chat;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MulticastSocket;

import chat.ReadingThread;

public class ChatClient {

	private MulticastSocket ms;
	private int port;
	private String host;
	
	public ChatClient(String host, int port) throws IOException {
		this.port = port;
		this.host = host;
		
		ms = new MulticastSocket(port);
		//ms.setLoopbackMode(true);
	}
	
	void joinGroup() {
		try {
			InetAddress group = InetAddress.getByName(host);
			ms.joinGroup(group);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	void leaveGroup() {
		try {
			InetAddress group = InetAddress.getByName(host);
			ms.leaveGroup(group);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws IOException {
		ChatClient chatClient = new ChatClient("224.0.1.0", 8080);
		chatClient.joinGroup();
		
		Thread writingThread = new Thread(new WritingThread(chatClient.ms, chatClient.host, chatClient.port));
		Thread readingThread = new Thread(new ReadingThread(chatClient.ms));
		
		writingThread.start();
		readingThread.start();
		
		while(true) {
			try {
			     Thread.sleep(10000);
			} catch (InterruptedException e) {
			      System.out.println("Main thread Interrupted");
			      chatClient.leaveGroup();
			}
		}
	}
}

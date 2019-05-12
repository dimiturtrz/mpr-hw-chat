package chat;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.nio.ByteBuffer;
import java.util.Scanner;

public class WritingThread implements Runnable {

	private MulticastSocket ms;
	private int port;
	private String host;

	private static final int MAX_BUFFER_SIZE = 4096;
	
	public WritingThread(MulticastSocket ms, String host, int port) throws IOException {
		this.port = port;
		this.host = host;
		
		this.ms = ms;
	}
	
	void pauseSocket() throws InterruptedException {
		try {
			InetAddress group = InetAddress.getByName(host);
			ms.leaveGroup(group);
			Thread.sleep(500);
			ms.joinGroup(group);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void sendMessage(String message) throws IOException, InterruptedException {
        byte [] inputBytes = message.getBytes();
		DatagramPacket p = new DatagramPacket(inputBytes, inputBytes.length);
		
		InetAddress a = InetAddress.getByName(host);
		p.setAddress(a);
		p.setPort(port);
		
		ms.send(p);
		pauseSocket();
	}
	
	public void sendFile(String filePath) throws IOException {
		File f = new File(filePath);
		System.out.println(f.getAbsolutePath());
		
		try (InputStream fileIn = new FileInputStream(f)) {

			InetAddress address = InetAddress.getByName(host);
			byte[] fileLength = longToBytes(f.length());
			
			System.out.println(fileLength.length);
			int serverRespSize = "SUCCESS".length();
			DatagramPacket serverResponse = new DatagramPacket(new byte[serverRespSize], 0, serverRespSize, address, port);
			String response = "FAILURE";
			do {
				DatagramPacket pSize = new DatagramPacket(fileLength, 0, fileLength.length, address, port);
				ms.send(pSize);
				ms.receive(serverResponse);
			} while (serverResponse.getLength() != serverRespSize && response.equals("SUCCESS"));
			
			System.out.println("here with "+ response);
			
			byte[] data = new byte[MAX_BUFFER_SIZE];
			int bytesRead = 0;
			
			while ((bytesRead = fileIn.read(data, 0, MAX_BUFFER_SIZE)) > 0) {
				DatagramPacket p = new DatagramPacket(data, 0, bytesRead, address, port);
				ms.send(p);
				try {
					Thread.sleep(2);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}		
		}
	}

	public byte[] longToBytes(long x) {
	    ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
	    buffer.putLong(x);
	    return buffer.array();
	}

	@Override
	public void run() {
		
		try (Scanner scanner = new Scanner(System.in)) {
			while(true) {
				System.out.println("enter message type (options are TEXT, IMAGE, VIDEO)");
		        String input = scanner.nextLine();
	        	sendMessage(input);
		        
		        switch(input) {
		        case "TEXT":
		        	System.out.println("enter text message");
		        	sendMessage("TEXT");
		        	input = scanner.nextLine();
		        	sendMessage(input);
		        	break;
		        case "IMAGE":
		        	System.out.println("enter path to image");
		        	sendMessage("FILE");
		        	input = scanner.nextLine();
		        	sendFile(input);
		        	break;
		        case "VIDEO":
		        	System.out.println("enter path to video");
		        	sendMessage("FILE");
		        	input = scanner.nextLine();
		        	sendFile(input);
		        	break;
		        }
			}
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}
}

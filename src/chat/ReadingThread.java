package chat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.nio.ByteBuffer;

public class ReadingThread implements Runnable {
	
	private MulticastSocket ms;

	private static final int MAX_BUFFER_SIZE = 4096;
	
	public ReadingThread(MulticastSocket ms) throws IOException {
		this.ms = ms;
	}
	
	void receiveText() throws IOException {
		DatagramPacket r = new DatagramPacket(new byte[1024], 1024);
		String input = "";
		do {
			ms.receive(r);
			input = new String(r.getData(), r.getOffset(), r.getLength());
		} while(input.equals("TEXT"));
		System.out.println(input);
	}
	
	void receiveFile() throws IOException, InterruptedException {
		DatagramPacket r = new DatagramPacket(new byte[8], 8);
		String input = "";
		do {
			ms.receive(r);
			input = new String(r.getData(), r.getOffset(), r.getLength());
		} while(input.equals("FILE"));

		File f = new File("receivedFile");
		try (OutputStream fileOut = new FileOutputStream(f)) {
			long fileSize = bytesToLong(r.getData());
			System.out.println("File size is: " + input);
			
			byte[] response = "SUCCESS".getBytes();
			r = new DatagramPacket(response, 0, response.length, r.getAddress(), r.getPort());
			ms.send(r);
			
			r = new DatagramPacket(new byte[MAX_BUFFER_SIZE], MAX_BUFFER_SIZE);
			
			int bytesReceived = 0;	
			long totalBytesReceived = 0L;
			do {
				ms.receive(r);
				byte[] data = r.getData();
				int offset = r.getOffset();
				int length = r.getLength();
				bytesReceived = length;
				fileOut.write(data, offset, length);
				totalBytesReceived += bytesReceived;
				//System.out.println("Received package from " + p.getAddress().getHostName() + ". The data is: " + new String(data, offset, length));
				//System.out.println(new String(data, offset, length));
				System.out.println(totalBytesReceived + " " + fileSize);
			} while (totalBytesReceived < fileSize);
			
			fileOut.flush();
			System.out.println("Total bytes received: " + totalBytesReceived + ". File path is: " + f.getAbsolutePath());
		}
			
		System.out.println(input);
	}

	public long bytesToLong(byte[] bytes) {
	    ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
	    buffer.put(bytes);
	    buffer.flip();//need flip 
	    return buffer.getLong();
	}

	@Override
	public void run() {

		try {
			DatagramPacket r = new DatagramPacket(new byte[1024], 1024);
			while(true) {
				ms.receive(r);
				String input = new String(r.getData(), r.getOffset(), r.getLength());
				Thread.sleep(500);
				switch(input) {
				case "TEXT":
					receiveText();
					break;
				case "FILE":
					receiveFile();
					break;
				}
			}
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}

}

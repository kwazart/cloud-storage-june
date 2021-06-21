package com.polozov.cloudstorage.lesson01;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {
	private final Socket socket;

	public ClientHandler(Socket socket) {
		this.socket = socket;
	}


	@Override
	public void run() {
		try (
				DataOutputStream out = new DataOutputStream(socket.getOutputStream());
				DataInputStream in = new DataInputStream(socket.getInputStream())
		) {
			System.out.printf("Client %s connected\n", socket.getInetAddress());
			while (true) {
				String command = in.readUTF();
				if ("upload".equals(command)) {
					uploading(out, in);
				}

				if ("download".equals(command)) {
					downloading(out, in);
				}
				if ("exit".equals(command)) {
					System.out.printf("Client %s disconnected correctly\n", socket.getInetAddress());
					break;
				}

				System.out.println(command);
//				out.writeUTF(command);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void downloading(DataOutputStream out, DataInputStream in) throws IOException {
		String filename = in.readUTF();
		try {
			File file = new File("server" + File.separator + filename);
			if (!file.exists()) {
				throw  new FileNotFoundException();
			}

			long fileLength = file.length();

			FileInputStream fis = new FileInputStream(file);

			out.writeLong(fileLength);

			int read;
			byte[] buffer = new byte[8 * 1024];
			while ((read = fis.read(buffer)) != -1) {
				out.write(buffer, 0, read);
			}

			out.flush();

			String status = in.readUTF();
			System.out.println("downloading status: " + status);
		} catch (FileNotFoundException e) {
			System.err.println("File not found - " + filename);
		}
	}

	private void uploading(DataOutputStream out, DataInputStream in) throws IOException {
		try {
			File file = new File("server"  + File.separator + in.readUTF());
			if (!file.exists()) {
				 file.createNewFile();
			}
			FileOutputStream fos = new FileOutputStream(file);

			long size = in.readLong();

			byte[] buffer = new byte[8 * 1024];

			for (int i = 0; i < (size + (buffer.length - 1)) / (buffer.length); i++) {
				int read = in.read(buffer);
				fos.write(buffer, 0, read);
			}
			fos.close();
			out.writeUTF("OK");
		} catch (Exception e) {
			out.writeUTF("FATAL ERROR");
		}
	}
}

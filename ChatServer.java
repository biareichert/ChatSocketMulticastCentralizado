import java.io.*;
import java.net.*;
import java.nio.file.*;

public class ChatServer {
	private static ServerSocket serverSocket = null;
	private static Socket clientSocket = null;
	private static final int maxClientsCount = 10;
	private static final clientThread[] threads = new clientThread[maxClientsCount];

	public static void main(String args[]) {
		int portNumber = 2223;

		if (args.length < 1) {
			System.out
				.println("Usage: java MultiThreadChatServer <portNumber>\n"
					+ "Now using port number=" + portNumber);
		} else {
				portNumber = Integer.valueOf(args[0]).intValue();
			}

		/*
	    * Open a server socket on the portNumber (default 2222). Note that we can
	    * not choose a port less than 1023 if we are not privileged users (root).
	    */

		try {
			serverSocket = new ServerSocket(portNumber);
		} catch (IOException e) {
			System.out.println("Problema ao acessar a porta.");
		}

		while (true) {
			try {
				clientSocket = serverSocket.accept();
				int i = 0;
				for (i = 0; i < maxClientsCount; i++)
					if (threads[i] == null) {
						(threads[i] = new clientThread(clientSocket, threads)).start();
						break;
					}

				if (i == maxClientsCount) {
					PrintStream os = new PrintStream(clientSocket.getOutputStream());
					os.println("Server too busy, try later");
					os.close();
					clientSocket.close();
				} else {
					PrintStream os = new PrintStream(clientSocket.getOutputStream());
					os.println("ok");
				}
			} catch (IOException e) {
				System.out.println(e);
			}
		}
	}
}

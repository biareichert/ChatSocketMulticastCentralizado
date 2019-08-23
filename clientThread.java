import java.io.*;
import java.net.*;
import java.nio.file.*;

class clientThread extends Thread {
	private DataInputStream is = null;
	private PrintStream os = null;
	private Socket clientSocket = null;
	private final clientThread[] threads;
	private final int maxClientsCount;
	private InetAddress address = null;
	private byte[] outBuf;
	private String name;
	private int contadorCliente = 1;

	private DatagramPacket outPacket = null;
	private DatagramSocket socket = null;

	public clientThread(Socket clientSocket, clientThread[] threads) throws SocketException {
		this.clientSocket = clientSocket;
		this.threads = threads;
		maxClientsCount = threads.length;
		socket = new DatagramSocket();
	}

	public void run() {
		try {
			is = new DataInputStream(clientSocket.getInputStream());
			os = new PrintStream(clientSocket.getOutputStream());

			int id = 0;
			while (threads[id] != this){
				id++;
			}

			while (true) {
				name = is.readLine().trim();
				boolean flagNome = false;

				for (int i = 0; i < maxClientsCount; i++)
					if (threads[i] != null && threads[i] != this && threads[i].name.equals(name)) {
						flagNome = true;
						os.println("Já existe um cliente com esse nome.");
						break;
					}
				if (flagNome == false){
					os.println("ok " + name + " " + (id + 1));
					break;
				}
			}
			MultiSender("*** A new user " + name + " entered the chat room !!! ***");
			//Arquivo
			String conteudo = "";
			while (true) {
				String line = is.readLine();

				if (line.startsWith("/quit")){
					break;
				}

				if (line.equals((char)0 + "")) {
					while (new File(name +"-0" + contadorCliente +".serv").exists()){
						contadorCliente++;
					}
					Files.write(new File(name +"-0" +contadorCliente +".serv").toPath(), conteudo.getBytes());
					MultiSender("< " + name+ ">: " + conteudo);
					conteudo = "";
					contadorCliente++;
				}
        MultiSender("<" + name + ">; " + line);

			}
			os.println("entrei aqui");
		  MultiSender("*** The user " + name + " is leaving the chat room !!! ***");

			//is.println("*** Bye " + name + " ***");



			for (int i = 0; i < maxClientsCount; i++){
				if (threads[i] == this) {
					threads[i] = null;
					break;
				}
			}

			clientSocket.close();
		} catch (IOException e) {
			System.err.println(e);
		}
	}

	private void MultiSender(String msg) throws IOException {
		outBuf = msg.getBytes();
		InetAddress address = InetAddress.getByName("224.0.0.2");
		outPacket = new DatagramPacket(outBuf, outBuf.length, address, 2223);
		socket.send(outPacket);
	}


}

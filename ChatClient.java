import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.HashMap;



public class ChatClient implements Runnable{

	private static Socket clientSocket = null;
	private static PrintStream os = null;
	private static DataInputStream is = null;
	private static BufferedReader inputLine = null;
	private static boolean closed = false;
	private static String name;
	private static MulticastSocket socket;
	private static byte[] inBuf;
	private static DatagramPacket inPacket = null;
	private static int contArq = -1;
	private static int contUs;
	private static HashMap<String, Integer> qntArq = new HashMap<String, Integer>();

	public static void main(String[] args) {
	 	int portNumber = 2223;
		String host = "localhost";

		if (args.length < 2) {
				System.out
								.println("Usage: java ChatClient <host> <portNumber>\n"
												+ "Now using host=" + host + ", portNumber=" + portNumber);
		} else {
				host = args[0];
				portNumber = Integer.valueOf(args[1]).intValue();
		}

		/*
    	* Open a socket on a given host and port. Open input and output streams.
      */

		try {
			clientSocket = new Socket(host, portNumber);
			inputLine = new BufferedReader(new InputStreamReader(System.in));
			os = new PrintStream(clientSocket.getOutputStream());
			is = new DataInputStream(clientSocket.getInputStream());

			//Multicast
			socket = new MulticastSocket(2223);
			InetAddress address = InetAddress.getByName("224.0.0.2");
			socket.joinGroup(address);

		} catch (IOException e) {
			System.err.println("Couldn't get I/O for connection to host " + host);
		}
		/*
		  * If everything has been initialized then we want to write some data to the
		  * socket we have opened a connection to on the port portNumber.
		  */
		if(clientSocket != null && os != null && is != null){
			try {
				String msg = is.readLine();

				if (msg.equals("ok")) {
					//Aqui verifica se o nome do cliente já existe
					while (true) {
						System.out.println("Enter your name.");
						name = inputLine.readLine().trim();
						os.println(name);
						msg = is.readLine();
						if (msg.startsWith("ok " + name + " ")) {
							contUs = Integer.parseInt(msg.split(" ")[2]);
							break;
						}
						System.err.println("Já existe um cliente com esse nome, informe outro.");
					}

					/* Create a thread to read from the server. */
					new Thread(new ChatClient()).start();

					while (!closed)
						try {
							String arquivo = inputLine.readLine();
							if (arquivo.equals("/quit")) {
								os.println("/quit");
								break;
							}
							//os.println(arquivo);
							//Leitura do arquivo
							String conteudoArq = new String(Files.readAllBytes(Paths.get(arquivo)));
							os.println(conteudoArq);
							os.println((char)0);

						} catch (IOException e) {
							System.err.println("IOException "+e);
						}
				} else
					System.out.println(msg);
				/*
	        * Close the output stream, close the input stream, close the socket.
	        */
				closed = true;
				os.close();
				is.close();
				clientSocket.close();
			} catch (IOException e) {
				System.err.println("IOException: " + e);
			}
		}
	}

	/*
    * Create a thread to read from the server. (non-Javadoc)
    *
    * @see java.lang.Runnable#run()
    */

	public void run() {
		/*
      * Keep on reading from the socket till we receive "Bye" from the
      * server. Once we received that then we want to break.
      */
		try {
			while (!closed) {
				//MulticastReceiver
				inBuf = new byte[256];
				inPacket = new DatagramPacket(inBuf, inBuf.length);
				socket.receive(inPacket);
				String msg = new String(inBuf, 0, inBuf.length);
				String[] saida = msg.split(": ");
				if (saida.length > 1) {
					String nome = saida[0].split(" ")[1];
					//Escrever no arquivo
					if (contArq == -1) {
						contArq = 1;
						while (Files.exists(Paths.get(name+"-0"+contArq+".client"))){
							contArq++;
						}
						qntArq.put(nome, contArq);
					}

					int qntSaida = qntArq.get(nome);
					String arqSaida = name+"-0"+qntSaida+".client";
					if (Files.exists(Paths.get(arqSaida))){
							Files.write(Paths.get(arqSaida), saida[1].getBytes(), StandardOpenOption.APPEND);
					}else{
							Files.createFile(Paths.get(arqSaida));
							Files.write(Paths.get(arqSaida), saida[1].getBytes(), StandardOpenOption.APPEND);
					}
					if (name.equals(name)){
						msg = msg.substring(1, msg.length());
					}
				}
				saida = msg.split(": ");
				String lines[] = msg.split("\n");
				int contador = 0;
				for(String i:lines){
					if(saida.length == 1 || contador == 0)
						System.out.println("" + i);
					else
						System.out.println(saida[0] + ": " + i);
					contador++;
				}
			}
			//closed = true;
		} catch (IOException e) {
			System.err.println("IOException: " + e);
		}
		socket.close();
	}
}

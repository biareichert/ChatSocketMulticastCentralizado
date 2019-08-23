TARGET = ChatServer.class ChatClient.class clientThread.class

make ChatServer:
		javac ChatServer.java;
		javac -Xlint clientThread.java;
		java ChatServer $(porta);

ChatClient:
		javac ChatClient.java;
		java ChatClient $(host) $(porta);
clean:
	rm -f *~ $(TARGET)

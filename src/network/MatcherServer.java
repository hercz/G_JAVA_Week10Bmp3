package network;

import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import search.ID3Tag;
import search.ID3TagProperty;
import search.Searcher;

public class MatcherServer {
	private Map<File, ID3Tag> hashMap;
	String pattern;

	MatcherServer() {
		Searcher searcher;
		try {
			ServerSocket serverSocket = new ServerSocket(1234);
			System.out.println("Server started...");
			Socket server = serverSocket.accept();
			System.out.println("Client connected.");

			ObjectInputStream ois = new ObjectInputStream(server.getInputStream());
			ObjectOutputStream oos = new ObjectOutputStream(server.getOutputStream());

			while (true) {
				if (ois.read() != -1) {
					Object object = ois.readObject();

					if (object instanceof Map) {
						hashMap = (Map<File, ID3Tag>) object;
					}

					else if (object instanceof String) {
						pattern = (String) object;
					}

					else if (object instanceof Set) {
						searcher = new Searcher((Set<ID3TagProperty>) object);
						List<File> result = new ArrayList<File>();
						for (File file : hashMap.keySet()) {
							ID3Tag tag = hashMap.get(file);
							if (searcher.matches(pattern, tag)) {
								result.add(file);
							}
						}
						oos.writeObject(result);
						String counter = String.format("Count: %d", result.size());
						oos.writeObject(counter);

					}
					else if (object instanceof Commands && (Commands) object == Commands.EXIT) {
						break;
					}
				}
			}
			System.out.println("Server closed!!");
			ois.close();
			oos.close();
			server.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void main(String[] args) {
		new MatcherServer();
	}
}

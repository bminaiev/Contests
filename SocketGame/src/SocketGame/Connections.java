package SocketGame;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class Connections {
	static PrintWriter out;
	static BufferedReader in, stdIn;

	public static class Event {
		boolean fromStdIn;
		MyTokenizer tokenizer;
		String line;

		public Event(boolean fromStdIn, String line) {
			super();
			this.fromStdIn = fromStdIn;
			this.line = line;
			this.tokenizer = new MyTokenizer(line);
		}

	}

	static void expect(String s) {
		try {
			String got = in.readLine();
			if (got.equals(s)) {
				return;
			}
			throw new AssertionError();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	MyTokenizer tokenizer;
	
	String nextToken() {
		while (tokenizer == null || !tokenizer.hasMoreElements()) {
			try {
				tokenizer = new MyTokenizer(in.readLine());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return tokenizer.nextToken();
	}

	static Event getNextEvent() throws IOException {
		if (in.ready()) {
			String s = in.readLine();
			System.err.println("get line from net: " + s);
			return new Event(false, s);
		} else {
			if (stdIn.ready()) {
				String s = stdIn.readLine();
				System.err.println("get line from stdin: " + s);
				return new Event(true, s);
			}
		}
		try {
			Thread.sleep(Constants.READ_TIMEOUT_MS);
		} catch (InterruptedException e) {

		}
		return getNextEvent();
	}

	static void sendToServer(String line) {
		System.err.println("send to net: " + line);
		out.println(line);
	}

	public static void createConnections() throws UnknownHostException,
			IOException {
		Socket socket = new Socket(Constants.HOSTNAME, Constants.PORT);
		out = new PrintWriter(socket.getOutputStream(), true);
		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		stdIn = new BufferedReader(new InputStreamReader(System.in));
	}
}
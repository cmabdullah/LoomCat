

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Logger;

public class AcceptRequest extends Thread {
	private static Logger LOGGER = Logger.getLogger(AcceptRequest.class.getName());
	private final int port;

	public AcceptRequest(int port) {
		this.port = port;
	}

	@Override
	public void run() {
		try {
			ServerSocket serverSocket = new ServerSocket(port);
			while (true) {
				try {
					Socket socket = serverSocket.accept();
					LoomCatApp.SOCKET_QUEUE.put(socket);
				} catch (InterruptedException e) {
					LOGGER.info("Request accept failed "+e.getLocalizedMessage());
				}
			}
		} catch (IOException e) {
			LOGGER.info("Server socket open failed "+e.getLocalizedMessage());
		}
	}
}

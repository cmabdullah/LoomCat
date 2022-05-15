
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

public class App {
	public static LinkedBlockingQueue<Socket> SOCKET_QUEUE = new LinkedBlockingQueue<>();
	private static Logger LOGGER = Logger.getLogger(App.class.getName());
	public static void main(String[] args) {
		AcceptRequest acceptRequest = new AcceptRequest(Config.getInstance().getPort());
		//acceptRequestThread
		Thread t = new Thread(acceptRequest);
		t.setName("Loom App");
		t.start();
//		int requestProcessor = Config.getInstance().getRequestProcessor();
		ProcessRequest processRequest = new ProcessRequest();
		try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
			LOGGER.info("request processor is ready to process requests");
			while (true) {
				try {
					Socket socket = SOCKET_QUEUE.take();
					LOGGER.info("new request" + LocalDateTime.now());
					executor.submit(() -> processRequest.startProcessing(socket));
				} catch (InterruptedException e) {
					LOGGER.info(e.getLocalizedMessage());
					e.printStackTrace();
				}
			}
		}
	}
}

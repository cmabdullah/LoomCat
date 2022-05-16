import java.net.Socket;
import java.time.LocalDateTime;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class TomCatApp {
	private static final Logger LOGGER = Logger.getLogger(TomCatApp.class.getName());

	public static void main(String[] args) {
		AcceptRequest acceptRequest = new AcceptRequest(Config.getInstance().getPort());
		//acceptRequestThread
		Thread t = new Thread(acceptRequest);
		t.setName("Old school App");
		t.start();
		int requestProcessor = Config.getInstance().getRequestProcessor();

		ProcessRequest processRequest = new ProcessRequest();

		try (var executor = Executors.newFixedThreadPool(1)) {
			LOGGER.info("request processor is ready to process requests");
			while (true) {
				try {
					Socket socket = LoomCatApp.SOCKET_QUEUE.take();
					LOGGER.info("new request" + LocalDateTime.now());

					Runnable runnable = () -> processRequest.startProcessing(socket);
					executor.submit(runnable);
				} catch (InterruptedException e) {
					LOGGER.info(e.getLocalizedMessage());
					e.printStackTrace();
				}
			}
		}
	}
}

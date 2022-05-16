import java.net.Socket;
import java.util.Random;
import java.util.logging.Logger;

public class ProcessRequest {
	private static Logger LOGGER = Logger.getLogger(ProcessRequest.class.getName());

	public boolean startProcessing(Socket socket) {

		try (MyHttpServletRequest myHttpServletRequest = new MyHttpServletRequest(
				socket.getInputStream());
				MyHttpServletResponse myHttpServletResponse = new MyHttpServletResponse(
						socket.getOutputStream())) {

			Thread.currentThread().setName(String.valueOf(new Random().ints(1,10)));
			LOGGER.info("Current thread info"+Thread.currentThread().getName());
			boolean isProcessingFinished = false;
			int position = myHttpServletRequest.getPosition();
			if (position > 0) {
				String uri = myHttpServletRequest.getUri();
				//http://localhost:8081
				if (uri.equalsIgnoreCase("/")) {
					//indexServlet
					MyHttpServlet myHttpServlet = new IndexServlet();
					myHttpServlet.doGet(myHttpServletRequest, myHttpServletResponse);
					isProcessingFinished = true;
				}
				//http://localhost:8081/rpc?a1=sylet&a2=city
				if (uri.startsWith("/rpc")) {
					//RPCServlet
					MyHttpServlet myHttpServlet = new RPCServlet();
					myHttpServlet.doGet(myHttpServletRequest, myHttpServletResponse);
					isProcessingFinished = true;
				}
			}
			//need to close socket
			return isProcessingFinished;
		} catch (Exception e) {
			LOGGER.info("Unable to close input/output stream");
			e.printStackTrace();
			return false;
		}
	}
}

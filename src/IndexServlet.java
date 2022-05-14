import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

public class IndexServlet extends MyHttpServlet {
	private static Logger LOGGER = Logger.getLogger(IndexServlet.class.getName());

	@Override
	public void doGet(
			MyHttpServletRequest myHttpServletRequest,
			MyHttpServletResponse myHttpServletResponse) {
		//business logic
		try {
			String host = Config.getInstance().getHost();
			String uri = myHttpServletRequest.getUri() + "index.html";
			Path path = Paths.get(host + File.separator + uri.substring(1));
			StringBuilder response;
			byte[] data;
			if (path.toFile().isFile()) {
				data = Files.readAllBytes(path);
				response = myHttpServletResponse.acceptHeader(data.length);
			} else {
				data = "resources not found".getBytes();
				response = myHttpServletResponse.errorHeader(data.length);
			}
			myHttpServletResponse.getOutputStream().write(response.toString().getBytes());
			myHttpServletResponse.getOutputStream().write(data);
		} catch (IOException e) {
			LOGGER.info("Resources not found "+e.getLocalizedMessage());
		}
	}
}

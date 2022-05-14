
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.SocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

public class RPCServlet extends MyHttpServlet {

	private static Logger LOGGER = Logger.getLogger(RPCServlet.class.getName());
	@Override
	public void doGet(
			MyHttpServletRequest myHttpServletRequest,
			MyHttpServletResponse myHttpServletResponse) {
		StringBuilder response;
		byte[] data;
		try {
			String host = Config.getInstance().getHost();
			String uri = myHttpServletRequest.getUri();
			//?a1=sylet&a2=city
			String lastPartOfThisUrl = uri.substring(uri.indexOf('?'));
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			DataOutputStream out = new DataOutputStream(baos);
			String httpResponse = httpCallV2(uri);
			data = httpResponse.getBytes(StandardCharsets.UTF_8);
			response = myHttpServletResponse.acceptHeader(data.length);
			myHttpServletResponse.getOutputStream().write(response.toString().getBytes());
			myHttpServletResponse.getOutputStream().write(data);
		} catch (IOException e) {
			LOGGER.info(e.getLocalizedMessage());
		}
	}

	@Deprecated
	private String httpCall(String lastPartOfThisUrl) {

		//http://localhost:8080/api/v1/product/add?a1=sylet&a2=city
		String url = Config.getInstance().getRpcUrl() + lastPartOfThisUrl;
		System.out.println("calling url " + url);
		try {
			HttpRequest request = HttpRequest.newBuilder().uri(new URI(url)).GET().build();
			HttpResponse<String> response = HttpClient.newHttpClient()
					.send(request, HttpResponse.BodyHandlers.ofString());
			System.out.println(response.body());
			return response.body();
		} catch (IOException | InterruptedException | URISyntaxException e) {
			e.printStackTrace();
		}
		return "";
	}

	//Ref: https://openjdk.java.net/jeps/353
	//It integrates with the existing buffer cache mechanism so that it doesn’t need to use the thread stack for I/O
	//exclude wrapper
	private String httpCallV2(String lastPartOfThisUrl) {

		//upstream calling url
		//http://localhost:8080/api/v1/product/add?a1=sylet&a2=city
		String url2 = Config.getInstance().getRpcUrl() + lastPartOfThisUrl;
		LOGGER.info("calling url " + url2);
		try {
			URL url = new URL(url2);
			//LOGGER.info("webclient calling url " + url2.toString() + " Request id : ");
			String host = url.getHost(), port = String.valueOf(url.getPort()), path = url.getPath(), filePath = url.getPath();

			SocketAddress inetSocketAddress = new InetSocketAddress(host, Integer.parseInt(port));
			try(SocketChannel webClientSocketChannel = SocketChannel.open()) {
				//webClientSocketChannel.configureBlocking(false); --> fuse for use loom
				if (webClientSocketChannel.connect(inetSocketAddress)) {
					LOGGER.info("Connected"); // Connected right-away: nothing else to do
				} else {
					LOGGER.info("connecting... to ");
				}
				ByteBuffer webClientRequestBuffer = prepareByteBufferForClientRequest(
						filePath, host, port);
				if (!webClientSocketChannel.isConnected()) {
					if (webClientSocketChannel.finishConnect()) { // Finish connection process
						LOGGER.info("done! with request ID ");
					} else {
						LOGGER.info("unfinished...");
					}
				}

				webClientSocketChannel.write(webClientRequestBuffer);
				LOGGER.info(
						"writing request header to call Upstream through webClient with RequestID ");
				if (!webClientRequestBuffer.hasRemaining()) {
					LOGGER.info(
							"switching to client read mode to read data from upstream for RequestID ");
				}

				//read
				LOGGER.info("start upstream response processing for request ID ");
				ByteBuffer readBuff = MappedByteBuffer.allocate(1500);

				try {
					StringBuilder stringBuilder = new StringBuilder();
					int length = webClientSocketChannel.read(readBuff);
					LOGGER.info("response length size is : " + length + " for request ID ");
					if (length == -1) {
//					closeConnection(webClientSocketChannel);
					} else if (length == 0) {
						// response is not prepared yet
						// this.selectionKey.interestOps(SelectionKey.OP_READ);
						// https://stackoverflow.com/questions/34490207/the-difference-of-socketchannel-read-in-async-and-sync-mode
					} else if (length > 0) {
						readBuff.flip();
						stringBuilder.append(StandardCharsets.UTF_8.decode(readBuff));

						String str = stringBuilder.toString();
						return str;
//					System.out.println("response from upstream  for request ID : "+requestId+" is \n" +str);

//					serverRequestProcessor.updateResponse(str);
//					System.out.println("closing webclient connection for request ID "+requestId);
//					closeConnection(webClientSocketChannel);
					}

				} catch (IOException e) {
//				closeConnection(webClientSocketChannel);
				}
			}catch (IOException e){
				LOGGER.info("webClientSocketChannel connection close issue "+e.getLocalizedMessage());
			}

		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
		return "";
	}

	private ByteBuffer prepareByteBufferForClientRequest(String path, String host, String port) {
		byte[] message = new String(
				"GET " + path + " HTTP/1.0\r\nHost:" + host + ":" + port + " \r\n\r\n").getBytes();
		return ByteBuffer.wrap(message);
	}
}

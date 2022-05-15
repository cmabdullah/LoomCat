import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.SocketAddress;
import java.net.URL;
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
			String httpResponse = httpCallV2(lastPartOfThisUrl);
			data = httpResponse.getBytes(StandardCharsets.UTF_8);
			response = myHttpServletResponse.acceptHeader(data.length);
			myHttpServletResponse.getOutputStream().write(response.toString().getBytes());
			myHttpServletResponse.getOutputStream().write(data);
		} catch (IOException e) {
			LOGGER.info(e.getLocalizedMessage());
		}
	}

	//Ref: https://openjdk.java.net/jeps/353
	//It integrates with the existing buffer cache mechanism so that it doesn’t need to use the thread stack for I/O
	//exclude wrapper
	private String httpCallV2(String lastPartOfThisUrl) {

		//upstream calling url
		//http://localhost:8080/api/v1/product/add?a1=sylet&a2=city
		//http://localhost:8086/api/v1/info/countryInfo?a1=Dhaka&Bangladesh
		String url2 = Config.getInstance().getRpcUrl() + lastPartOfThisUrl;
		LOGGER.info("calling url " + url2);
		String result = "";
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
						filePath+lastPartOfThisUrl, host, port);
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
						// https://stackoverflow.com/questions/34490207/the-difference-of-socketchannel-read-in-async-and-sync-mode
					} else if (length > 0) {
						readBuff.flip();
						stringBuilder.append(StandardCharsets.UTF_8.decode(readBuff));

						String str = stringBuilder.toString();
						LOGGER.info("rpc response "+str);
						result = str;
					}
				} catch (IOException e) {
				}
			}catch (IOException e){
				LOGGER.info("webClientSocketChannel connection close issue "+e.getLocalizedMessage());
			}

		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
		return result;
	}

	/**
	 *
	 * @param path /api/v1/info/countryInfo?a1=sylet&a2=city
	 * @param host localhost
	 * @param port 8087
	 * @return
	 */
	private ByteBuffer prepareByteBufferForClientRequest(String path, String host, String port) {
		byte[] message = new String(
				"GET " + path + " HTTP/1.0\r\n"+
						"User-Agent: PostmanRuntime/7.28.4\r\n"+
						"Accept: */*\r\n"+
						"Accept-Encoding: gzip, deflate, br\r\n"
						+"Host:" + host + ":" + port + " \r\n\r\n").getBytes();
		return ByteBuffer.wrap(message);
	}
}

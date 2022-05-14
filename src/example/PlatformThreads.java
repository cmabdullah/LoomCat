package example;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.IntStream;

/**
 * LoomVai
 * Created on 13/5/22 - Friday
 * User Khan, C M Abdullah
 * Ref:
 */
public class PlatformThreads {

	public static void main(String[] args) {
		PlatformThreads platformThreads = new PlatformThreads();
//		System.out.println(platformThreads.httpCall("a1=cm&a2=khulna"));//1m
//		System.out.println(platformThreads.httpCall("a1=city&a2=dhaka"));//30 s
//		System.out.println(platformThreads.httpCall("a1=Chittagong&a2=city"));//instant

		try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
//		try (var executor = Executors.newFixedThreadPool(4)) {
			Future<String> result1 = executor.submit(() -> platformThreads.httpCall("a1=cm&a2=khulna"));//1m
			Future<String> result2 = executor.submit(() -> platformThreads.httpCall("a1=city&a2=dhaka"));//30 s

			Future<String> result3 = executor.submit(() -> platformThreads.httpCall("a1=Chittagong&a2=city"));//instant

			IntStream.range(0, 200).forEach(i -> {
				executor.submit(() -> executor.submit(() -> platformThreads.httpCall("a1=Chittagong&a2=city"))
				);
			});
			System.out.println(result3.get());//instant
			System.out.println(result1.get());//1 minute wait
			System.out.println(result2.get());// 30 second wait
		} catch (ExecutionException e) {
			throw new RuntimeException(e);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	private String httpCall(String params) {
		try {
			String url = "http://localhost:8080/api/v1/product/add?"+params;
			System.out.println("calling url " + url);
			HttpRequest request = HttpRequest.newBuilder().uri(new URI(url)).GET().build();
			HttpResponse<String> response = HttpClient.newHttpClient()
					.send(request, HttpResponse.BodyHandlers.ofString());
			//System.out.println(response.body());
			return response.body();
		} catch (IOException | InterruptedException | URISyntaxException e) {
			e.printStackTrace();
		}
		return "";
	}
}



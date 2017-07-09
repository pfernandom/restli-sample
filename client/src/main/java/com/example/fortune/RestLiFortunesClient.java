package com.example.fortune;

import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.linkedin.common.callback.FutureCallback;
import com.linkedin.common.util.None;
import com.linkedin.parseq.Engine;
import com.linkedin.parseq.EngineBuilder;
import com.linkedin.parseq.Task;
import com.linkedin.parseq.batching.BatchingSupport;
import com.linkedin.r2.transport.common.Client;
import com.linkedin.r2.transport.common.bridge.client.TransportClientAdapter;
import com.linkedin.r2.transport.http.client.HttpClientFactory;
import com.linkedin.restli.client.ParSeqRestClient;
import com.linkedin.restli.client.ParSeqRestliClientBuilder;
import com.linkedin.restli.client.ParSeqRestliClientConfigBuilder;
import com.linkedin.restli.client.Request;
import com.linkedin.restli.client.Response;
import com.linkedin.restli.client.RestClient;

public class RestLiFortunesClient {
	/**
	 * This stand-alone app demos the client-side Rest.li API. To see the demo,
	 * run the server, then start the client
	 */
	private static RestClient restClient;
	private static HttpClientFactory http;
	
	public static void main(String[] args) throws Exception {

		// Generate a random ID for a fortune cookie, in the range 0-5
		long fortuneId = (long) (Math.random() * 5);

		// Construct a request for the specified fortune
		FortunesGetBuilder getBuilder = _fortuneBuilders.get();
		Request<Fortune> getReq = getBuilder.id(fortuneId).build();
		Request<Fortune> getReq2 = getBuilder.id( (long) (Math.random() * 5)).build();

		ParSeqRestClient _parseqClient = getClient("http://localhost:8080/");
		Task<Response<Fortune>> fortunesResponseTask = _parseqClient.createTask(getReq);
		Task<Response<Fortune>> fortunesResponseTask2 = _parseqClient.createTask(getReq2);

		Task<String> print = Task.par(fortunesResponseTask,fortunesResponseTask2)
					.map("map", (tuple) -> {
			Fortune fortune = tuple._1().getEntity();
			Fortune fortune2 = tuple._2().getEntity();
			return fortune+"-"+fortune2;
		})
		.andThen("print", (str)->{
			System.out.println(str);
		});

		Engine engine = getEngine();
		engine.run(print);
		engine.shutdown();
		engine.awaitTermination(5, TimeUnit.SECONDS);
		restClient.shutdown(new FutureCallback<None>());
		http.shutdown(new FutureCallback<None>());
	}

	private static Engine getEngine() {
		final int numCores = Runtime.getRuntime().availableProcessors();
		final ExecutorService taskScheduler = Executors.newFixedThreadPool(numCores + 1);
		final ScheduledExecutorService timerScheduler = Executors.newSingleThreadScheduledExecutor();

		return new EngineBuilder().setTaskExecutor(taskScheduler).setTimerScheduler(timerScheduler).build();
	}

	private static ParSeqRestClient getClient(String url) {

		// Create an HttpClient and wrap it in an abstraction layer
		http = new HttpClientFactory();

		final Client r2Client = new TransportClientAdapter(http.getClient(Collections.<String, String> emptyMap()));
		// Create a RestClient to talk to localhost:8080
		restClient = new RestClient(r2Client, url);

		ParSeqRestliClientConfigBuilder b = new ParSeqRestliClientConfigBuilder();
		return new ParSeqRestliClientBuilder().setRestClient(restClient).setBatchingSupport(new BatchingSupport())
				.setConfig(b.build()).build();
	}

	private static final FortunesBuilders _fortuneBuilders = new FortunesBuilders();
}
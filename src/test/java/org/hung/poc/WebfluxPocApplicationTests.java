package org.hung.poc;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import reactor.core.publisher.Flux;
import reactor.core.publisher.SynchronousSink;
import reactor.test.StepVerifier;
import reactor.test.publisher.TestPublisher;

@SpringBootTest
class WebfluxPocApplicationTests {

	@Test
	void contextLoads() {
	}
	
	@Test
	void FluxInternvalTest() {
		//Flux flux = Flux.range(1, 5).log();
		
		Flux flux = 
			Flux.interval(Duration.ofMillis(100l)).log();
		
		StepVerifier
			.create(flux)
			.expectNext(0l)
			.expectNext(1l)
			.expectNext(2l)
			.expectNext(3l)
			.expectNextCount(100)
			.expectNext(104l)
			.expectComplete()
			//.expectTimeout(Duration.ofMinutes(5l))
			.verify();
			//.verifyComplete();
	}
	
	@Test
	void testFluxGenerate() {
		Flux<String> flux2 = Flux.<String>generate(sink -> {
		}).log();
		
		Flux<String> flux = 
			Flux
			.<String,Integer>generate(
				() -> 1,
				(state,sink) -> {
					sink.next("Current state:"+state);
					if (state >= 100) {
						sink.complete();
					}
					return state + 1;
				}
			)
			.delayElements(Duration.parse("PT0.5S"))
			.log();
		
		StepVerifier
			.create(flux)
			.expectNextCount(100)
			.expectComplete()
			//.expectTimeout(Duration.ofMinutes(5l))
			.verify();
	}
	
	@Test
	void testFluxCreate() {
		
		ExecutorService executor = Executors.newFixedThreadPool(5);

		Flux<String> flux = Flux
			.<String>create(emitter -> {
				executor.execute(() -> {
					for (;;) {
						Thread currentThread = Thread.currentThread();
						emitter.next(currentThread.getName());
						try {
							currentThread.sleep((long)Math.random()*1000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				});
			})
			.delayElements(Duration.parse("PT0.5S"))
			.log();
		
		
		StepVerifier
			.create(flux)
			.expectNextCount(100)
			.expectComplete()
			//.expectTimeout(Duration.ofMinutes(5l))
			.verify();
	}
	
	@Test
	void testTestPublisher() {
		Flux<String> flux = 
			TestPublisher
			  .<String>create()
			  .next("1234")
			  .next("dfdsf")
			  .complete()
			  .flux()
			  .log();

		StepVerifier
			.create(flux)
			.expectNextCount(100)
			.expectComplete()
			//.expectTimeout(Duration.ofMinutes(5l))
			.verify();
		
	}

}

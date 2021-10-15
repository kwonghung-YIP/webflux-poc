package org.hung.poc;

import static org.springframework.web.reactive.function.server.RequestPredicates.contentType;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.config.ResourceHandlerRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

@Configuration
@EnableWebFlux
@Slf4j
public class WebFluxConfig implements WebFluxConfigurer {
	
	@Autowired
	private ObjectMapper objectMapper;

	@Bean
	public RouterFunction<ServerResponse> router() {
		ServerResponse.ok().bodyValue("Hello");
		return route()
				.GET("/echo",request -> ServerResponse.ok().bodyValue("Hello " + request.queryParam("name").orElse("nobody")))
				.POST("/person",contentType(MediaType.APPLICATION_JSON),request -> ServerResponse.ok().build())
				.GET("/text-stream", 
						request -> ServerResponse
							.ok()
							.contentType(MediaType.TEXT_EVENT_STREAM)
							.body(
									Flux
										.interval(Duration.ofMillis(500l))
										.map(v -> "Here:" + v)
										.log()
										,String.class)
				)
				.GET("/json-stream",
						request -> ServerResponse
							.ok()
							.contentType(MediaType.APPLICATION_NDJSON)
							.body(
								Flux
								  .interval(Duration.ofMillis(500))
								  .map(i -> {
									  Person person = new Person();
									  person.firstName = "John" + i;
									  //return person;
									  try {
										return objectMapper.writeValueAsString(person);
									  } catch (JsonProcessingException e) {
										log.error("",e);
										return e.getMessage();
									  }
								  })
								.log()
								,String.class)
				)
				.GET("/json-stream2",
						request -> ServerResponse
							.ok()
							.contentType(MediaType.APPLICATION_NDJSON)
							.body(
								Flux
								  .interval(Duration.ofMillis(500))
								  .map(i -> Collections.singletonMap("value", i))
								  .log()
								  ,new ParameterizedTypeReference<Map<String,Long>>(){})
				)
				.GET("/json-stream3",
						request -> ServerResponse
							.ok()
							.contentType(MediaType.APPLICATION_NDJSON)
							.body(
								Flux
								  .interval(Duration.ofMillis(500))
								  .map(i -> { 
								    Person person = new Person();
								    person.setFirstName("John"+i);
								    person.setDob(LocalDate.now());
								    return person;
								  })
								  .log()
								  ,Person.class)
				)
				.build();
	}
	
	
	
	@Override
	public void configureHttpMessageCodecs(ServerCodecConfigurer configurer) {
	//	configurer.customCodecs().
	}

	
	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry
		  .addResourceHandler("/**")
		  .addResourceLocations("/static/");
	}

	

	@Data
	public class Person {
		private String title;
		private String firstName;
		private String lastName;
		private LocalDate dob;
	}
}
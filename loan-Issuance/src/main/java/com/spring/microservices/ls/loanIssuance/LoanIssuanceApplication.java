package com.spring.microservices.ls.loanIssuance;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.ServiceInstance;
/*There is a confusing import with same name please check*/ 
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableEurekaClient /*This annotation is necessary for injecting the DiscoveryClient bean*/
@EnableFeignClients
public class LoanIssuanceApplication {

	public static void main(String[] args) {
		SpringApplication.run(LoanIssuanceApplication.class, args);
	}

	
	@Bean
	@Primary
	@LoadBalanced
	RestTemplate restTemplate() {
		return new RestTemplate();
	}
	
	@Bean
	RestTemplate restNonLoadTemplate() {
		return new RestTemplate();
	}
	
	
}

@RestController
class LoanIssuanceController {
	

	RestTemplate restTemplate;
	@Autowired
	@Qualifier("restNonLoadTemplate")
	RestTemplate restNonLoadTemplate;
	
	public final DiscoveryClient discoveryClient;
	
	@Autowired
	private  FraudClient fraudClient;
	
	@Autowired
	LoanIssuanceController(DiscoveryClient discoveryClient,	RestTemplate restTemplate){
		this.discoveryClient=discoveryClient;
		this.restTemplate= restTemplate;
	}
	
	@GetMapping("/resttemplate/loan/{id}/fraud")
	@SuppressWarnings("unchecked")
	List<String> restTemplateFrauds(@PathVariable("id") int id) {
		System.out.println("\n\n resttemplate: Got loan/" + id + "/fraud request\n\n");
		ServiceInstance instance = this.discoveryClient.getInstances("fraud-detection").stream().findFirst()
				.orElseThrow(() -> new IllegalStateException("No instance found!"));
		
		return new RestTemplate().getForObject(instance.getUri()+ "/frauds",List.class);
	}
	
	
	
	
	@GetMapping("/resttemplateNLB/loan/{id}/fraud")
	@SuppressWarnings("unchecked")
	List<String> restTemplateNLBFrauds(@PathVariable("id") int id) {
		System.out.println("\n\n resttemplateNLB: Got loan/" + id + "/fraud request\n\n");
		//this.discoveryClient.getInstances("fraud-detection").stream().forEach(System.out::println);
		ServiceInstance instance = this.discoveryClient.getInstances("fraud-detection").stream().findFirst()
				.orElseThrow(() -> new IllegalStateException("No instance found!"));
		return restNonLoadTemplate.getForObject(instance.getUri()+ "/frauds",List.class);
		
	}
	
	@GetMapping("/resttemplateLB/loan/{id}/fraud")
	@SuppressWarnings("unchecked")
	List<String> restTemplateLBFrauds(@PathVariable("id") int id) {
		System.out.println("\n\n resttemplateLB: Got loan/" + id + "/fraud request\n\n");
		//this.discoveryClient.getInstances("fraud-detection").stream().forEach(System.out::println);
		
		return restTemplate.getForObject("http://fraud-detection/frauds",List.class);
		
	}
	
	@GetMapping("/openFeign/loan/{id}/fraud")
	@SuppressWarnings("unchecked")
	List<String> openFeignFrauds(@PathVariable("id") int id) {
		System.out.println("\n\n resttemplateLB: Got loan/" + id + "/fraud request\n\n");
		//this.discoveryClient.getInstances("fraud-detection").stream().forEach(System.out::println);
		return this.fraudClient.frauds();
	}
}

//Add the feign starter dependency in pom.xml
@FeignClient("fraud-detection")
interface FraudClient {

	@GetMapping("/frauds")
	List<String> frauds();
}


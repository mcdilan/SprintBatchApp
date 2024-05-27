package cl.batch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
 
//@RequiredArgsConstructor
@SpringBootApplication
public class SpringBatchAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringBatchAppApplication.class, args);
	}
 
//	private final JobLauncher jobLauncher;
//	private final Job job;
//	
//	@Bean
//	CommandLineRunner init(){
//		return args -> {
//			JobParameters jobParameters = new JobParametersBuilder()
//	                .addLong("startAt", System.currentTimeMillis())
//	                .addDate("date", new Date())
//	                .toJobParameters();
//			
//			jobLauncher.run(job, jobParameters);
//		};	
//	}

}

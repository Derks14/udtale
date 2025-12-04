package udtale;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import udtale.config.exceptions.GlobalUncaughtHandler;

@SpringBootApplication
@EnableMongoAuditing

public class Application {

	public static void main(String[] args) {

		Thread.setDefaultUncaughtExceptionHandler(new GlobalUncaughtHandler());
		SpringApplication.run(Application.class, args);
	}

}

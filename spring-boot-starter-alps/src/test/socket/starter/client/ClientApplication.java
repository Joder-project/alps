package starter.client;

import lombok.extern.slf4j.Slf4j;
import org.alps.starter.AlpsClient;
import org.alps.starter.ClientSessionManager;
import org.alps.starter.anno.AlpsModule;
import org.alps.starter.anno.Command;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ActiveProfiles;

@SpringBootApplication
@AlpsClient
@ActiveProfiles("client")
public class ClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClientApplication.class, args);
    }

}

@Slf4j
@AlpsModule(module = "User")
class MyController {

    @Command(command = 2, type = Command.Type.FORGET)
    public void hello(String message) {
        log.info("receive msg: {}", message);
    }
}

@Component
@Slf4j
class Runner implements CommandLineRunner {

    private final ClientSessionManager sessionManager;

    Runner(ClientSessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @Override
    public void run(String... args) throws Exception {
        var session = sessionManager.session("User").get();
        var response = session.request(1).data("111").send().get();
        log.info("Response: {}", response.data(String.class));
    }
}

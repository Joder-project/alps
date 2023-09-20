package starter.client;

import com.google.protobuf.StringValue;
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

import java.util.concurrent.Flow;

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
    public void hello(StringValue message) {
        log.info("receive msg: {}", message.getValue());
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
        var response = session.request(1).data(StringValue.of("111")).send(StringValue.class);
        log.info("Response: {}", response.getValue());

        session.stream(5).data(StringValue.of("1"))
                .send(StringValue.class)
                .subscribe(new Flow.Subscriber<>() {
                    @Override
                    public void onSubscribe(Flow.Subscription subscription) {

                    }

                    @Override
                    public void onNext(StringValue item) {
                        log.info("receive stream: {}", item.getValue());
                    }

                    @Override
                    public void onError(Throwable throwable) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }
}

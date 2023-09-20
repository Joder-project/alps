package starter.server;

import com.google.protobuf.StringValue;
import lombok.extern.slf4j.Slf4j;
import org.alps.starter.AlpsExchange;
import org.alps.starter.AlpsServer;
import org.alps.starter.anno.AlpsModule;
import org.alps.starter.anno.Command;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.Flow;

@SpringBootApplication
@AlpsServer
@ActiveProfiles("server")
public class ServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServerApplication.class, args);
    }
}

@AlpsModule(module = "User")
@Slf4j
class MyController {


    @Command(command = 1, type = Command.Type.REQUEST_RESPONSE)
    public StringValue hello(StringValue message, AlpsExchange exchange) {
        exchange.session().forget(2).data(StringValue.of("I am Server")).send();

        return StringValue.of("hello, " + message);
    }

    @Command(command = 3, type = Command.Type.FORGET)
    public void helloForget(StringValue message, AlpsExchange exchange) {
        exchange.session().forget(2).data(StringValue.of("I am Server")).send();
    }

    @Command(command = 5, type = Command.Type.STREAM)
    public Flow.Publisher<StringValue> helloStream(StringValue message, AlpsExchange exchange) {
        return subscriber -> {
            int n = 0;
            while (true) {
                try {
                    subscriber.onNext(StringValue.of("Hello " + n));
                    Thread.sleep(1000L);
                    n++;
                } catch (Exception e) {
                    log.info("send stream: {}", n);
                }
            }
        };
    }
}
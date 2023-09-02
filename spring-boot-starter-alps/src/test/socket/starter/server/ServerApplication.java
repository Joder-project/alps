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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

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
    public Mono<StringValue> hello(StringValue message, AlpsExchange exchange) {
        return exchange.session().forget(2).data(StringValue.of("I am Server")).send()
                .thenReturn(StringValue.of("hello, " + message));
    }

    @Command(command = 3, type = Command.Type.FORGET)
    public void helloForget(StringValue message, AlpsExchange exchange) {
        exchange.session().forget(2).data(StringValue.of("I am Server")).send().subscribe();
    }

    @Command(command = 5, type = Command.Type.STREAM)
    public Flux<StringValue> helloStream(StringValue message, AlpsExchange exchange) {
        return Flux.interval(Duration.ofSeconds(1))
                .map(n -> StringValue.of("Hello " + n))/*
                .doOnNext(n -> log.info("send stream: {}", n))*/;
    }
}
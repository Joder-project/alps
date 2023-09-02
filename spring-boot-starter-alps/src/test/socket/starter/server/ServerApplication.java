package starter.server;

import com.google.protobuf.StringValue;
import org.alps.starter.AlpsExchange;
import org.alps.starter.AlpsServer;
import org.alps.starter.anno.AlpsModule;
import org.alps.starter.anno.Command;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.test.context.ActiveProfiles;

@SpringBootApplication
@AlpsServer
@ActiveProfiles("server")
public class ServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServerApplication.class, args);
    }
}

@AlpsModule(module = "User")
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
}
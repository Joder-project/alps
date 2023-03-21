package starter.server;

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
    public String hello(String message, AlpsExchange exchange) {
        exchange.session().forget(2).data("I am Server").send();
        return "hello, " + message;
    }
}
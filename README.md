# Alps

该项目旨在提供一个可以对通信进行分模块的处理能力，方便对不同通信的管理。比如登录(Login)、玩家(Player)
模块，可以定义不同模块（token、version）,而不需要采用不同的指令号区间进行区别。

目前属于不成熟版本，代码仅供学习、交流使用。

## 提供功能

* [x] 支持 QUIC
* [x] 支持Protobuf编解码
* [x] 提供自定义帧拓展能力。
* [x] Spring Boot 依赖
* [x] Forget 请求不管响应信息
* [x] Request-Response 请求响应
* [x] Stream 流传输


## Example

Server

```java
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import org.alps.core.frame.ForgetFrame;
import org.alps.core.frame.RequestFrame;
import org.alps.core.frame.ResponseFrame;
import org.alps.core.socket.netty.server.AlpsTcpServer;
import org.alps.core.socket.netty.server.NettyServerConfig;

import java.util.Map;

public class Server {

    public static void main(String[] args) {
        var dataCoderFactory = new AlpsDataCoderFactory();
        var frameFactory = new FrameCoders(dataCoderFactory);
        var routerDispatcher = new RouterDispatcher();
        var listenerHandler = new FrameListeners(routerDispatcher);
        var config = new AlpsConfig();
        config.getModules().add(new AlpsConfig.ModuleConfig((short) 1, (short) 1, 1L));
        routerDispatcher.addRouter(new Router() {
            @Override
            public short module() {
                return 1;
            }

            @Override
            public int command() {
                return 1;
            }

            @Override
            public void handle(AlpsEnhancedSession session, CommandFrame frame) {
                if (frame instanceof ForgetFrame forgetFrame) {

                } else if (frame instanceof RequestFrame requestFrame) {
                    var metadata = requestFrame.metadata();
                    int id = session.nextId();
                    session.receive(new ResponseFrame(id, requestFrame.id(),
                            new AlpsMetadataBuilder().isZip(metadata.isZip())
                                    .verifyToken(metadata.verifyToken())
                                    .version(metadata.version())
                                    .containerCoder(metadata.containerCoder())
                                    .coder(metadata.coder())
                                    .frameType(FrameCoders.DefaultFrame.RESPONSE.frameType)
                                    .frame(ResponseFrame.toBytes(id, requestFrame.id()))
                                    .build()
                            , requestFrame.data()));
                }
            }
        });
        var nettyServerConfig = new NettyServerConfig();
        nettyServerConfig.setPort(6195);
        nettyServerConfig.setOptionSettings(Map.of(
                ChannelOption.SO_BACKLOG, 128
        ));
        nettyServerConfig.setChildOptionSettings(Map.of(
                ChannelOption.SO_KEEPALIVE, true
        ));

        var enhancedSessionFactory = new DefaultEnhancedSessionFactory(frameFactory, dataCoderFactory, listenerHandler, config);
        var server = new NettyAlpsServer(new NioEventLoopGroup(1),
                new NioEventLoopGroup(12),
                new NioEventLoopGroup(12),
                nettyServerConfig, enhancedSessionFactory,
                enhancedSessionFactory.config.getModules().stream().map(AlpsConfig.ModuleConfig::getModule).toList());
        server.start();
    }
}
```

Client

```java
import io.netty.channel.nio.NioEventLoopGroup;
import org.alps.core.socket.netty.client.AlpsTcpClient;
import org.alps.core.socket.netty.client.NettyClientConfig;

import java.util.concurrent.ExecutionException;

public class A {

    public static void main(String[] args) throws Exception {
        var dataCoderFactory = new AlpsDataCoderFactory();
        var frameFactory = new FrameCoders(dataCoderFactory);
        var routerDispatcher = new RouterDispatcher();
        var listenerHandler = new FrameListeners(routerDispatcher);
        var config = new AlpsConfig();
        config.getModules().add(new AlpsConfig.ModuleConfig((short) 1, (short) 1, 1L));

        var nettyServerConfig = new NettyClientConfig();
        nettyServerConfig.setPort(6195);
        nettyServerConfig.setHost("127.0.0.1");

        var enhancedSessionFactory = new DefaultEnhancedSessionFactory(frameFactory, dataCoderFactory, listenerHandler, config);
        var client = new NettyAlpsClient(new NioEventLoopGroup(2), nettyServerConfig, enhancedSessionFactory,
                enhancedSessionFactory.config.getModules().stream().map(AlpsConfig.ModuleConfig::getModule).toList());
        client.start();
        while (!client.isReady()) {
        }
        var session = client.session((short) 1).map(e -> ((AlpsEnhancedSession) e)).get();
        session.forget(1).data(1).send().get();
        session.request(1).data(1).send(int.class).get();
    }
}
```

## 结合SpringBoot Example

```yaml
alps:
  modules:
    - code: 1
      name: 'User'
      verify-token: 1
      version: 1

---
spring:
  config:
    activate:
      on-profile: server
alps:
  server:
    port: 6195

---

spring:
  config:
    activate:
      on-profile: client

alps:
  client:
    host: 'localhost'
    port: 6195

```

Server

```java
import org.alps.starter.AlpsExchange;
import org.alps.starter.AlpsServer;
import org.alps.starter.anno.AlpsModule;
import org.alps.starter.anno.Command;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.test.context.ActiveProfiles;

@SpringBootApplication
@AlpsServer // 标记服务端
public class ServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServerApplication.class, args);
    }
}

/**
 * 定义controller
 */
@AlpsModule(module = "User")
class MyController {

    @Command(command = 1, type = Command.Type.REQUEST_RESPONSE)
    public String hello(String message, AlpsExchange exchange) {
        exchange.session().forget(2).data("I am Server").send();
        return "hello, " + message;
    }
}
```

Client

```java
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
@AlpsClient // 标记客户端
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

```
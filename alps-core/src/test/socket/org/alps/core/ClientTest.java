package org.alps.core;

import com.google.protobuf.Int32Value;
import lombok.extern.slf4j.Slf4j;
import org.alps.core.socket.netty.client.AlpsTcpClient;

import java.util.Collections;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
public class ClientTest {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        AtomicReference<AlpsTcpClient> client = new AtomicReference<>();
        var thread = new Thread(() -> {

        });
        thread.start();
        while (client.get() == null || client.get().isNotReady()) {

        }
        var session = client.get().session("1").map(e -> ((AlpsEnhancedSession) e)).orElseThrow();
        session.forget(1).data(1).send();
        var ret = session.request(2).data(1)
                .send(Int32Value.class);
        log.info("receive: {}", ret);

    }

    static class DefaultEnhancedSessionFactory implements EnhancedSessionFactory {

        final FrameCoders frameCoders;
        final AlpsDataCoderFactory dataCoderFactory;
        final FrameListeners frameListeners;
        final AlpsConfig config;

        DefaultEnhancedSessionFactory(RouterDispatcher routerDispatcher) {
            this.dataCoderFactory = new AlpsDataCoderFactory();
            this.config = new AlpsConfig();
            this.config.getDataConfig().setEnabledZip(true);
            this.config.getMetaDataConfig().setEnabledZip(true);
            this.config.getModules().add("1");
            this.frameCoders = new FrameCoders();
            this.frameListeners = new FrameListeners(routerDispatcher);
        }

        @Override
        public AlpsEnhancedSession create(AlpsSession session) {
            return new AlpsEnhancedSession(session, frameCoders, dataCoderFactory, frameListeners, new SessionListeners(Collections.emptyList()), config);
        }
    }

}

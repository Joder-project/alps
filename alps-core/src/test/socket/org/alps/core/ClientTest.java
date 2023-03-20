package org.alps.core;

import lombok.extern.slf4j.Slf4j;
import org.alps.core.socket.netty.client.NettyAlpsClient;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
public class ClientTest {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        AtomicReference<NettyAlpsClient> client = new AtomicReference<>();
        var thread = new Thread(() -> {

        });
        thread.start();
        while (client.get() == null || !client.get().isReady()) {

        }
        var session = client.get().session(((short) 1)).map(e -> ((AlpsEnhancedSession) e)).orElseThrow();
        session.forget(1).data(1).send().get();
        var ret = session.request(2).data(1).send(int.class).get();
        log.info("receive: {}", ret);

    }

    static class DefaultEnhancedSessionFactory implements EnhancedSessionFactory {

        final FrameCoders frameCoders;
        final AlpsDataCoderFactory dataCoderFactory;
        final FrameListeners frameListeners;
        final AlpsConfig config;

        DefaultEnhancedSessionFactory(RouterDispatcher routerDispatcher) {
            this.dataCoderFactory = new AlpsDataCoderFactory();
            this.frameListeners = new FrameListeners(routerDispatcher);
            this.config = new AlpsConfig();
            this.config.getDataConfig().setEnabledZip(true);
            this.config.getMetaDataConfig().setEnabledZip(true);
            this.config.getModules().add(new AlpsConfig.ModuleConfig((short) 1, (short) 1, 1L));
            this.frameCoders = new FrameCoders(dataCoderFactory);
        }

        @Override
        public AlpsEnhancedSession create(AlpsSession session) {
            return new AlpsEnhancedSession(session, frameCoders, dataCoderFactory, frameListeners, config);
        }
    }

}

package org.alps.core.socket.netty.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.alps.core.AlpsConfig;
import org.alps.core.AlpsDataCoderFactory;
import org.alps.core.EnhancedSessionFactory;
import org.alps.core.common.AlpsSocketException;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class AlpsTcpClient extends AbstractAlpsClient {

    private final NioEventLoopGroup eventExecutors;
    private Bootstrap bootstrap;
    private NioSocketChannel socketChannel;

    public AlpsTcpClient(NioEventLoopGroup eventExecutors, NettyClientConfig clientConfig, EnhancedSessionFactory sessionFactory,
                         List<AlpsConfig.ModuleConfig> supportModules, AlpsDataCoderFactory coderFactory) {
        super(clientConfig, sessionFactory, supportModules, coderFactory);
        this.eventExecutors = eventExecutors;
    }

    @Override
    public void start() {
        //创建bootstrap对象，配置参数
        this.bootstrap = new Bootstrap();
        //设置线程组
        bootstrap.group(eventExecutors)
                //设置客户端的通道实现类型
                .channel(NioSocketChannel.class)
                //使用匿名内部类初始化通道
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        handleChannel(ch);
                    }
                });
        log.debug("Start client finish!");
        //连接服务端
        ChannelFuture channelFuture = null;
        try {
            channelFuture = bootstrap.connect(clientConfig.getHost(), clientConfig.getPort()).sync();
        } catch (InterruptedException e) {
            throw new AlpsSocketException(e);
        }

        //对通道关闭进行监听
        socketChannel = ((NioSocketChannel) channelFuture.channel());
        try {
            isStart.await();
            log.info("Connect Server=> {}:{}", clientConfig.getHost(), clientConfig.getPort());
        } catch (Exception e) {
            log.error("await error", e);
        }

    }

    @Override
    public CompletableFuture<Void> close() {
        return CompletableFuture.runAsync(() -> {
            eventExecutors.shutdownGracefully();
            try {
                socketChannel.closeFuture().sync();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }


}

package org.alps.core.socket.netty.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.alps.core.AlpsDataCoderFactory;
import org.alps.core.EnhancedSessionFactory;
import org.alps.core.common.AlpsException;
import org.alps.core.common.AlpsSocketException;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class AlpsTcpServer extends AbstractAlpsServer {

    private final EventLoopGroup bossGroup;
    private final EventLoopGroup workerGroup;

    private ServerBootstrap bootstrap;
    private ChannelFuture serverFeature;

    public AlpsTcpServer(EventLoopGroup bossGroup, EventLoopGroup workerGroup,
                         NettyServerConfig serverConfig, EnhancedSessionFactory sessionFactory,
                         List<String> supportModules, AlpsDataCoderFactory coderFactory) {
        super(serverConfig, coderFactory, sessionFactory, supportModules);
        this.bossGroup = bossGroup;
        this.workerGroup = workerGroup;
    }

    @Override
    public void start() {
        //创建服务端的启动对象，设置参数
        this.bootstrap = new ServerBootstrap();
        //设置两个线程组boosGroup和workerGroup
        bootstrap.group(bossGroup, workerGroup)
                //设置服务端通道实现类型
                .channel(NioServerSocketChannel.class);
//                .channelFactory(AlpsNioServerSocketChannel::new);
        serverConfig.getOptionSettings().forEach((k, v) -> {
            //设置线程队列得到连接个数
            bootstrap.option((ChannelOption<Object>) k, v);
        });
        serverConfig.getChildOptionSettings().forEach((k, v) -> {
            //设置保持活动连接状态
            bootstrap.childOption((ChannelOption<Object>) k, v);
        });
        //使用匿名内部类的形式初始化通道对象
        bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel socketChannel) throws Exception {
                handleChannel(socketChannel);
            }
        });

        //绑定端口号，启动服务端
        try {
            this.serverFeature = bootstrap.bind(serverConfig.getPort()).sync();
        } catch (InterruptedException e) {
            throw new AlpsSocketException(e);
        }
        log.info("Server start finish. Listening port: {}", serverConfig.getPort());
    }

    @Override
    public CompletableFuture<Void> close() {
        return CompletableFuture.runAsync(() -> {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            //对关闭通道进行监听
            try {
                this.serverFeature.channel().closeFuture().sync();
            } catch (InterruptedException e) {
                throw new AlpsException(e);
            }
        });
    }


}

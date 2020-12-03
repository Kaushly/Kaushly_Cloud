package nettwork;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import utils.CallBack;

import io.netty.channel.socket.SocketChannel;
import utils.Command;

public class NetworkNetty {
    private SocketChannel channel;

    public NetworkNetty(CallBack onMessageReceivedCallBack) {
        Thread t = new Thread(() -> {
            EventLoopGroup worker = new NioEventLoopGroup();

            try{
                Bootstrap bsb = new Bootstrap();
                bsb.group(worker)
                        .channel(NioSocketChannel.class)
                        .handler(new ChannelInitializer<io.netty.channel.socket.SocketChannel>() {
                            @Override
                            protected void initChannel(io.netty.channel.socket.SocketChannel socketChannel) throws Exception {
                                channel = socketChannel;
                                socketChannel.pipeline().addLast(
                                        new ObjectDecoder(ClassResolvers.cacheDisabled(null)),
                                        new ObjectEncoder(),
                                        new CommandInboundHandler(onMessageReceivedCallBack)

                                );
                            }
                        });
                ChannelFuture future = bsb.connect("localhost", 8189).sync();
                future.channel().closeFuture().sync();
            } catch (Exception e) {
            } finally {
                worker.shutdownGracefully();
            }
        });
//        t.setDaemon(true);
        t.start();
    }

    public void sendCommand(Command command) {
        channel.writeAndFlush(command);
    }


    public void close() {
        channel.close();
    }

}

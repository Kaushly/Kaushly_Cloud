import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

public class Server {
    private final static String HOST = "localhost";
    private final static int PORT = 8189;

    public void start() throws InterruptedException {
        EventLoopGroup auth = new NioEventLoopGroup(1);
        EventLoopGroup worker = new NioEventLoopGroup();
        UserAUTH userAUTH = new UserAUTH();

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(auth, worker)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline()
                                    .addLast(new ObjectDecoder(ClassResolvers.cacheDisabled(null)),
                                            new ObjectEncoder(),
                                            new CloudServerHandler(userAUTH));
                        }
                    });
            ChannelFuture future = b.bind(HOST, PORT).sync();
            System.out.println("Server started");
            future.channel().closeFuture().sync();
        }finally {
            auth.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        new Server().start();
    }
}

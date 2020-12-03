import controllers.Controller;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import utils.Command;

import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CloudServerHandler extends ChannelInboundHandlerAdapter {

    private UserAUTH userAUTH;
    private Path userRoot;
    private static final Path ROOT_SERVER = Paths.get("");
    private Path currentDir;

    public CloudServerHandler(UserAUTH userAUTH) {
        this.userAUTH = userAUTH;

    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Client accepted");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Client de-accepted");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof Command) {
            Command cmd = (Command) msg;
            switch (cmd.getType()) {
                case AUTH: {
                    try {
                        AuthInfo authInfo = new AuthInfo(cmd.getArgs());

                        if (userAUTH.checkUser(authInfo.getName(), authInfo.getPassword())) {
                            userRoot = ROOT_SERVER.resolve(authInfo.getName());
                            if (!Files.exists(userRoot)) {
                                Files.createDirectory(userRoot);
                            }
                            currentDir = Paths.get(userRoot.toString());

                            ctx.channel().writeAndFlush(Command.generate(Command.CommandType.AUTH_OK, currentDir.toString()));
                        } else {
                            ctx.channel().writeAndFlush(Command.generate(Command.CommandType.ERROR, "Не правильный логин, пароль."));
                        }
                    } catch (Exception e) {
                        ctx.channel().writeAndFlush(Command.generate(Command.CommandType.ERROR, e.getMessage()));
                    }
                    break;
                }

                case REGISTER: {
                    try {
                        userAUTH.registerUser(cmd.getArgs()[0], cmd.getArgs()[1]);
                        userRoot = ROOT_SERVER.resolve(cmd.getArgs()[0]);
                        if (!Files.exists(userRoot)) {
                            Files.createDirectory(userRoot);
                        }
                        currentDir = Paths.get(userRoot.toString());
                        ctx.channel().writeAndFlush(Command.generate(Command.CommandType.AUTH_OK, currentDir.toString()));
                    } catch (Exception e) {
                        ctx.channel().writeAndFlush(Command.generate(Command.CommandType.ERROR, e.getMessage()));
                    }
                    break;
                }
            }
        }
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}

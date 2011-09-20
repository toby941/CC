/*
 * Copyright 2011 MITIAN Technology, Co., Ltd. All rights reserved.
 */


import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 * NBServer.java
 * 
 * @author baojun
 */
public class NBServer {
    int port = 8090;
    int BUFFERSIZE = 1024;
    Selector selector = null;
    ServerSocketChannel serverChannel = null;
    HashMap clientChannelMap = null;// 用来存放每一个客户连接对应的套接字和通道

    public NBServer(int port) {
        clientChannelMap = new HashMap();
        this.port = port;
    }

    public void initialize() throws IOException {
        // 初始化，分别实例化一个选择器，一个服务器端可选择通道
        selector = Selector.open();
        serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);
        InetAddress localhost = InetAddress.getLocalHost();
        InetSocketAddress isa = new InetSocketAddress(localhost, port);
        serverChannel.socket().bind(isa);// 将该套接字绑定到服务器某一可用端口
    }

    // 结束时释放资源
    @Override
    public void finalize() throws IOException {
        serverChannel.close();
        selector.close();
    }

    // 将读入字节缓冲的信息解码
    public String decode(ByteBuffer byteBuffer) throws CharacterCodingException {
        Charset charset = Charset.forName("ISO-8859-1");
        CharsetDecoder decoder = charset.newDecoder();
        CharBuffer charBuffer = decoder.decode(byteBuffer);
        String result = charBuffer.toString();
        return result;
    }

    // 监听端口，当通道准备好时进行相应操作
    public void portListening(String data) throws IOException,
            InterruptedException {
        // 服务器端通道注册OP_ACCEPT事件
        SelectionKey acceptKey =
                serverChannel.register(selector, SelectionKey.OP_ACCEPT);
        // 当有已注册的事件发生时,select()返回值将大于0
        while (acceptKey.selector().select() > 0) {
            System.out.println("event happened");
            // 取得所有已经准备好的所有选择键
            Set readyKeys = selector.selectedKeys();
            // 使用迭代器对选择键进行轮询
            Iterator i = readyKeys.iterator();
            while (i.hasNext()) {
                SelectionKey key = (SelectionKey) i.next();
                i.remove();// 删除当前将要处理的选择键
                if (key.isAcceptable()) {// 如果是有客户端连接请求
                    System.out.println("more client connect in!");
                    ServerSocketChannel nextReady =
                            (ServerSocketChannel) key.channel();
                    // 获取客户端套接字
                    Socket s = nextReady.accept().socket();
                    // 设置对应的通道为异步方式并注册感兴趣事件
                    s.getChannel().configureBlocking(false);
                    SelectionKey readWriteKey =
                            s.getChannel().register(
                                    selector,
                                    SelectionKey.OP_READ
                                            | SelectionKey.OP_WRITE);
                    // 将注册的事件与该套接字联系起来
                    readWriteKey.attach(s);
                    // 将当前建立连接的客户端套接字及对应的通道存放在哈希表//clientChannelMap中
                    clientChannelMap.put(s,
                            new ClientChInstance(s.getChannel()));
                } else if (key.isReadable()) {// 如果是通道读准备好事件
                    System.out.println("Readable");
                    // 取得选择键对应的通道和套接字
                    SelectableChannel nextReady = key.channel();
                    Socket socket = (Socket) key.attachment();
                    // 处理该事件，处理方法已封装在类ClientChInstance中
                    readFromChannel(socket.getChannel(),
                            (ClientChInstance) clientChannelMap.get(socket));
                } else if (key.isWritable()) {// 如果是通道写准备好事件
                    System.out.println("writeable");
                    // 取得套接字后处理，方法同上
                    Socket socket = (Socket) key.attachment();
                    SocketChannel channel = socket.getChannel();
                    // this.writeToChannel(channel, "This is from server!");
                    writeToChannel(channel, data);
                }
            }
        }
    }

    // 对通道的写操作
    public void writeToChannel(SocketChannel channel, String message)
            throws IOException {
        ByteBuffer buf = ByteBuffer.wrap(message.getBytes());
        int nbytes = channel.write(buf);
    }

    // 对通道的读操作
    public void readFromChannel(SocketChannel channel,
            ClientChInstance clientInstance) throws IOException,
            InterruptedException {
        ByteBuffer byteBuffer = null;
        try {
            byteBuffer = ByteBuffer.allocate(BUFFERSIZE);
            int nbytes = channel.read(byteBuffer);
        } catch (Exception e) {
            clientChannelMap.remove(channel.socket());
            channel.close();
            e = null;
            return;
        }
        byteBuffer.flip();
        String result = decode(byteBuffer);
        // 当客户端发出”@exit”退出命令时，关闭其通道
        if (result.indexOf("@exit") >= 0 || result.indexOf("q") >= 0) {
            channel.close();
        }
        // else if(result.indexOf("@close") >= 0){//关闭服务
        // channel.close();
        // this.finalize();
        // }
        else {
            clientInstance.append(result.toString());
            // 读入一行完毕，执行相应操作
            if (result.indexOf("\n") >= 0) {
                System.out.println("client input" + result);
                clientInstance.execute();
            }
        }
    }

    // 该类封装了怎样对客户端的通道进行操作，具体实现可以通过重载execute()方法
    public class ClientChInstance {
        SocketChannel channel;
        StringBuffer buffer = new StringBuffer();

        public ClientChInstance(SocketChannel channel) {
            this.channel = channel;
        }

        public void execute() throws IOException {
            String message = "This is response after reading from channel!";
            writeToChannel(channel, message);
            buffer = new StringBuffer();
        }

        // 当一行没有结束时，将当前字窜置于缓冲尾
        public void append(String values) {
            buffer.append(values);
        }
    }

    // 主程序
    public static void main(String[] args) {
        NBServer nbServer = new NBServer(8090);
        try {
            nbServer.initialize();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
        try {
            nbServer.portListening("This is from server!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

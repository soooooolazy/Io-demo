package cn.wuc.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.sql.SQLSyntaxErrorException;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by Administrator on 2016/11/11.
 */
public class ClientHandle implements Runnable {
    private String host;
    private int port;
    private Selector selector;
    private SocketChannel socketChannel;
    private volatile  boolean stop;

    public ClientHandle(String host,int port){
        this.host=host==null?"127.0.0.1":host;
        this.port=port;
        try {
            //打开多路复用器
            selector=Selector.open();
            //打开Socket通道
            socketChannel=SocketChannel.open();
            //设置为非阻塞同步
            socketChannel.configureBlocking(false);
        }catch (IOException e){
            e.printStackTrace();
            System.exit(1);
        }
    }

    @Override
    public void run() {
        try {
            doConnect();
        }catch (IOException e){
            e.printStackTrace();
            System.exit(1);
        }
        while (!stop){
            try {
                selector.select();
                Set<SelectionKey> selectedKeys=selector.selectedKeys();
                Iterator<SelectionKey> it=selectedKeys.iterator();
                SelectionKey key=null;
                while (it.hasNext()){
                    key=it.next();
                    it.remove();
                    try {
                        handleInput(key);
                    }catch (Exception e){
                        if(key!=null){
                            key.cancel();
                            if(key.channel()!=null){
                                key.channel().close();
                            }
                        }
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
                System.exit(1);
            }
        }
        if(selector!=null){
            try {
                selector.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void handleInput(SelectionKey key) throws IOException{
        if(key.isValid()){
            //判断是否连接成功
            SocketChannel sc=(SocketChannel)key.channel();
            if(key.isConnectable()){
                System.out.println("isConnectable");
                if(sc.isConnectionPending()){
                    if(sc.finishConnect()){
                        doWrite(sc);
                }
                }else {
                    System.out.println("连接失败");
                    //连接失败，进程退出
                    System.exit(1);
                }
            }
            sc.register(selector,SelectionKey.OP_READ);
            if(key.isReadable()) {
                ByteBuffer readBuffer = ByteBuffer.allocate(1024);
                int readBytes = sc.read(readBuffer);
                // readBytes>0说明读到了数据，如果为-1，为关闭
                System.out.println("readBytes 返回值为" + readBytes);
                if (readBytes > 0) {
                    readBuffer.flip();
                    byte[] bytes = new byte[readBuffer.remaining()];
                    readBuffer.get(bytes);
                    String body = new String(bytes, "UTF-8");
                    System.out.println("Now is :" + body);
                } else if (readBytes < 0) {
                    //对端关闭
                    key.cancel();
                    sc.close();
                } else {
                    ;//readBytes 为0，不处理
                }
            }
        }
    }
    public void doConnect() throws IOException {
        //如果直接连接成功，则注册到Selector上，发送请求消息，读响应
        System.out.println("host:port"+host+":"+port);
        if(socketChannel.connect(new InetSocketAddress(host,port))){
            System.out.println("连接成功");
            socketChannel.register(selector,SelectionKey.OP_READ);
            doWrite(socketChannel);
        }else{
            System.out.println("去注册Selector");
            socketChannel.register(selector,SelectionKey.OP_CONNECT);
        }
    }

    public void doWrite(SocketChannel socketChannel) throws IOException {
        byte[] req="QUERY THE TIME".getBytes();
        ByteBuffer writeBuffer=ByteBuffer.allocate(req.length);
        writeBuffer.clear();
        writeBuffer.put(req);
        writeBuffer.flip();
        socketChannel.write(writeBuffer);
        if(!writeBuffer.hasRemaining()){
            System.out.println("Send order to the server");
        }
    }

}

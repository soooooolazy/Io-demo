package cn.wuc.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ThreadFactory;

/**
 * Created by Administrator on 2016/11/11.
 */
public class MultiplexerServer implements Runnable {
    private Selector selector;
    private ServerSocketChannel serverSocketChannel;
    private volatile boolean stop;

    public MultiplexerServer(int port) {
        try {
            selector=Selector.open();
            //打开服务器套接字通道
            serverSocketChannel=ServerSocketChannel.open();
            //设置为非阻塞
            serverSocketChannel.configureBlocking(false);
            //绑定端口
            serverSocketChannel.socket().bind(new InetSocketAddress(port),1024);
            //注册到Selector，等待连接
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            System.out.println("线程:"+Thread.currentThread().getName()+" The server is start in port:---"+port);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("初始化出错啦");
        }
    }
    public void stop(){
        this.stop=true;
    }

    @Override
    public void run() {
        System.out.println("线程:"+ Thread.currentThread().getName());
        while (!stop){
            try {
                //Selector每隔1S被唤醒一次，无参的方法表示有就绪状态的Channel集合，就返回SeletionKey集合
                selector.select();
                Set<SelectionKey> selectedKeys=selector.selectedKeys();
                System.out.println("有就绪状态的Channel Num: "+selectedKeys.size());
                Iterator<SelectionKey> iterator=selectedKeys.iterator();
                SelectionKey key=null;
                while (iterator.hasNext()){
                    key= iterator.next();
                    iterator.remove();
                    handleKey(key);
                    try {
                        if(key!=null){
                            key.cancel();
                            if(key.channel()!=null){
                                key.channel().close();
                            }
                        }
                    }catch (Throwable t){
                        t.printStackTrace();
                    }
                }
            } catch (Throwable t) {
                t.printStackTrace();
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
    private void handleKey(SelectionKey key) throws Exception{
        if(key.isValid()){
            if(key.isAcceptable()){
                //接受新连接
                ServerSocketChannel ssc=(ServerSocketChannel) key.channel();
                //客户端通道
                SocketChannel sc=ssc.accept();
                sc.configureBlocking(false);
                sc.register(selector,SelectionKey.OP_READ);
            }
            if(key.isReadable()){
                //读取数据
                SocketChannel sc=(SocketChannel)key.channel();
                ByteBuffer readBuffer=ByteBuffer.allocate(1024);
                int readByte=sc.read(readBuffer);
                if(readByte>0){
                    readBuffer.flip();
                    byte[] bytes=new byte[readBuffer.remaining()];
                    readBuffer.get(bytes);
                    String body=new String(bytes,"UTF-8");
                    System.out.println("The server receive order:"+ body);
                    String currentTime="QUERY THE TIME".equalsIgnoreCase(body)?new Date().toString():"Wrong order";
                    doWriter(sc,currentTime);
                }else if(readByte<0){
                    key.cancel();
                    sc.close();
                }else {
                    System.out.println("readBytes = 0");;
                }
            }
        }
    }
    private void doWriter(SocketChannel channel,String response) throws IOException {
        if(response!=null && response.trim().length()>0){
            byte[] bytes=response.getBytes();
            ByteBuffer writeBuffer=ByteBuffer.allocate(bytes.length);
            writeBuffer.clear();
            writeBuffer.put(bytes);
            writeBuffer.flip();
            System.out.println(writeBuffer.toString());
            channel.write(writeBuffer);
            System.out.println(new String(writeBuffer.array()));
            System.out.println("send the time");
        }
    }
}

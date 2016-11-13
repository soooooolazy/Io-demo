package cn.wuc.aio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.util.concurrent.CountDownLatch;


public class AsyncServerHandler implements Runnable{
    private int port;
    CountDownLatch latch;
    AsynchronousServerSocketChannel asynchronousServerSocketChannel;

    public AsyncServerHandler(int port){
        this.port=8080;
        try {
            //打开通道
            asynchronousServerSocketChannel=AsynchronousServerSocketChannel.open();
            //绑定端口
            asynchronousServerSocketChannel.bind(new InetSocketAddress(port));
            System.out.println("The server is starting in port:"+port);
            System.out.println(asynchronousServerSocketChannel);
        }catch (IOException e){
            e.printStackTrace();
        }
    }
    @Override
    public void run() {
        latch=new CountDownLatch(1);
        //接收客户端的连接
        doAccept();
        try {
            latch.await();
        }catch (InterruptedException e){
            e.printStackTrace();
        }
    }

    public void doAccept(){
        asynchronousServerSocketChannel.accept(this,new AcceptCompletionHandler());
    }
}

package cn.wuc.nio;

public class NIOServer {
    public static void main(String[] args) {
        int port=8080;
        MultiplexerServer server=new MultiplexerServer(port);
        //创建一个独立线程，负责轮询多路复用器Selector,可以处理多个客户端并发接入
        new Thread(server,"Nio-server001").start();
    }
}

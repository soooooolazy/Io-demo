package cn.wuc.nio;

public class NIOClient {
    public static void main(String[] args) {
        int port=8080;
        new Thread(new ClientHandle("127.0.0.1",port),"NIO-Clinet").start();
    }
}

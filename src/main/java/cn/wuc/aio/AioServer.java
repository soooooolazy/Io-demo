package cn.wuc.aio;


public class AioServer {
    public static void main(String[] args) {
        new Thread(new AsyncServerHandler(8080),"Server").start();
    }
}

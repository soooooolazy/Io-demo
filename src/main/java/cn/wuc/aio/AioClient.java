package cn.wuc.aio;


public class AioClient {
    public static void main(String[] args) {
        new Thread(new AsyncClientHandler("127.0.0.1",8080),"Client").start();
    }
}

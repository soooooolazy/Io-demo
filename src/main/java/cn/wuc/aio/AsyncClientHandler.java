package cn.wuc.aio;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.CountDownLatch;

public class AsyncClientHandler implements Runnable,CompletionHandler<Void,AsyncClientHandler> {
    private AsynchronousSocketChannel client;
    private String host;
    private int port;
    private CountDownLatch latch;

    public AsyncClientHandler(String host,int port){
        this.host="127.0.0.1";
        this.port=8080;
        try {
            client=AsynchronousSocketChannel.open();
        }catch (IOException e){
            e.printStackTrace();
        }
        System.out.println("The client is open");
    }

    @Override
    public void run() {
        latch=new CountDownLatch(1);
        client.connect(new InetSocketAddress(host,port),this,this);
        try {
            latch.await();
        }catch (InterruptedException e){
            e.printStackTrace();
        }
        try {
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void completed(Void result, AsyncClientHandler attachment) {
        System.out.println("client input");
        byte[] bytes="QUERY THE TIME".getBytes();
        ByteBuffer wriByteBuffer=ByteBuffer.allocate(bytes.length);
        wriByteBuffer.put(bytes);
//        wriByteBuffer.wrap(bytes,0,bytes.length);
        System.out.println(new String(wriByteBuffer.array()));
        System.out.println(wriByteBuffer);
        wriByteBuffer.flip();
        client.write(wriByteBuffer, wriByteBuffer, new CompletionHandler<Integer, ByteBuffer>() {
            @Override
            public void completed(Integer result, ByteBuffer attachment) {
                if(attachment.hasRemaining()){
                    System.out.println("has remaining");
                    client.write(attachment,attachment,this);
                }else {
                    ByteBuffer readBuffer=ByteBuffer.allocate(1024);
                    client.read(readBuffer, readBuffer, new CompletionHandler<Integer, ByteBuffer>() {
                        @Override
                        public void completed(Integer result, ByteBuffer attachment) {
                            attachment.flip();
                            byte[] bytes1=new byte[attachment.remaining()];
                            attachment.get(bytes1);
                            String body;
                            try {
                                body=new String (bytes1,"UTF-8");
                                System.out.println("Now is : "+body);
                                latch.countDown();
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void failed(Throwable exc, ByteBuffer attachment) {
                            try {
                                client.close();
                                latch.countDown();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }

            @Override
            public void failed(Throwable exc, ByteBuffer attachment) {
                try {
                    client.close();
                    latch.countDown();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void failed(Throwable exc, AsyncClientHandler attachment) {
        exc.printStackTrace();
        try {
            client.close();
            latch.countDown();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

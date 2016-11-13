package cn.wuc.aio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by Administrator on 2016/11/12.
 */
public class AcceptCompletionHandler implements CompletionHandler<AsynchronousSocketChannel,AsyncServerHandler> {


    @Override
    public void completed(AsynchronousSocketChannel result, AsyncServerHandler attachment) {
        attachment.asynchronousServerSocketChannel.accept(attachment,this);
        System.out.println("AcceptCompletionHandler read...");
        ByteBuffer buffer=ByteBuffer.allocate(1024);
        result.read(buffer,buffer,new ReadCompletionHandler(result));
        //        //在此调用accept()方法,每当一个客户端连接成功，再异步接收新的客户端连接
//        System.out.println(result);
//        try {
//            buffer.clear();
//            result.read(buffer).get(100, TimeUnit.SECONDS);
//            String getText=new String(buffer.array());
//            System.out.println(123);
//            System.out.println(getText);
//        }catch (InterruptedException | ExecutionException e){
//            e.printStackTrace();
//        }catch (TimeoutException  e){
//            e.printStackTrace();
//        }finally {
//            try {
//                result.close();
//                attachment.asynchronousServerSocketChannel.accept(null,this);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
    }

    @Override
    public void failed(Throwable exc, AsyncServerHandler attachment) {
        exc.printStackTrace();
        attachment.latch.countDown();
    }
}

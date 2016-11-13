package cn.wuc.aio;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.Date;

/**
 * Created by Administrator on 2016/11/12.
 */
public class ReadCompletionHandler implements CompletionHandler<Integer,ByteBuffer> {
    private AsynchronousSocketChannel channel;

    public ReadCompletionHandler(AsynchronousSocketChannel channel){
        if(this.channel==null) {
            this.channel = channel;
        }
    }

    @Override
    public void completed(Integer result, ByteBuffer attachment) {
        attachment.flip();
        byte[] body=new byte[attachment.remaining()];
        attachment.get(body);
        System.out.println(new String(body));
        try {
            String req=new String(body,"UTF-8");
            System.out.println("The server has receive order: "+req);
            String currentTime="QUERY THE TIME".equalsIgnoreCase(req)?new Date().toString():"Wrong order";
            doWrite(currentTime);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
public void doWrite(String currentTime){
    System.out.println(currentTime);
    if(currentTime!=null && currentTime.trim().length()>0){
        byte[] bytes=currentTime.getBytes();
        ByteBuffer writeByteBuffer=ByteBuffer.allocate(bytes.length);
        writeByteBuffer.put(bytes);
        writeByteBuffer.flip();
        channel.write(writeByteBuffer, writeByteBuffer, new CompletionHandler<Integer, ByteBuffer>() {
            @Override
            public void completed(Integer result, ByteBuffer attachment) {
                if(attachment.hasRemaining()){
                    channel.write(attachment,attachment,this);
                }
            }

            @Override
            public void failed(Throwable exc, ByteBuffer attachment) {
                try {
                    channel.close();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        });
    }
}

    @Override
    public void failed(Throwable exc, ByteBuffer attachment) {
        try {
            this.channel.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}


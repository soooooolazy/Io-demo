package cn.wuc.bio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Date;

/**
 * Created by Administrator on 2016/11/11.
 */
public class ServerHandler implements Runnable {
    private Socket socket;

    public ServerHandler(Socket socket){
        this.socket=socket;
    }

    public void run() {
        BufferedReader in=null;
        PrintWriter out=null;
        try {
            in=new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out=new PrintWriter(socket.getOutputStream(),true);
            String currentTime=null;
            String body=null;
            while (true){
                body=in.readLine();
                if(body==null)
                    break;
                System.out.println("The server has reveived the order");
                currentTime="QUERY THE TIME".equalsIgnoreCase(body)?new Date().toString():"Wrong order";
                out.println(currentTime);
            }
        }catch (Exception e){
            if (in!=null){
                try {
                    in.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            if(out!=null){
                out.close();
            }
            if(socket!=null){
                try {
                    socket.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }
}

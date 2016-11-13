package cn.wuc.bio;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Administrator on 2016/11/11.
 */
public class Server {
    public static void main(String[] args) {
        int port=8080;
        ServerSocket serverSocket=null;
        try {
            serverSocket=new ServerSocket(port);
            System.out.println("The server is on!");
            Socket socket=null;
            ServerHandlerExecutePool serverHandlerExecutePool=new ServerHandlerExecutePool(50,1000);
            while (true) {
                socket=serverSocket.accept();
                serverHandlerExecutePool.execute(new ServerHandler(socket));
            }
        }catch (Exception e){

        }finally {
            if(serverSocket!=null){
                try {
                    serverSocket.close();
                    serverSocket=null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

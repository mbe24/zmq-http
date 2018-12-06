package org.beyene.zmq;

import org.zeromq.ZFrame;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Socket;
import org.zeromq.ZContext;
import org.zeromq.ZMsg;

import java.util.StringJoiner;

public class Application {

    public static void main(String[] args) {

        String httpResponse = new StringJoiner("\r\n")
                .add("HTTP/1.1 200 OK")
                .add("Content-Type: text/html")
                .add("")
                .add("<html><body><h1>Hello, World!</h1></body></html>")
                .toString();

        try (ZContext context = new ZContext()) {
            Socket socket = context.createSocket(ZMQ.STREAM);
            socket.bind("tcp://*:8080");

            System.out.println("Server is listening on http://localhost:8080");
            while (!Thread.currentThread().isInterrupted()) {
                ZMsg request = ZMsg.recvMsg(socket);

                if (request.isEmpty() || request.size() != 2)
                    continue;

                ZFrame identityFrame = request.poll();
                ZFrame messageFrame = request.poll();

                String httpRequest = new String(messageFrame.getData(), ZMQ.CHARSET);
                if (httpRequest.isEmpty())
                    continue;

                System.out.println("ID:" + identityFrame.strhex());
                System.out.println("REQUEST:" + System.lineSeparator() + httpRequest);

                ZMsg response = new ZMsg();
                response.add(identityFrame.getData()); // specify receiver by sending identity
                response.add(httpResponse.getBytes(ZMQ.CHARSET)); // send http response

                response.add(identityFrame.getData()); // specify receiver by sending identity
                response.add(new byte[0]); // close connection by sending empty message

                response.send(socket);
            }

        }

    }
}

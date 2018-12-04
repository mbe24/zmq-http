package org.beyene.zmq;

import org.zeromq.ZFrame;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Socket;
import org.zeromq.ZContext;
import org.zeromq.ZMsg;

import java.util.StringJoiner;

public class Application {

    public static void main(String[] args) {

        String message = new StringJoiner("\r\n")
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
                ZMsg msg = ZMsg.recvMsg(socket);

                if (msg.isEmpty() || msg.size() != 2)
                    continue;

                ZFrame identityFrame = msg.poll();
                ZFrame messageFrame = msg.poll();

                String request = new String(messageFrame.getData(), ZMQ.CHARSET);
                if (request.isEmpty())
                    continue;

                System.out.println("ID:" + identityFrame.strhex());
                System.out.println("REQUEST:" + System.lineSeparator() + request);

                // specify receiver by sending identity
                socket.send(identityFrame.getData(), ZMQ.SNDMORE);
                // send http response
                socket.send(message.getBytes(ZMQ.CHARSET), ZMQ.SNDMORE);

                // specify receiver by sending identity
                socket.send(identityFrame.getData(), ZMQ.SNDMORE);
                // close connection by sending empty message
                socket.send(new byte[0]);
            }

        }

    }
}

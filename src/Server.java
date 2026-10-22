
import com.google.gson.Gson;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Server {

    private static WebSocketServer server;
    private static final Scanner scanner = new Scanner(System.in);
    private static final Gson gson = new Gson();
    private static Map<WebSocket, String> clients;
    private static Queue<Chat> chatLog;

    // 서버 초기화
    public static void init() {
        clients = new ConcurrentHashMap<>();
        chatLog = new ConcurrentLinkedQueue<>();
        server = new WebSocketServer(
                new InetSocketAddress("127.0.0.1", 8080)) {

            @Override
            public void onStart() {
                System.out.println("서버 시작! 포트: 8080");
            }

            @Override
            public void onOpen(WebSocket conn, ClientHandshake handshake) {
                System.out.println("클라이언트 접속: "
                        + conn.getRemoteSocketAddress());

                MessageForm messageForm = new MessageForm();
                messageForm.setType("SYSTEM");
                messageForm.setContent("서버에 연결되었습니다.");

                conn.send(gson.toJson(messageForm));
            }

            @Override
            public void onMessage(WebSocket conn, String message) {
                receiveMessage(message, conn);
            }

            @Override
            public void onClose(WebSocket conn, int code,
                                String reason, boolean remote) {
                System.out.println("클라이언트 연결 종료: "
                        + conn.getRemoteSocketAddress());
                clients.remove(conn);
            }

            @Override
            public void onError(WebSocket conn, Exception ex) {
                System.err.println("WebSocket 오류: " + ex.getMessage());
            }
        };

        server.start();
        startCommand();
    }

    // 클라이언트 메시지 수신 및 처리
    private static void receiveMessage(String message, WebSocket conn) {

        MessageForm messageForm = gson.fromJson(message, MessageForm.class);

        if("USER_JOIN".equals(messageForm.getType())){
            System.out.println(messageForm.getContent() + " 님이 입장했습니다");
            clients.put(conn, messageForm.getContent());
        }else if("CHAT".equals(messageForm.getType())){
            System.out.println("[수신]" + clients.get(conn) + " : " + messageForm.getContent());
            Chat chat = new Chat();
            chat.setName(clients.get(conn));
            chat.setMessage(messageForm.getContent());

            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            chat.setTime(now.format(formatter));

            chatLog.add(chat);
            MessageForm broadCastMessageForm = new MessageForm();
            broadCastMessageForm.setType("CHAT");
            broadCastMessageForm.setContent(chat.getName() + " : " + messageForm.getContent());
            broadcastMessage(conn, gson.toJson(broadCastMessageForm));
        }
    }

    // 모든 클라이언트에게 메시지 전송
    public static void broadcastMessage(WebSocket sender, String message) {

        for (WebSocket client : clients.keySet()) {

            if (client != sender && client.isOpen()) {
                client.send(message);
            }
        }
    }

    // 서버 콘솔 명령어 처리
    private static void startCommand() {

        while (true) {
            String command = scanner.nextLine();

            if (command.equals("/exit")) {
                close();
                break;
            }

            if(command.equals("/clients")){
                for(Map.Entry<WebSocket, String> entry : clients.entrySet()){
                    System.out.println("Websocket : " + entry.getKey() + " Client Name : " + entry.getValue());
                }
            }

            if(command.equals("/chatLog")){
                for(Chat chat : chatLog){
                    System.out.println("[" + chat.getTime() + "] " + chat.getName() + " : " + chat.getMessage());
                }
            }
        }
    }

    // 서버 종료
    public static void close() {

        if (server == null) {
            return;
        }

        try {
            server.stop(1000);
            System.out.println("서버 종료 완료");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("서버 종료 중 인터럽트 발생");
        }
    }
}


import com.google.gson.Gson;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.Scanner;

public class Client {

    private static WebSocketClient client;
    private static final Scanner scanner = new Scanner(System.in);
    private static String clientName = null;
    private static final Gson gson = new Gson();
    // 클라이언트 초기화 및 서버 연결
    public static void init() throws Exception {

        while(clientName == null || clientName.isBlank()){
            System.out.print("이름 입력 : ");
            clientName = scanner.nextLine();
        }

        client = new WebSocketClient(
                new URI("ws://127.0.0.1:8080")) {

            @Override
            public void onOpen(ServerHandshake handshake) {
                System.out.println("서버 연결 성공!");
                sendMessage(clientName, "R");
            }

            @Override
            public void onMessage(String message) {
                MessageForm messageForm = gson.fromJson(message, MessageForm.class);
                if("CHAT".equals(messageForm.getType())){
                    System.out.println("메시지: " + messageForm.getContent());
                }else if ("SYSTEM".equals(messageForm.getType())) {
                    System.out.println("[SYSTEM] " + messageForm.getContent());
                }
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                System.out.println("서버 연결 종료");
            }

            @Override
            public void onError(Exception ex) {
                ex.printStackTrace();
            }
        };

        boolean connected = client.connectBlocking();

        if (!connected) {
            System.out.println("서버 연결 실패");
            return;
        }

        startChat();
    }

    // 콘솔 입력 및 채팅
    private static void startChat() {

        while (client.isOpen()) {

            String message = scanner.nextLine();

            if (message.equals("/exit")) {
                break;
            }

            sendMessage(message, "M");
        }

        close();
    }

    // 메시지 전송
    public static void sendMessage(String content, String type) {
        if (client != null && client.isOpen()) {
            MessageForm messageForm = new MessageForm();
            messageForm.setContent(content);
            if("M".equals(type)){
                messageForm.setType("CHAT");
            }else if("R".equals(type)){
                messageForm.setType("USER_JOIN");
            }
            client.send(gson.toJson(messageForm));
        }
    }

    // 연결 종료
    public static void close() {

        if (client != null) {
            client.close();
        }
    }
}

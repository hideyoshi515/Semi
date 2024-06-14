package co.kr.necohost.semi.websocket;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class OrderWebSocketHandler extends TextWebSocketHandler {
	private final List<WebSocketSession> sessions = new ArrayList<>();

	@Override
	public void afterConnectionEstablished(WebSocketSession session) {
		sessions.add(session);
		System.out.println("새로운 세션이 연결됨: " + session.getId());
	}

	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
		for (WebSocketSession s : sessions) {
			s.sendMessage(new TextMessage(message.getPayload()));
		}
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
		sessions.remove(session);
		System.out.println("세션이 닫힘: " + session.getId());
	}

	public void sendMessageToAll(String message) throws IOException {
		for (WebSocketSession session : sessions) {
			session.sendMessage(new TextMessage(message));
		}
	}

	public void sendMessageToAllSessions(TextMessage message) throws IOException {
		System.out.println("웹소켓 1");

		for (WebSocketSession session : sessions) {
			System.out.println("세션 ID: " + session.getId() + ", 세션 상태: " + session.isOpen());

			if (session.isOpen()) {
				try {
					session.sendMessage(message);
					System.out.println("웹 소켓 2");
				} catch (IOException e) {
					System.out.println("메시지 전송 중 예외 발생: " + e.getMessage());
					e.printStackTrace();
				}
			} else {
				System.out.println("세션이 열려 있지 않음: " + session.getId());
			}
		}
	}

	public List<WebSocketSession> getSessions() {
		return sessions;
	}
}

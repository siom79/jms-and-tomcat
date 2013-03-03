package com.wordpress.martinsdeveloperworld;

import java.io.IOException;

import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueReceiver;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.InitialContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;

@WebServlet(name = "ReceiveMessageServlet", urlPatterns = "/receiveMessage")
public class ReceiveMessageServlet extends HttpServlet {

	private static final Logger logger = LoggerFactory.getLogger(ReceiveMessageServlet.class);

	@Override
	protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
			throws ServletException, IOException {
		logger.info("doGet() called");
		Optional<String> text = receiveMessages();
		writeRepsonse(httpServletResponse, text);
	}

	private void writeRepsonse(HttpServletResponse httpServletResponse, Optional<String> text) throws IOException {
		httpServletResponse.setContentType("text/plain");
		if (text.isPresent()) {
			httpServletResponse.getWriter().write(String.format("Received message with text '%s'.", text.get()));
		} else {
			httpServletResponse.getWriter().write("Received no message.");
		}
	}

	private Optional<String> receiveMessages() {
		Optional<String> text = Optional.absent();
		try {
			InitialContext initCtx = new InitialContext();
			QueueConnectionFactory connectionFactory = (QueueConnectionFactory) initCtx
					.lookup("java:comp/env/jms/ConnectionFactory");
			QueueConnection queueConnection = connectionFactory.createQueueConnection();
			QueueSession queueSession = queueConnection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
			Queue queue = (Queue) initCtx.lookup("java:comp/env/jms/queue/MyQueue");
			QueueReceiver receiver = queueSession.createReceiver(queue);

			queueConnection.start();
			try {
				Message m = receiver.receive(1000);
				if (m != null && m instanceof TextMessage) {
					TextMessage tm = (TextMessage) m;
					text = Optional.of(tm.getText());
					logger.debug(String.format("Received TextMessage with text '%s'.", text));
				} else {
					logger.debug(String.format("No TextMessage received: '%s'", m));
				}
			} finally {
				queueSession.close();
				queueConnection.close();
			}
		} catch (Exception e) {
			logger.error("Receiving messages failed: " + e.getMessage(), e);
		}
		return text;
	}

}

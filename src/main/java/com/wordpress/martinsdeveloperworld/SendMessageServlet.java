package com.wordpress.martinsdeveloperworld;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

import javax.jms.*;
import javax.naming.InitialContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;

@WebServlet(name = "SendMessageServlet", urlPatterns = "/sendMessage")
public class SendMessageServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(SendMessageServlet.class);

    @Override
    protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
    	logger.info("doGet() called");
    	String parameter = getTextParameter(httpServletRequest);
        sendMessage(parameter);
        writeResponse(httpServletResponse, parameter);
    }

	private void writeResponse(HttpServletResponse httpServletResponse,
			String parameter) throws IOException {
		httpServletResponse.setContentType("text/plain");
        httpServletResponse.getWriter().write(String.format("Sent message with content '%s'.", parameter));
	}

	private String getTextParameter(HttpServletRequest httpServletRequest) {
		String parameter = httpServletRequest.getParameter("text");
    	if(Strings.isNullOrEmpty(parameter)) {
    		parameter = (new Date()).toString();
    	}
		return parameter;
	}

    private void sendMessage(String text) {
        try {
            InitialContext initCtx = new InitialContext();
            ConnectionFactory connectionFactory = (ConnectionFactory) initCtx.lookup("java:comp/env/jms/ConnectionFactory");
            Connection connection = connectionFactory.createConnection();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            MessageProducer producer = session.createProducer((Destination) initCtx.lookup("java:comp/env/jms/queue/MyQueue"));

            TextMessage testMessage = session.createTextMessage();
            testMessage.setText(text);
            testMessage.setStringProperty("aKey", "someRandomTestValue");
            producer.send(testMessage);
            logger.debug("Successfully sent message.");
        } catch (Exception e) {
            logger.error("Sending JMS message failed: "+e.getMessage(), e);
        }
    }
}

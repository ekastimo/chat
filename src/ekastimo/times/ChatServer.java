package ekastimo.times;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.ServletException;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebServlet(asyncSupported = true)
public class ChatServer extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String IS_ASYNC = "IS_ASYNC";
	private static final String TOKEN = "#########";
	private Logger LOGGER = LoggerFactory.getLogger(ChatServer.class);
	private HashMap<String, AsyncContext> users = new HashMap<String, AsyncContext>();

	public ChatServer() {
	}
	
	@Override
	public void destroy() {
		synchronized (users) {
			LOGGER.info("Users To destroy:{}", users.size());
			for (Entry<String, AsyncContext> ent : users.entrySet()) {
				ent.getValue().complete();
			}
			users.clear();
			users=null;
		}
		super.destroy();
	}
	public void doGet(HttpServletRequest request, HttpServletResponse response)	throws ServletException, IOException {
		LOGGER.info("Calling do get");
		String username = request.getParameter("username");
		if(username==null){
			LOGGER.info("No user name");
			return;
		}
		synchronized (users) {
			if(!users.containsKey(username)){
				LOGGER.info("Adding user:{}",username);
				request.getSession().setAttribute("username", username);
				request.getSession().setAttribute(IS_ASYNC, TOKEN);
				AsyncContext async = request.startAsync(request, response);
				async.addListener(new MyAsyncListener());
				users.put(username, async);
			}else{
				LOGGER.info("User {} already has context: Going to complete it",username);
				users.remove(username).complete();
			}			
		}
	}
	
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String username = request.getParameter("username");
		String message = request.getParameter("message");
		LOGGER.info("User:{}, Message: {}", username, message);
		synchronized (users) {
			LOGGER.info("Users:{}", users.size());
			for (Entry<String, AsyncContext> ent : users.entrySet()) {
				ServletResponse async_response = ent.getValue().getResponse();
				async_response.setContentType("text/plain");
				async_response.setCharacterEncoding("UTF-8");
				PrintWriter writer = async_response.getWriter();
				writer.println(serialize(username+":"+message));
				LOGGER.info("Writing:" + message);
				try {
					writer.flush();
				} catch (Exception e) {
					LOGGER.warn("User {} disconected ",ent.getKey());
				}finally{
					writer.close();
				}
				LOGGER.info("Closing connection");
				ent.getValue().complete();
			}
			users.clear();
		}
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
		}
	}

	private String serialize(Serializable serializable) {
		return serializable.toString();
	}
	class MyAsyncListener implements AsyncListener{
		public MyAsyncListener() {
			
		}
		@Override
		public void onTimeout(AsyncEvent arg0) throws IOException {
			synchronized (users) {
				LOGGER.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>onTimeout");
				HttpSession ss = ((HttpServletRequest)arg0.getAsyncContext().getRequest()).getSession();
				String username=(String) ss.getAttribute("username");
				arg0.getAsyncContext().getResponse().getWriter().close();
				arg0.getAsyncContext().complete();
				users.remove(username);
				LOGGER.info("Have removed {}",username);
			}
		}
		
		@Override
		public void onStartAsync(AsyncEvent arg0) throws IOException {
			LOGGER.info("onStartAsync");
			
		}
		
		@Override
		public void onError(AsyncEvent arg0) throws IOException {
			synchronized (users) {
				LOGGER.error("#################onError",arg0.getThrowable().getMessage());
				HttpSession ss = ((HttpServletRequest)arg0.getAsyncContext().getRequest()).getSession();
				String username=(String) ss.getAttribute("username");
				arg0.getAsyncContext().getResponse().getWriter().close();
				arg0.getAsyncContext().complete();
				users.remove(username);
				LOGGER.info("Have removed {}",username);
			}
		}
		
		@Override
		public void onComplete(AsyncEvent arg0) throws IOException {
			LOGGER.info("onComplete");
		}
	}
	
}
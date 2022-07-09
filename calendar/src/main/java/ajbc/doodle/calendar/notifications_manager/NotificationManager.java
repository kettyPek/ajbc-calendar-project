package ajbc.doodle.calendar.notifications_manager;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse.BodyHandlers;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

import javax.annotation.PostConstruct;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.auth0.jwt.JWT;
import com.fasterxml.jackson.core.JsonProcessingException;

import ajbc.doodle.calendar.Application;
import ajbc.doodle.calendar.PushProp;
import ajbc.doodle.calendar.daos.DaoException;
import ajbc.doodle.calendar.daos.EventDao;
import ajbc.doodle.calendar.daos.NotificationDao;
import ajbc.doodle.calendar.daos.UserDao;
import ajbc.doodle.calendar.entities.Event;
import ajbc.doodle.calendar.entities.Notification;
import ajbc.doodle.calendar.entities.User;
import ajbc.doodle.calendar.entities.webpush.PushMessage;
import ajbc.doodle.calendar.enums.Units;
import lombok.Setter;
@Setter
@Component
public class NotificationManager {
	
	@Autowired
	private UserDao userDao;
	
	@Autowired
	private NotificationDao notificationDao;
	
	@Autowired
	private EventDao eventDao;
	
	private Queue<Notification> notificationsQueue  = new PriorityQueue<Notification>(new Comparator<Notification>() {
		@Override
		public int compare(Notification n1, Notification n2) {
			try {
				return calculateNotificationTime(n1).isBefore(calculateNotificationTime(n2))? -1: 1;
			} catch (DaoException e) {
				return 1;
			}	}
	});
	
	private PushProp pushProps;
	
		
	public void inntializeNotificationsQueue(List<Notification> notificationsList) {
		for(int i=0; i<notificationsList.size(); i++)
			notificationsQueue.add(notificationsList.get(i));
	}
	
	public void run() throws DaoException {
		User user;
		Object message;
		System.out.println("in run()");
		Notification not ;
		while(!notificationsQueue.isEmpty()) {
			not = notificationsQueue.poll();
			user = userDao.getUserById(not.getUserId());
			System.out.println(calculateNotificationTime(not));
			if(user.isLoggedIn()) {
				message = new PushMessage("message: ", not.getTitle());
				byte[] result;
				try {
					result = pushProps.getCryptoService().encrypt(pushProps.getObjectMapper().writeValueAsString(message), user.getP256dh(),
							user.getAuth(), 0);
					sendPushMessage(user.getEndPoint(), result);
					System.out.println("massge: " + not.getTitle() + " sent to user " + user.getEmail());
					Thread.sleep(5000);
				} catch (InvalidKeyException | NoSuchAlgorithmException | InvalidKeySpecException
						| InvalidAlgorithmParameterException | NoSuchPaddingException | IllegalBlockSizeException
						| BadPaddingException | JsonProcessingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		}
	}
	
	/**
	 * @return true if the subscription is no longer valid and can be removed, false
	 *         if everything is okay
	 */
	private boolean sendPushMessage(String endPoint, byte[] body) {
		String origin = null;
		try {
			URL url = new URL(endPoint);
			origin = url.getProtocol() + "://" + url.getHost();
		} catch (MalformedURLException e) {
			Application.logger.error("create origin", e);
			return true;
		}

		Date today = new Date();
		Date expires = new Date(today.getTime() + 12 * 60 * 60 * 1000);

		String token = JWT.create().withAudience(origin).withExpiresAt(expires)
				.withSubject("mailto:example@example.com").sign(pushProps.getJwtAlgorithm());

		URI endpointURI = URI.create(endPoint);

		Builder httpRequestBuilder = HttpRequest.newBuilder();
		if (body != null) {
			httpRequestBuilder.POST(BodyPublishers.ofByteArray(body)).header("Content-Type", "application/octet-stream")
					.header("Content-Encoding", "aes128gcm");
		} else {
			httpRequestBuilder.POST(BodyPublishers.ofString("sososo"));
			// httpRequestBuilder.header("Content-Length", "0");
		}

		HttpRequest request = httpRequestBuilder.uri(endpointURI).header("TTL", "180")
				.header("Authorization", "vapid t=" + token + ", k=" + pushProps.getServerKeys().getPublicKeyBase64()).build();
		try {
			HttpResponse<Void> response = pushProps.getHttpClient().send(request, BodyHandlers.discarding());

			switch (response.statusCode()) {
			case 201:
				Application.logger.info("Push message successfully sent: {}", endPoint);
				break;
			case 404:
			case 410:
				Application.logger.warn("Subscription not found or gone: {}", endPoint);
				// remove subscription from our collection of subscriptions
				return true;
			case 429:
				Application.logger.error("Too many requests: {}", request);
				break;
			case 400:
				Application.logger.error("Invalid request: {}", request);
				break;
			case 413:
				Application.logger.error("Payload size too large: {}", request);
				break;
			default:
				Application.logger.error("Unhandled status code: {} / {}", response.statusCode(), request);
			}
		} catch (IOException | InterruptedException e) {
			Application.logger.error("send push message", e);
		}

		return false;
	}
	
	private LocalDateTime calculateNotificationTime(Notification notification) throws DaoException {
		Event event = eventDao.getEventById(notification.getEventId());
		LocalDateTime date;
		if(notification.getUnits() == Units.HOURS)
			date = event.getStartDateTime().minusHours(notification.getQuantity());
		else
			date = event.getStartDateTime().minusMinutes(notification.getQuantity());
		return date;
	}
	
	
	
	

}

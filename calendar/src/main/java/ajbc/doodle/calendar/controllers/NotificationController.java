package ajbc.doodle.calendar.controllers;

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
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.auth0.jwt.JWT;
import com.fasterxml.jackson.core.JsonProcessingException;

import ajbc.doodle.calendar.Application;
import ajbc.doodle.calendar.PushProp;
import ajbc.doodle.calendar.daos.DaoException;
import ajbc.doodle.calendar.daos.NotificationDao;
import ajbc.doodle.calendar.daos.UserDao;
import ajbc.doodle.calendar.entities.Notification;
import ajbc.doodle.calendar.entities.User;
import ajbc.doodle.calendar.entities.webpush.PushMessage;
import ajbc.doodle.calendar.notifications_manager.NotificationManager;
import ajbc.doodle.calendar.services.NotificationServcie;
import lombok.NoArgsConstructor;

@RequestMapping("/notifications")
@RestController
public class NotificationController {
	
	@Autowired
	private NotificationDao notificationDao;
	
	@Autowired
	private NotificationServcie notificationService;
	
	@Autowired
	private UserDao userDao;
	
	@Autowired
	private PushProp pushProps;
	
	@Autowired
	private NotificationManager notMan;
	

	
	@RequestMapping(method = RequestMethod.POST)
	public ResponseEntity<?> createNotification(@RequestBody Notification notification, @RequestParam int userId ,@RequestParam Integer eventId) {
		try {
			// TODO check if user logged in
			notificationService.addNotificationToEventOfUser(userId, eventId, notification);
			// TODO send the created notification
			return ResponseEntity.status(HttpStatus.CREATED).build();
		} catch (DaoException e) {
			return ResponseEntity.status(HttpStatus.valueOf(500)).body(e.getMessage());
		}
	}
	
	@RequestMapping(method = RequestMethod.GET)
	public ResponseEntity<?> getAllNotifications() {
		try {
			List<Notification> notifications = notificationService.getAllNotifications();
			return ResponseEntity.status(HttpStatus.CREATED).body(notifications);
		} catch (DaoException e) {
			return ResponseEntity.status(HttpStatus.valueOf(500)).body(e.getMessage());
		}
	}
	
	@GetMapping(path = "/publicSigningKey", produces = "application/octet-stream")
	public byte[] publicSigningKey() {
		return pushProps.getServerKeys().getPublicKeyUncompressed();
	}

	@GetMapping(path = "/publicSigningKeyBase64")
	public String publicSigningKeyBase64() {
		return pushProps.getServerKeys().getPublicKeyBase64();
	}
	
	@PostConstruct
	public void update() throws DaoException {
		notMan.setPushProps(pushProps);
		List<Notification> notifications = notificationDao.getAllNotifications();
		notMan.inntializeNotificationsQueue(notifications);
		}
	@Scheduled(initialDelay = 3_000 ,fixedDelay = 10_000)
	public void run() throws DaoException, InterruptedException {
		notMan.run();
	}
	
//	@Scheduled(fixedDelay = 3_000)
//	public void testNotification() throws DaoException {
//		List<User> users = userDao.getAllUsers();
//		List<Notification> notif = notificationDao.getAllNotifications();
//		Object message = new PushMessage("message: ", notif.get(0).getTitle());
//
//		users.forEach(u -> {
//			if (u.isLoggedIn() == true) {
//				byte[] result;
//				try {
//					result = pushProps.getCryptoService().encrypt(pushProps.getObjectMapper().writeValueAsString(message), u.getP256dh(),
//							u.getAuth(), 0);
//					sendPushMessage(u.getEndPoint(), result);
//				} catch (InvalidKeyException | NoSuchAlgorithmException | InvalidKeySpecException
//						| InvalidAlgorithmParameterException | NoSuchPaddingException | IllegalBlockSizeException
//						| BadPaddingException | JsonProcessingException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//
//			}
//		});
//	}
	
//	@Scheduled(fixedDelay = 3_000)
//	public void testNotification() throws DaoException {
//		User user;
//		Object message;
//		List<Notification> notifications = notificationDao.getAllNotifications();
//		for(Notification notif : notifications) {
//			user = userDao.getUserById(notif.getUserId());
//			System.out.println("inin");
//			System.out.println(user.isLoggedIn());
//			if(user.isLoggedIn()) {
//				message = new PushMessage("message: ", "work");
//				byte[] result;
//				try {
//					result = pushProps.getCryptoService().encrypt(pushProps.getObjectMapper().writeValueAsString(message), user.getP256dh(),
//							user.getAuth(), 0);
//					sendPushMessage(user.getEndPoint(), result);
//				} catch (InvalidKeyException | NoSuchAlgorithmException | InvalidKeySpecException
//						| InvalidAlgorithmParameterException | NoSuchPaddingException | IllegalBlockSizeException
//						| BadPaddingException | JsonProcessingException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
//		}
//	}
//	
//	/**
//	 * @return true if the subscription is no longer valid and can be removed, false
//	 *         if everything is okay
//	 */
//	private boolean sendPushMessage(String endPoint, byte[] body) {
//		String origin = null;
//		try {
//			URL url = new URL(endPoint);
//			origin = url.getProtocol() + "://" + url.getHost();
//		} catch (MalformedURLException e) {
//			Application.logger.error("create origin", e);
//			return true;
//		}
//
//		Date today = new Date();
//		Date expires = new Date(today.getTime() + 12 * 60 * 60 * 1000);
//
//		String token = JWT.create().withAudience(origin).withExpiresAt(expires)
//				.withSubject("mailto:example@example.com").sign(pushProps.getJwtAlgorithm());
//
//		URI endpointURI = URI.create(endPoint);
//
//		Builder httpRequestBuilder = HttpRequest.newBuilder();
//		if (body != null) {
//			httpRequestBuilder.POST(BodyPublishers.ofByteArray(body)).header("Content-Type", "application/octet-stream")
//					.header("Content-Encoding", "aes128gcm");
//		} else {
//			httpRequestBuilder.POST(BodyPublishers.ofString("sososo"));
//			// httpRequestBuilder.header("Content-Length", "0");
//		}
//
//		HttpRequest request = httpRequestBuilder.uri(endpointURI).header("TTL", "180")
//				.header("Authorization", "vapid t=" + token + ", k=" + pushProps.getServerKeys().getPublicKeyBase64()).build();
//		try {
//			HttpResponse<Void> response = pushProps.getHttpClient().send(request, BodyHandlers.discarding());
//
//			switch (response.statusCode()) {
//			case 201:
//				Application.logger.info("Push message successfully sent: {}", endPoint);
//				break;
//			case 404:
//			case 410:
//				Application.logger.warn("Subscription not found or gone: {}", endPoint);
//				// remove subscription from our collection of subscriptions
//				return true;
//			case 429:
//				Application.logger.error("Too many requests: {}", request);
//				break;
//			case 400:
//				Application.logger.error("Invalid request: {}", request);
//				break;
//			case 413:
//				Application.logger.error("Payload size too large: {}", request);
//				break;
//			default:
//				Application.logger.error("Unhandled status code: {} / {}", response.statusCode(), request);
//			}
//		} catch (IOException | InterruptedException e) {
//			Application.logger.error("send push message", e);
//		}
//
//		return false;
//	}


}

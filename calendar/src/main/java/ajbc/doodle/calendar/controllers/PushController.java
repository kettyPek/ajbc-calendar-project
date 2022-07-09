package ajbc.doodle.calendar.controllers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ajbc.doodle.calendar.Application;
import ajbc.doodle.calendar.PushProp;
import ajbc.doodle.calendar.ServerKeys;
import ajbc.doodle.calendar.daos.DaoException;
import ajbc.doodle.calendar.daos.NotificationDao;
import ajbc.doodle.calendar.daos.UserDao;
import ajbc.doodle.calendar.entities.Event;
import ajbc.doodle.calendar.entities.Notification;
import ajbc.doodle.calendar.entities.User;
import ajbc.doodle.calendar.entities.webpush.PushMessage;
import ajbc.doodle.calendar.entities.webpush.Subscription;
import ajbc.doodle.calendar.entities.webpush.SubscriptionEndpoint;
import ajbc.doodle.calendar.services.CryptoService;
import ajbc.doodle.calendar.services.UserService;

@RestController
public class PushController {

//	private final ServerKeys serverKeys;
//
//	private final CryptoService cryptoService;
//
//	private final HttpClient httpClient;
//
//	private final Algorithm jwtAlgorithm;
//
//	private final ObjectMapper objectMapper;

//	@Autowired
//	private NotificationDao notificationDao;
//
//	@Autowired
//	private UserDao userDao;
//	
//	@Autowired
//	private PushProp pushProps;

//	public PushController(ServerKeys serverKeys, CryptoService cryptoService, ObjectMapper objectMapper) {
//		this.serverKeys = serverKeys;
//		this.cryptoService = cryptoService;
//		this.httpClient = HttpClient.newHttpClient();
//		this.objectMapper = objectMapper;
//
//		this.jwtAlgorithm = Algorithm.ECDSA256(this.serverKeys.getPublicKey(), this.serverKeys.getPrivateKey());
//	}

//	@GetMapping(path = "/publicSigningKey", produces = "application/octet-stream")
//	public byte[] publicSigningKey() {
//		return pushProps.getServerKeys().getPublicKeyUncompressed();
//	}
//
//	@GetMapping(path = "/publicSigningKeyBase64")
//	public String publicSigningKeyBase64() {
//		return pushProps.getServerKeys().getPublicKeyBase64();
//	}

	

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
const subscribeButton = document.getElementById('subscribeButton');
const unsubscribeButton = document.getElementById('unsubscribeButton');

const notificationOutput = document.getElementById('notification');

const email = document.getElementById('email');
const alert = document.getElementById('alert');

if ("serviceWorker" in navigator) {
	try {
		checkSubscription();
		init();
	} catch (e) {
		console.error('error init(): ' + e);
	}

	subscribeButton.addEventListener('click', () => {
		subscribe().catch(e => {
			if (Notification.permission === 'denied') {
				console.warn('Permission for notifications was denied');
			} else {
				console.error('error subscribe(): ' + e);
			}
		});
	});

	unsubscribeButton.addEventListener('click', () => {
		unsubscribe().catch(e => console.error('error unsubscribe(): ' + e));
	});
}


async function checkSubscription() {
	const registration = await navigator.serviceWorker.ready;
	const subscription = await registration.pushManager.getSubscription();
	if (subscription) {

		const response = await fetch("/isSubscribed", {
			method: 'POST',
			body: JSON.stringify({ endpoint: subscription.endpoint }),
			headers: {
				"content-type": "application/json"
			}
		});
		const subscribed = await response.json();

		if (subscribed) {
			subscribeButton.disabled = true;
			unsubscribeButton.disabled = false;
			email.disabled = true;
			alert.style.display = "none";
		}

		return subscribed;
	}

	return false;
}

async function init() {
	fetch('/publicSigningKey')
		.then(response => response.arrayBuffer())
		.then(key => this.publicSigningKey = key)
		.finally(() => console.info('Application Server Public Key fetched from the server'));

	await navigator.serviceWorker.register("/sw.js", {
		scope: "/"
	});

	await navigator.serviceWorker.ready;
	console.info('Service Worker has been installed and is ready');
	navigator.serviceWorker.addEventListener('message', event => displayLastMessages());

	//displayLastMessages();
}

function displayLastMessages() {
	caches.open('data').then(dataCache => {
		dataCache.match('notification')
			.then(response => response ? response.text() : '')
			.then(txt => {
				notificationOutput.innerHTML = "";
				notificationOutput.innerHTML = txt;
				/*
				txt = txt.split("(")[1];
				txt = txt.split(")")[0];
				txt = txt.split(",");

				for (i = 0; i < txt.length; i++) {
					line = txt[i].split("=");
					notificationOutput.innerHTML += "<div><span class='field'>" + line[0] + "&emsp;</span><span class='value'>" + line[1] + "</span></h4>";
				}
				*/

			});

	});
}

async function unsubscribe() {
	const registration = await navigator.serviceWorker.ready;
	const subscription = await registration.pushManager.getSubscription();
	if (subscription) {
		const successful = await subscription.unsubscribe();
		if (successful) {
			console.info('Unsubscription successful');

			await fetch("/unsubscribe/" + email.value, {
				method: 'POST',
				body: JSON.stringify({ endpoint: subscription.endpoint }),
				headers: {
					"content-type": "application/json"
				}
			});

			console.info('Unsubscription info sent to the server');

			subscribeButton.disabled = false;
			unsubscribeButton.disabled = true;
			email.disabled = false;

		}
		else {
			console.error('Unsubscription failed');
		}
	}
}

async function subscribe() {
	if (email.value === "") {
		alert.style.display = "block";
		return;
	} else {
		alert.style.display = "none";
	}
	const registration = await navigator.serviceWorker.ready;
	const subscription = await registration.pushManager.subscribe({
		userVisibleOnly: true,
		applicationServerKey: this.publicSigningKey
	});

	console.info(`Subscribed to Push Service: ${subscription.endpoint}`);
	
	// sends subsription information to the app
	await fetch("/subscribe/" + email.value, {
		method: 'POST',
		body: JSON.stringify(subscription),
		headers: {
			"content-type": "application/json"
		}
	});

	console.info('Subscription info sent to the server');

	subscribeButton.disabled = true;
	unsubscribeButton.disabled = false;
	email.disabled = true;

}

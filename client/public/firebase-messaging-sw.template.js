importScripts('https://www.gstatic.com/firebasejs/9.23.0/firebase-app-compat.js');
importScripts('https://www.gstatic.com/firebasejs/9.23.0/firebase-messaging-compat.js');

const firebaseConfig = {
    apiKey: "__VITE_API_KEY__",
    authDomain: "__VITE_AUTH_DOMAIN__",
    projectId: "__VITE_PROJECT_ID__",
    storageBucket: "__VITE_STORAGE_BUCKET__",
    messagingSenderId: "__VITE_MESSAGING_SENDER_ID__", 
    appId: "__VITE_APP_ID__",
    measurementId: "__VITE_MEASUREMENT_ID__"
}

firebase.initializeApp(firebaseConfig);
const messaging = firebase.messaging();

messaging.onBackgroundMessage(function(payload) {
    console.log("Received background message ", payload);

    payload.notificationOptions = {
        body: payload.notification.body,
        icon: payload.notification.icon || '/icon.png',
        data: payload.data
    };

    self.registration.showNotification(notificationTitle, notificationOptions);
});

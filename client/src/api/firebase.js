import { initializeApp } from "firebase/app";
import { getMessaging, getToken, onMessage } from "firebase/messaging";

const firebaseConfig = {
    apiKey: import.meta.env.VITE_API_KEY,
    authDomain: import.meta.env.VITE_AUTH_DOMAIN,
    projectId: import.meta.env.VITE_PROJECT_ID,
    storageBucket: import.meta.env.VITE_STORAGE_BUCKET,
    messagingSenderId: import.meta.env.VITE_MESSAGING_SENDER_ID, 
    appId: import.meta.env.VITE_APP_ID,
    measurementId: import.meta.env.VITE_MEASUREMENT_ID
}

const app = initializeApp(firebaseConfig);
const messaging = getMessaging(app);

/**
 * FCM 토큰을 요청하고 반환한다.
 */
export const requestForToken = async () => {
    try {
        const currentToken = await getToken(messaging, { vapidKey: import.meta.env.VITE_VAPID_KEY});
        if (currentToken) {
            console.log("current token for client : ", currentToken);
        } else {
        console.log("FCM Token 요청에 실패했습니다.");

        }
        //TODO: 발급받은 토큰을 서버로 전송하여 저장한다.
    }
}
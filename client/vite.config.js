import { defineConfig, loadEnv } from 'vite';
import react from '@vitejs/plugin-react';
import compression from 'vite-plugin-compression';
import fs from 'fs';
import path from 'path';

// https://vite.dev/config/
export default defineConfig(({ mode }) => {
  //환경변수 로드
  const env = loadEnv(mode, process.cwd(), '');

  return {
      plugins: [
        react(),
        compression({
          algorithm: 'gzip',
          ext: '.gz',
          threshold: 1024,
          deleteOriginFile: false,
        }),

        //Service Worker 생성 플러그인 추가
        {
          name: 'generate-firebase-sw',
          configResolved() {
            //빌드 시작 시 실행
            generateServiceWorker(env);
          },
          configureServer() {
            //개발 서버 시작 시 실행
            generateServiceWorker(env);
          }
        }
      ],
  }
});

function generateServiceWorker(env) {
  try {
    //1. 템플릿 파일 경로
    const templatePath = path.resolve(__dirname, 'public/firebase-messaging-sw.template.js');

    if(!fs.existsSync(templatePath)) {
      console.warn('firebase-messaging-sw template.js 파일을 찾을 수 없습니다.', templatePath);
      return;
    }

    //2. 템플릿 파일 읽기
    let swContent = fs.readFileSync(templatePath, 'utf-8');

    //3. placeholder를 실제 환경변수로 치환
    const replacements = {
      '__VITE_API_KEY__': env.VITE_API_KEY || '',
      '__VITE_AUTH_DOMAIN__': env.VITE_AUTH_DOMAIN || '',
      '__VITE_PROJECT_ID__': env.VITE_PROJECT_ID || '',
      '__VITE_STORAGE_BUCKET__': env.VITE_STORAGE_BUCKET || '',
      '__VITE_MESSAGING_SENDER_ID__': env.VITE_MESSAGING_SENDER_ID || '',
      '__VITE_APP_ID__': env.VITE_APP_ID || '',
      '__VITE_MEASUREMENT_ID__': env.VITE_MEASUREMENT_ID || ''
    };

    // 모든 placeholder 치환
    Object.entries(replacements).forEach(([placeholder, value]) => {
      swContent = swContent.replace(new RegExp(placeholder, 'g'), value);
    });

    //4. public 폴더에 생성 (개발 모드용)
    const publicSwPath = path.resolve(__dirname, 'public/firebase-messaging-sw.js');
    fs.writeFileSync(publicSwPath, swContent);
    console.log('Service Worker 생성 완료 (개발)', publicSwPath);

    //5. dist 폴더에도 생성 (프로덕션용)
    const distDir = path.resolve(__dirname, 'dist');
    if(fs.existsSync(distDir)) {
      const distSwPath = path.resolve(distDir, 'firebase-messaging-sw.js');
      fs.writeFileSync(distSwPath, swContent);
      console.log('Service Worker 생성 완료 (빌드)', distSwPath);
    }
  
  } catch (error) {
    console.error('Service Worker 생성 중 오류', error.message);
  }
}
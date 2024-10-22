import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import config from './config'

const backendURL = config.backendURL; 

export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      '/api': {
        target: backendURL,
        changeOrigin: true,
        secure: false,
        configure: (proxy, _options) => {
          proxy.on('proxyReq', (proxyReq, req, _res) => {

            console.log("On req: " + backendURL);

            const cookies = req.headers.cookie ? req.headers.cookie.split(';') : [];
            let accessToken = '';
            let refreshToken = '';

            cookies.forEach(cookie => {
              const [name, value] = cookie.trim().split('=');
              if (name === 'accessToken') accessToken = value;
              if (name === 'refreshToken') refreshToken = value;
            });

            if (accessToken) {
              proxyReq.setHeader('Authorization', accessToken);
            }
            if (refreshToken) {
              proxyReq.setHeader('X-Refresh-Token', refreshToken);
            }
          });

          proxy.on('proxyRes', (proxyRes, req, res) => {

            console.log("On res: " + backendURL);

            const accessToken = proxyRes.headers['authorization'];
            const refreshToken = proxyRes.headers['x-refresh-token'];
            
            let body = '';
            proxyRes.on('data', (chunk) => {
                body += chunk.toString();
            });
            
            proxyRes.on('end', () => {
                console.log('Response body:', body);               
            });
      


            if (accessToken && refreshToken) {
              res.setHeader('Set-Cookie', [
                `accessToken=${accessToken}; Path=/; HttpOnly; SameSite=Strict`,
                `refreshToken=${refreshToken}; Path=/; HttpOnly; SameSite=Strict`
              ]);
            } else if (accessToken) {
              res.setHeader('Set-Cookie', `accessToken=${accessToken}; Path=/; HttpOnly; SameSite=Strict`);
            } else if (refreshToken) {
              res.setHeader('Set-Cookie', `refreshToken=${refreshToken}; Path=/; HttpOnly; SameSite=Strict`);
            }
            
            delete proxyRes.headers['authorization'];
            delete proxyRes.headers['x-refresh-token'];
          });
        },
      }
    }
  }
})
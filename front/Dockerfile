FROM node:20-alpine as build

WORKDIR /app

COPY ./front .

RUN npm install

EXPOSE 5173

RUN npm run build

CMD ["npm", "run", "dev", "--", "--host", "0.0.0.0"]
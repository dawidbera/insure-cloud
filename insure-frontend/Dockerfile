# Stage 1: Build Angular application
FROM node:23-alpine AS build
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm install
COPY . .
RUN npm run build -- --output-path=dist/insure-frontend --configuration=production

# Stage 2: Serve application with Nginx
FROM nginx:alpine
COPY --from=build /app/dist/insure-frontend/browser /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]

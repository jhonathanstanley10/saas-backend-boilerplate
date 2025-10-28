# frontend-boilerplate/Dockerfile (Updated)

# --- Stage 1: Build (Unchanged) ---
FROM node:20 as builder
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build -- --configuration production

# --- Stage 2: Serve ---
FROM nginx:alpine

# Remove default Nginx config
RUN rm /etc/nginx/conf.d/default.conf

# !! ADDED: Copy the custom Nginx config from the build context into the image !!
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Copy the built Angular app from the 'builder' stage
COPY --from=builder /app/dist/saas-boilerplate-frontend/browser /usr/share/nginx/html

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]

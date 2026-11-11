FROM node:22-alpine
WORKDIR /app

# Instalar dependencias
COPY package*.json ./
RUN npm install

# Copiar código fuente
COPY . .

EXPOSE 4200

# Arranca Angular Dev Server con host 0.0.0.0, proxy a backend en docker y polling para hot-reload
CMD ["npm", "run", "start:docker"]

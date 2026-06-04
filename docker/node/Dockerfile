FROM node:22-alpine

WORKDIR /app

# Instalar dependencias necesarias para node-gyp u otros si fuera necesario
RUN apk add --no-cache libc6-compat

# Copiar archivos de dependencias
COPY package*.json ./

# Instalar dependencias
RUN npm install

# Copiar el resto del código
COPY . .

EXPOSE 3000

# Comando por defecto para desarrollo
CMD ["npm", "run", "dev"]

FROM php:8.4-fpm

# Instalar dependencias del sistema
RUN apt-get update && apt-get install -y \
    libpq-dev \
    libpng-dev \
    libzip-dev \
    zip \
    unzip \
    git \
    curl

# Instalar extensiones de PHP
RUN docker-php-ext-install pdo_pgsql pdo_mysql bcmath gd zip

# Instalar Redis
RUN pecl install redis && docker-php-ext-enable redis

WORKDIR /var/www/html

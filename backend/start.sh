#!/bin/bash
set -e

# Ensure public/storage symlink exists
rm -rf public/storage
php artisan storage:link --relative || true

# Start queue worker in background to process asynchronous jobs (alerts, emails, WhatsApp)
php artisan queue:work --sleep=3 --tries=3 --timeout=90 &

# Start scheduler in background to execute cron jobs (verify-inactivity every 5 mins)
php artisan schedule:work &

# Start web server as main process
if [ -n "$START_COMMAND" ]; then
    exec $START_COMMAND
else
    exec php artisan serve --host=0.0.0.0 --port="${PORT:-8080}"
fi

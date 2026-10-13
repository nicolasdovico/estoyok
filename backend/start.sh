#!/bin/bash
set -e

# Ensure public/storage symlink exists
rm -rf public/storage
php artisan storage:link --relative || true

# Start queue worker in background with auto-restart supervisor loop
(while true; do
    echo "[$(date -u +'%Y-%m-%dT%H:%M:%SZ')] Starting queue:work..."
    php artisan queue:work --queue="${REDIS_QUEUE:-default},dev_default" --sleep=3 --tries=3 --timeout=90 || true
    echo "[$(date -u +'%Y-%m-%dT%H:%M:%SZ')] Queue worker exited ($?), restarting in 3s..."
    sleep 3
done) &

# Start scheduler in background with auto-restart supervisor loop
(while true; do
    echo "[$(date -u +'%Y-%m-%dT%H:%M:%SZ')] Starting schedule:work..."
    php artisan schedule:work || true
    echo "[$(date -u +'%Y-%m-%dT%H:%M:%SZ')] Scheduler exited ($?), restarting in 3s..."
    sleep 3
done) &

# Start web server as main process
if [ -n "$START_COMMAND" ]; then
    exec $START_COMMAND
else
    exec php artisan serve --host=0.0.0.0 --port="${PORT:-8080}"
fi

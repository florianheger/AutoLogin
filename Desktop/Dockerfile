FROM python:3.11-slim

# Create app directory
WORKDIR /app

# Copy files
COPY your_script.py .
COPY logindata .
COPY cronjob /etc/cron.d/dailyjob

# Install cron
RUN apt-get update && \
    apt-get install -y cron && \
    chmod 0644 /etc/cron.d/dailyjob && \
    crontab /etc/cron.d/dailyjob

# Create log file
RUN touch /var/log/cron.log

# Start cron and tail logs
CMD cron && tail -f /var/log/cron.log

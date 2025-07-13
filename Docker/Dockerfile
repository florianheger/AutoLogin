FROM python:3.11-slim

# install pip dependencies
RUN pip install --no-cache-dir --upgrade pip \
  && pip install --no-cache-dir selenium

# Create app directory
WORKDIR /app

# Install dependencies and cron
RUN apt-get update && \
    apt-get install -y --no-install-recommends cron curl ca-certificates gnupg && \
    rm -rf /var/lib/apt/lists/*

# Copy files
COPY main.py .
COPY logindata .
COPY cronjob /etc/cron.d/dailyjob

# Set correct permissions for cron job and install it
RUN chmod 0644 /etc/cron.d/dailyjob && \
    crontab /etc/cron.d/dailyjob

# Create a log file to be able to run tail
RUN touch /var/log/cron.log

# Start cron and tail the log file
CMD ["sh", "-c", "cron && tail -f /var/log/cron.log"]
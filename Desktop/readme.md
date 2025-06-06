docker build -t scheduled-runner .
docker run -d --name scheduled-script scheduled-runner
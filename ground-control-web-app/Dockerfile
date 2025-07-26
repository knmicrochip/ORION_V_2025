# ---- Base Image Stage ----
FROM python:3.12-slim AS base

# Install curl
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Set up a non-root user
RUN useradd -ms /bin/bash appuser

# Install uv
RUN curl -LsSf https://astral.sh/uv/install.sh | sh
ENV PATH="/root/.local/bin:$PATH"

# Copy project files and install dependencies
WORKDIR /home/appuser
COPY . .

RUN uv sync

# Expose the port and run the application
ENV LOG_LEVEL=DEBUG
EXPOSE 8080
CMD ["uv", "run", "main.py", "--host", "0.0.0.0", "--port", "8080"]

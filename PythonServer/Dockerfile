FROM node:20-slim AS frontend
WORKDIR /app/vitaliti-frontend
COPY vitaliti-frontend/package*.json ./
RUN npm install
COPY vitaliti-frontend/ ./
RUN npm run build

FROM python:3.11-slim
WORKDIR /app

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY . .
COPY --from=frontend /app/static ./static

EXPOSE 8000
CMD ["uvicorn", "app:app", "--host", "0.0.0.0", "--port", "8000"]
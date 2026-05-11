# Kubernetes quick start

This is a fast scaffold for running the app in Kubernetes with:

- `postgres`
- `backend`
- `gateway`
- `frontend`
- `ingress-nginx`

## Build local images

```bash
docker build -t lab1-backend:latest ./backend
docker build -t lab1-gateway:latest ./gateway
docker build -t lab1-frontend:latest ./frontend
```

If you use `minikube`, load them:

```bash
minikube image load lab1-backend:latest
minikube image load lab1-gateway:latest
minikube image load lab1-frontend:latest
```

If you use `kind`, load them:

```bash
kind load docker-image lab1-backend:latest
kind load docker-image lab1-gateway:latest
kind load docker-image lab1-frontend:latest
```

## Apply manifests

```bash
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/postgres.yaml
kubectl apply -f k8s/backend.yaml
kubectl apply -f k8s/gateway.yaml
kubectl apply -f k8s/frontend.yaml
kubectl apply -f k8s/ingress.yaml
```

## Hostname

Add this to `/etc/hosts`:

```text
127.0.0.1 lab1.local
```

Then open:

```text
http://lab1.local
```

`/api`, `/swagger-ui`, and `/v3/api-docs` are proxied by the frontend nginx to the gateway service.

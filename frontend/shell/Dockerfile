FROM node:24.12.0-alpine3.23 AS build
WORKDIR /workspace
COPY frontend/package.json frontend/package-lock.json frontend/tsconfig.base.json frontend/eslint.config.mjs ./
COPY frontend/shared/package.json shared/package.json
COPY frontend/shell/package.json shell/package.json
COPY frontend/requests-mfe/package.json requests-mfe/package.json
RUN npm ci
COPY frontend/shared shared
COPY frontend/shell shell
ARG KEYCLOAK_URL=http://localhost:8080
ARG KEYCLOAK_REALM=operational-requests
ARG KEYCLOAK_CLIENT_ID=operational-requests-web
ARG REQUESTS_API_URL=http://localhost:18081
ARG INDICATORS_API_URL=http://localhost:8082
ARG REQUESTS_MFE_URL=http://localhost:4201/remoteEntry.js
ENV KEYCLOAK_URL=$KEYCLOAK_URL KEYCLOAK_REALM=$KEYCLOAK_REALM KEYCLOAK_CLIENT_ID=$KEYCLOAK_CLIENT_ID REQUESTS_API_URL=$REQUESTS_API_URL INDICATORS_API_URL=$INDICATORS_API_URL REQUESTS_MFE_URL=$REQUESTS_MFE_URL
RUN npm run build -w shell
FROM nginxinc/nginx-unprivileged:1.29.4-alpine3.23
COPY frontend/shell/nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /workspace/shell/dist /usr/share/nginx/html
EXPOSE 8080
HEALTHCHECK --interval=10s --timeout=3s --retries=5 CMD wget -q -O /dev/null http://127.0.0.1:8080/ || exit 1

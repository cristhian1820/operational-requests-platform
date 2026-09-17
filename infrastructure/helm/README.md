# Despliegue Helm

El chart `operational-requests` despliega únicamente los componentes propios:

- `requests-service`
- `indicators-service`
- `shell`
- `requests-mfe`

SQL Server operacional, SQL Server analítico, Kafka y Keycloak son dependencias externas. Deben estar accesibles desde el clúster y sus URLs/bootstrap servers se configuran en `external`.

## Secret externo

Por defecto el chart **no crea** Secrets. Antes de instalar, crea un Secret con las claves esperadas:

```bash
kubectl create secret generic operational-requests-runtime \
  --from-literal=requests-db-password="$REQUESTS_DB_PASSWORD" \
  --from-literal=indicators-db-password="$INDICATORS_DB_PASSWORD"
```

También se puede usar otro nombre con `--set secrets.existingSecret=...`. La creación opcional del Secret (`secrets.create=true`) exige que los valores se suministren externamente mediante `--set-string`; nunca se almacenan en `values.yaml`.

## Validación y despliegue

Desde la raíz:

```bash
helm lint infrastructure/helm/operational-requests
helm template operational-requests infrastructure/helm/operational-requests \
  -f infrastructure/helm/operational-requests/values-dev.yaml
helm upgrade --install operational-requests infrastructure/helm/operational-requests \
  -f infrastructure/helm/operational-requests/values-dev.yaml \
  --namespace operational-requests --create-namespace
kubectl rollout status deployment/operational-requests-operational-requests-requests -n operational-requests
helm uninstall operational-requests -n operational-requests
```

`values-dev.yaml` contiene referencias de desarrollo y nombres de imagen configurables, no credenciales. Para producción deben fijarse repositorios y tags inmutables, endpoints externos con TLS, requests/limits revisados y políticas de red.

## Frontend y CORS

Las URLs del shell y del MFE se incorporan durante el `npm build` de las imágenes Docker. Helm no puede cambiar esos valores dentro de nginx en runtime; por eso `frontend.*` documenta los valores usados al construir cada imagen y requiere reconstrucción para cambiar de entorno. El `remoteEntry` debe ser una URL accesible por el navegador, no solo un DNS interno del clúster.

El chart expone `cors.allowedOrigins` en la configuración de despliegue. La implementación backend actual mantiene los orígenes locales codificados; antes de producción debe hacerse configurable mediante la aplicación o un gateway. No se usa wildcard con credenciales.

Ingress está deshabilitado por defecto (`ingress.enabled=false`). Al habilitarlo se crean hosts separados para shell y MFE; TLS y anotaciones se suministran desde los valores del entorno.

## Probes y dependencias

Los backends usan `/actuator/health/readiness` y `/actuator/health/liveness`. Shell usa `/` y requests-mfe usa `/remoteEntry.js`. Kubernetes no impone orden de arranque: los pods reinician y las probes permiten recuperarse cuando SQL Server, Kafka o Keycloak externos tardan en estar disponibles.

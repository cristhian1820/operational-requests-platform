# ADR-004: Module Federation y propiedad de sesión

## Decisión

El shell es dueño de Keycloak, layout y rutas globales. `requests-mfe` expone sus rutas mediante Module Federation y recibe un puente de sesión que entrega tokens desde memoria. Puede iniciar standalone con su propia instancia Keycloak solo para desarrollo aislado.

Las dependencias que mantienen contexto o estado son singletons con versión estricta. El paquete `shared` contiene únicamente contratos Zod, HTTP y tipos/slice de sesión para evitar duplicación real.

## Consecuencias

No se almacenan tokens en Redux ni Web Storage y una recarga reconstruye sesión desde SSO. El remoteEntry es configurable en build y se sirve sin caché agresiva; chunks versionados sí usan caché larga. La disponibilidad del remoto es requisito para sus rutas, mientras el inicio e indicadores siguen siendo responsabilidad del shell.

## Alternativas

Una SPA monolítica sería más simple pero no cumpliría el despliegue federado solicitado. Dar a cada remoto su propia sesión produciría renovaciones y estados divergentes. Un paquete común mayor aumentaría acoplamiento innecesario.

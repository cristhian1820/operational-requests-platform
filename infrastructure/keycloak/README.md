# Keycloak local

El export crea el realm, roles y usuarios exclusivamente demostrativos. Las contraseñas `LocalOnly_*` son temporales, ficticias y nunca deben reutilizarse fuera de este entorno. Las credenciales administrativas sí se inyectan desde `.env`.

Keycloak no interpola variables de entorno dentro de un archivo JSON de importación de realm. Por eso las claves de los tres usuarios están explícitas y marcadas como temporales y locales; en cambio, la cuenta administrativa se configura mediante variables de Compose. Un despliegue real debe aprovisionar usuarios mediante un gestor de secretos o la API administrativa, no reutilizar este export.

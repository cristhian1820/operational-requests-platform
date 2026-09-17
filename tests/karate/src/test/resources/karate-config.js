function fn() {
  var env = karate.env || 'local';
  var prop = function (name, envName, fallback) { return karate.properties[name] || java.lang.System.getenv(envName) || fallback; };
  var config = {
    env: env,
    apiUrl: karate.properties['requests.api.url'] || 'http://localhost:18081',
    keycloakUrl: karate.properties['keycloak.url'] || 'http://localhost:8080',
    realm: karate.properties['keycloak.realm'] || 'operational-requests',
    clientId: prop('keycloak.acceptance.client', 'KARATE_KEYCLOAK_ACCEPTANCE_CLIENT', 'operational-requests-acceptance'),
    users: {
      solicitante: { username: prop('acceptance.solicitante.username', 'KARATE_SOLICITANTE_USERNAME', 'solicitante'), password: prop('acceptance.solicitante.password', 'KARATE_SOLICITANTE_PASSWORD', 'LocalOnly_Solicitante_2026!') },
      analista: { username: prop('acceptance.analista.username', 'KARATE_ANALISTA_USERNAME', 'karate-analista-a'), password: prop('acceptance.analista.password', 'KARATE_ANALISTA_PASSWORD', 'KarateOnly_AnalistaA_2026!') },
      analista2: { username: prop('acceptance.analista2.username', 'KARATE_ANALISTA2_USERNAME', 'karate-analista-b'), password: prop('acceptance.analista2.password', 'KARATE_ANALISTA2_PASSWORD', 'KarateOnly_AnalistaB_2026!') },
      supervisor: { username: prop('acceptance.supervisor.username', 'KARATE_SUPERVISOR_USERNAME', 'karate-supervisor'), password: prop('acceptance.supervisor.password', 'KARATE_SUPERVISOR_PASSWORD', 'KarateOnly_Supervisor_2026!') }
    }
  };
  karate.configure('connectTimeout', 10000);
  karate.configure('readTimeout', 30000);
  return config;
}

Feature: API operacional - criterios A1 a A4

Background:
  * url apiUrl
  * def tokenFeature = read('classpath:co/com/operationalrequests/acceptance/token.feature')
  * def concurrent = Java.type('co.com.operationalrequests.acceptance.ConcurrentAssignment')
  * def categoryId = '11111111-1111-1111-1111-111111111111'

Scenario: A1 crear una solicitud correctamente
  * def auth = call tokenFeature { username: '#(users.solicitante.username)', password: '#(users.solicitante.password)' }
  Given path 'api/v1/solicitudes'
  And header Authorization = 'Bearer ' + auth.accessToken
  And request { asunto: 'Karate A1', descripcion: 'Solicitud creada por prueba de aceptación', categoriaId: '#(categoryId)', prioridad: 'MEDIA' }
  When method post
  Then status 201
  And match response.id == '#uuid'
  And match response.estado == 'REGISTRADA'
  And match response.categoriaId == categoryId
  And match response.historial[0].estadoNuevo == 'REGISTRADA'
  * def createdId = response.id
  Given path 'api/v1/solicitudes', createdId
  And header Authorization = 'Bearer ' + auth.accessToken
  When method get
  Then status 200
  And match response.numero == '#string'

Scenario: A2 dos analistas compiten por la misma solicitud
  * def requester = call tokenFeature { username: '#(users.solicitante.username)', password: '#(users.solicitante.password)' }
  * def analystA = call tokenFeature { username: '#(users.analista.username)', password: '#(users.analista.password)' }
  * def analystB = call tokenFeature { username: '#(users.analista2.username)', password: '#(users.analista2.password)' }
  Given path 'api/v1/solicitudes'
  And header Authorization = 'Bearer ' + requester.accessToken
  And request { asunto: 'Karate A2', descripcion: 'Solicitud para carrera de asignación', categoriaId: '#(categoryId)', prioridad: 'BAJA' }
  When method post
  Then status 201
  * def createdId = response.id
  * def race = concurrent.race(apiUrl, createdId, analystA.accessToken, analystB.accessToken)
  * def statuses = race
  * match statuses contains 200
  * match statuses contains 409
  Given path 'api/v1/solicitudes', createdId
  And header Authorization = 'Bearer ' + analystA.accessToken
  When method get
  Then status 200
  And match response.estado == 'EN_ATENCION'

Scenario: A3 solicitante no puede asignar
  * def requester = call tokenFeature { username: '#(users.solicitante.username)', password: '#(users.solicitante.password)' }
  Given path 'api/v1/solicitudes'
  And header Authorization = 'Bearer ' + requester.accessToken
  And request { asunto: 'Karate A3', descripcion: 'Solicitud para autorización', categoriaId: '#(categoryId)', prioridad: 'ALTA' }
  When method post
  Then status 201
  * def createdId = response.id
  Given path 'api/v1/solicitudes', createdId, 'asignaciones'
  And header Authorization = 'Bearer ' + requester.accessToken
  When method post
  Then status 403
  And match response.errorCode == 'ACCESS_DENIED'

Scenario: A4 transición inválida devuelve error de negocio
  * def requester = call tokenFeature { username: '#(users.solicitante.username)', password: '#(users.solicitante.password)' }
  * def supervisor = call tokenFeature { username: '#(users.supervisor.username)', password: '#(users.supervisor.password)' }
  Given path 'api/v1/solicitudes'
  And header Authorization = 'Bearer ' + requester.accessToken
  And request { asunto: 'Karate A4', descripcion: 'Solicitud para transición inválida', categoriaId: '#(categoryId)', prioridad: 'MEDIA' }
  When method post
  Then status 201
  * def createdId = response.id
  Given path 'api/v1/solicitudes', createdId, 'transiciones'
  And header Authorization = 'Bearer ' + supervisor.accessToken
  And request { estadoDestino: 'CERRADA', motivo: 'Salto no permitido' }
  When method post
  Then status 422
  And match response.errorCode == 'INVALID_TRANSITION'

Feature: token local de aceptación

Scenario: obtener token
  Given url keycloakUrl + '/realms/' + realm + '/protocol/openid-connect/token'
  And form field username = username
  And form field password = password
  And form field grant_type = 'password'
  And form field client_id = clientId
  When method post
  Then status 200
  And match response.access_token == '#string'
  * def accessToken = response.access_token

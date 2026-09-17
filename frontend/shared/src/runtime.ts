import {z} from 'zod';
export const RuntimeConfigSchema=z.object({keycloakUrl:z.url(),keycloakRealm:z.string().min(1),keycloakClientId:z.string().min(1),requestsApiUrl:z.url(),indicatorsApiUrl:z.url(),requestsMfeUrl:z.url()});
export type RuntimeConfig=z.infer<typeof RuntimeConfigSchema>;
export const runtimeConfig=RuntimeConfigSchema.parse({
 keycloakUrl:process.env.KEYCLOAK_URL??'http://localhost:8080', keycloakRealm:process.env.KEYCLOAK_REALM??'operational-requests', keycloakClientId:process.env.KEYCLOAK_CLIENT_ID??'operational-requests-web',
 requestsApiUrl:process.env.REQUESTS_API_URL??'http://localhost:18081', indicatorsApiUrl:process.env.INDICATORS_API_URL??'http://localhost:8082', requestsMfeUrl:process.env.REQUESTS_MFE_URL??'http://localhost:4201/remoteEntry.js'});

package co.com.operationalrequests.acceptance;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/** Small real HTTP race used by A2; it does not bypass the API or database. */
public final class ConcurrentAssignment {
    private ConcurrentAssignment() {}

    public static List<Integer> race(String endpoint, String requestId, String tokenA, String tokenB) {
        var client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
        var first = send(client, endpoint, requestId, tokenA);
        var second = send(client, endpoint, requestId, tokenB);
        CompletableFuture.allOf(first, second).join();
        return List.of(first.join().statusCode(), second.join().statusCode());
    }

    private static CompletableFuture<HttpResponse<Void>> send(HttpClient client, String endpoint, String id, String token) {
        var request = HttpRequest.newBuilder(URI.create(endpoint + "/api/v1/solicitudes/" + id + "/asignaciones"))
                .timeout(Duration.ofSeconds(20)).header("Authorization", "Bearer " + token)
                .header("X-Correlation-Id", java.util.UUID.randomUUID().toString()).POST(HttpRequest.BodyPublishers.noBody()).build();
        return client.sendAsync(request, HttpResponse.BodyHandlers.discarding());
    }
}

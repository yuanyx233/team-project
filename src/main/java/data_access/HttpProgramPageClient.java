package data_access;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public final class HttpProgramPageClient implements ProgramPageClient {
    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5)).followRedirects(HttpClient.Redirect.NEVER).build();

    @Override
    public String fetch(URI uri) throws IOException, InterruptedException {
        // Only public, explicitly configured official program pages are fetched.
        if (!"https".equals(uri.getScheme()) || !"artsci.calendar.utoronto.ca".equals(uri.getHost())
                || !uri.getPath().matches("/program/asmaj[0-9]+") || uri.getQuery() != null) {
            throw new IOException("Only allowlisted U of T major page paths are supported.");
        }
        HttpRequest request = HttpRequest.newBuilder(uri).timeout(Duration.ofSeconds(5))
                .header("User-Agent", "StressdewValley-StudentProject/1.0")
                .header("Accept", "text/html").GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) { throw new IOException("Program page HTTP " + response.statusCode()); }
        String type = response.headers().firstValue("Content-Type").orElse("");
        if (!type.toLowerCase(java.util.Locale.ROOT).contains("text/html")
                || response.body().length() > 2_000_000) {
            throw new IOException("Invalid program page response.");
        }
        return response.body();
    }
}

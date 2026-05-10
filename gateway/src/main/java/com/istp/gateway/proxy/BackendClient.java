package com.istp.gateway.proxy;

import com.istp.gateway.security.CurrentUser;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URI;
import java.util.Enumeration;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class BackendClient {

    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String USER_EMAIL_HEADER = "X-User-Email";
    private static final String USER_ROLE_HEADER = "X-User-Role";

    private final RestClient.Builder restClientBuilder;
    private RestClient restClient;

    @Value("${app.backend.base-url}")
    private String backendBaseUrl;

    @PostConstruct
    void init() {
        this.restClient = restClientBuilder.baseUrl(backendBaseUrl).build();
    }

    public ResponseEntity<byte[]> forward(HttpServletRequest request, byte[] body, CurrentUser currentUser) {
        HttpHeaders headers = buildForwardHeaders(request, currentUser, true);
        RestClient.RequestBodySpec requestSpec = restClient
                .method(HttpMethod.valueOf(request.getMethod()))
                .uri(buildTargetUri(request))
                .headers(httpHeaders -> httpHeaders.addAll(headers));

        return execute(requestSpec, body);
    }

    public ResponseEntity<byte[]> forwardMultipart(HttpServletRequest request, MultipartFile file, CurrentUser currentUser) {
        HttpHeaders headers = buildForwardHeaders(request, currentUser, false);
        LinkedMultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();

        HttpHeaders partHeaders = new HttpHeaders();
        partHeaders.setContentType(file.getContentType() == null
                ? MediaType.APPLICATION_OCTET_STREAM
                : MediaType.parseMediaType(file.getContentType()));
        partHeaders.setContentDispositionFormData("file", file.getOriginalFilename());

        try {
            parts.add("file", new HttpEntity<>(new NamedByteArrayResource(file.getBytes(), file.getOriginalFilename()), partHeaders));
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to read upload body", exception);
        }

        RestClient.RequestBodySpec requestSpec = restClient
                .post()
                .uri(buildTargetUri(request))
                .headers(httpHeaders -> httpHeaders.addAll(headers))
                .contentType(MediaType.MULTIPART_FORM_DATA);

        return execute(requestSpec, parts);
    }

    private ResponseEntity<byte[]> execute(RestClient.RequestBodySpec requestSpec, Object body) {
        return requestSpec.body(body).exchange((clientRequest, clientResponse) ->
                toResponseEntity(clientResponse.getStatusCode(), clientResponse.getHeaders(), readResponseBody(clientResponse))
        );
    }

    private ResponseEntity<byte[]> execute(RestClient.RequestBodySpec requestSpec, byte[] body) {
        if (body == null || body.length == 0) {
            return requestSpec.exchange((clientRequest, clientResponse) ->
                    toResponseEntity(clientResponse.getStatusCode(), clientResponse.getHeaders(), readResponseBody(clientResponse))
            );
        }
        return requestSpec.body(body).exchange((clientRequest, clientResponse) ->
                toResponseEntity(clientResponse.getStatusCode(), clientResponse.getHeaders(), readResponseBody(clientResponse))
        );
    }

    private ResponseEntity<byte[]> toResponseEntity(HttpStatusCode status, HttpHeaders headers, byte[] body) {
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.putAll(headers);
        responseHeaders.remove(HttpHeaders.TRANSFER_ENCODING);
        responseHeaders.remove(HttpHeaders.CONNECTION);
        return new ResponseEntity<>(body, responseHeaders, status);
    }

    private byte[] readResponseBody(org.springframework.http.client.ClientHttpResponse response) {
        try {
            if (response.getBody() == null) {
                return new byte[0];
            }
            return StreamUtils.copyToByteArray(response.getBody());
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to read backend response", exception);
        }
    }

    private HttpHeaders buildForwardHeaders(HttpServletRequest request, CurrentUser currentUser, boolean includeContentType) {
        HttpHeaders headers = new HttpHeaders();
        Enumeration<String> headerNames = request.getHeaderNames();

        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            if (shouldSkipHeader(headerName, includeContentType)) {
                continue;
            }

            Enumeration<String> headerValues = request.getHeaders(headerName);
            while (headerValues.hasMoreElements()) {
                headers.add(headerName, headerValues.nextElement());
            }
        }

        if (currentUser != null) {
            headers.set(USER_ID_HEADER, String.valueOf(currentUser.id()));
            headers.set(USER_EMAIL_HEADER, currentUser.email());
            headers.set(USER_ROLE_HEADER, currentUser.role().name());
        }

        return headers;
    }

    private boolean shouldSkipHeader(String headerName, boolean includeContentType) {
        if (headerName.equalsIgnoreCase(HttpHeaders.AUTHORIZATION)
                || headerName.equalsIgnoreCase(HttpHeaders.HOST)
                || headerName.equalsIgnoreCase(HttpHeaders.CONTENT_LENGTH)
                || headerName.equalsIgnoreCase(USER_ID_HEADER)
                || headerName.equalsIgnoreCase(USER_EMAIL_HEADER)
                || headerName.equalsIgnoreCase(USER_ROLE_HEADER)) {
            return true;
        }
        return !includeContentType && headerName.equalsIgnoreCase(HttpHeaders.CONTENT_TYPE);
    }

    private URI buildTargetUri(HttpServletRequest request) {
        StringBuilder uri = new StringBuilder(backendBaseUrl).append(request.getRequestURI());
        if (request.getQueryString() != null && !request.getQueryString().isBlank()) {
            uri.append('?').append(request.getQueryString());
        }
        return URI.create(uri.toString());
    }

    private static final class NamedByteArrayResource extends ByteArrayResource {

        private final String filename;

        private NamedByteArrayResource(byte[] byteArray, String filename) {
            super(byteArray);
            this.filename = filename == null ? "file" : filename;
        }

        @Override
        public String getFilename() {
            return filename;
        }
    }
}

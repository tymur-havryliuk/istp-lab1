package com.istp.gateway.proxy;

import com.istp.gateway.security.CurrentUser;
import com.istp.gateway.security.GatewayAuthFilter;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class GatewayProxyController {

    private final BackendClient backendClient;

    @PostMapping(
            value = {"/files", "/reports/grades/import"},
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<byte[]> multipartProxy(HttpServletRequest request, @RequestParam("file") MultipartFile file) {
        return backendClient.forwardMultipart(request, file, getCurrentUser(request));
    }

    @RequestMapping("/**")
    public ResponseEntity<byte[]> proxy(HttpServletRequest request, @RequestBody(required = false) byte[] body) {
        return backendClient.forward(request, body, getCurrentUser(request));
    }

    private CurrentUser getCurrentUser(HttpServletRequest request) {
        return (CurrentUser) request.getAttribute(GatewayAuthFilter.CURRENT_USER_ATTRIBUTE);
    }
}

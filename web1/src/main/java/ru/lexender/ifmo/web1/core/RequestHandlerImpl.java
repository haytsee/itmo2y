package ru.lexender.ifmo.web1.core;

import com.fastcgi.FCGIInterface;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import ru.lexender.ifmo.web1.core.dto.CoordinatesDto;
import ru.lexender.ifmo.web1.core.service.ContourService;
import ru.lexender.ifmo.web1.json.ObjectMapperHolder;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class RequestHandlerImpl implements RequestHandler {
    ContourService contourService;
    FCGIInterface fcgiInterface = new FCGIInterface();

    @Override
    public void handle() {
        String content = """
                {
                    "result": %s
                }
                """;

        while (fcgiInterface.FCGIaccept() >= 0) {
            try {
                var requestBody = readRequestBody();
                error("Test error. Body: " + requestBody);
            } catch (Exception e) {
                error("Invalid request data");
            }
        }
    }

    @Override
    public void error(String message) {
        while (fcgiInterface.FCGIaccept() >= 0) {
            var response = """
                    HTTP/1.1 400 Bad Request
                    Content-Type: application/json
                    Content-Length: %d
                    
                    {
                        "error": "%s"
                    }
                    """.formatted(message.length(), message);

            System.out.println(response);
        }
    }

    private String readRequestBody() throws IOException {
        FCGIInterface.request.inStream.fill();

        var contentLength = FCGIInterface.request.inStream.available();
        var buffer = ByteBuffer.allocate(contentLength);
        var readBytes = FCGIInterface.request.inStream.read(buffer.array(), 0, contentLength);

        var requestBodyRaw = new byte[readBytes];
        buffer.get(requestBodyRaw);
        buffer.clear();

        return new String(requestBodyRaw, StandardCharsets.UTF_8);
    }
}

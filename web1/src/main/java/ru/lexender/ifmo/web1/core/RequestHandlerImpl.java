package ru.lexender.ifmo.web1.core;

import com.fastcgi.FCGIInterface;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import ru.lexender.ifmo.web1.core.dto.CoordinatesDto;
import ru.lexender.ifmo.web1.core.service.ContourService;
import ru.lexender.ifmo.web1.core.validation.ValidationService;
import ru.lexender.ifmo.web1.fcgi.FcgiInterfaceHolder;
import ru.lexender.ifmo.web1.json.ObjectMapperHolder;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class RequestHandlerImpl implements RequestHandler {
    ContourService contourService;
    ValidationService validationService;

    @Override
    public void handle() {
        var fcgiInterface = FcgiInterfaceHolder.getInstance();

        while (fcgiInterface.FCGIaccept() >= 0) {
            try {
                String content = "<td>%s</td>";

                String requestBody = readRequestBody();

                CoordinatesDto coordinates = ObjectMapperHolder
                        .getInstance().readValue(requestBody, CoordinatesDto.class);

                if (!validationService.validate(coordinates)) {
                    error("Request data is invalid");
                    continue;
                }

                String result = contourService.isInsideContour(coordinates) ? "true" : "false";
                content = content.formatted(result);

                String response = """
                    HTTP/2 200 OK
                    Content-Type: text/html
                    Content-Length: %d
                    
                    %s
                    
                    """.formatted(content.getBytes(StandardCharsets.UTF_8).length, content);

                System.out.println(response);
            } catch (Exception e) {
                error("Can't process request");
            }
        }
    }


    @Override
    public void error(String message) {
        message = message.replace("\n", " ");

        String content = """
                <td>%s</td>
                """.formatted(message);


        var response = """
                    HTTP/2 400 Bad Request
                    Content-Type: text/html
                    Content-Length: %d
                    
                    %s
                    
                    """.formatted(content.getBytes(StandardCharsets.UTF_8).length, content);

        System.out.println(response);
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

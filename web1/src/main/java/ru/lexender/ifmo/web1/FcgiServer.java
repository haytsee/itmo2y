package ru.lexender.ifmo.web1;

import com.fastcgi.FCGIInterface;

public class FcgiServer {

    public static void main(String[] args) {

        var fcgiInterface = new FCGIInterface();
        while (fcgiInterface.FCGIaccept() >= 0) {
            var content = """
                    {
                        "result": True
                    }
                    """;

            var response = """
                    HTTP/1.1 200 OK
                    Content-Type: application/json
                    Content-Length: %d
                    
                    %s
                    """.formatted(content.length(), content);

            System.out.println(response);
        }
    }

}

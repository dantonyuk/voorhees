package com.hylamobile.voorhees.example.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hylamobile.voorhees.jsonrpc.JsonRpcException;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

class JsonUtils {

    interface InputStreamSupplier {
        InputStream get() throws IOException;
    }

    static void withJsonRpcException(Runnable runnable) {
        try {
            runnable.run();
        }
        catch (JsonRpcException ex) {
            System.out.println(ex.getClass().getSimpleName());
            System.out.println(ex.getError().getMessage());
            System.out.println(ex.getError().getData());
        }
    }

    static <T> void forJsonStream(InputStreamSupplier input, TypeReference<T> typeRef, Consumer<T> consumer) {
        try (InputStream in = input.get()) {
            String movieJson = StreamUtils.copyToString(in, StandardCharsets.UTF_8);
            ObjectMapper mapper = new ObjectMapper();
            consumer.accept(mapper.readValue(movieJson, typeRef));
        }
        catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }
}

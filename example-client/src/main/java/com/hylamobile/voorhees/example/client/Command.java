package com.hylamobile.voorhees.example.client;

import com.hylamobile.voorhees.example.server.api.RemoteMovieService;

import static com.hylamobile.voorhees.example.client.JsonUtils.withJsonRpcException;

public interface Command {

    default void execute(RemoteMovieService movieService, String arg) {
        withJsonRpcException(() -> executeMe(movieService, arg));
    }

    void executeMe(RemoteMovieService movieService, String arg);
}

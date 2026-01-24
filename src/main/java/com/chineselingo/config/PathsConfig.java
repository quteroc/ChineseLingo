package com.chineselingo.config;

import java.nio.file.Path;

public final class PathsConfig {

    private PathsConfig() {}

    public static Path userStateJson() {
        return Path.of(
                System.getProperty("user.dir"),
                "user-state.json"
        );
    }
}
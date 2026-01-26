package com.chineselingo.user;

import com.chineselingo.config.PathsConfig;
import com.chineselingo.persistence.json.JsonFileRepository;
import com.chineselingo.persistence.json.JsonMapperFactory;
import com.chineselingo.user.dto.UserStateDto;
import com.chineselingo.user.mapper.UserStateMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;

public class UserManager {
    private static final Logger logger = LoggerFactory.getLogger(UserManager.class);
    private final JsonFileRepository<UserStateDto> repository;
    private final UserState userState;

    public UserManager () {
        repository = getRepository();
        userState = loadUserState();
    }

    public UserManager (UserState userState ) {
        repository = getRepository();
        this.userState = userState;
    }

    private UserState loadUserState () {
        UserStateDto dto = null;
        try {
            dto = repository.load();
        } catch (IOException e) {
            logger.error("Cannot load user state. Creating empty UserState. ", e);
        }
        return UserStateMapper.fromDto(dto);
    }
    private JsonFileRepository<UserStateDto>  getRepository() {
        Path path = PathsConfig.userStateJson();
        return new JsonFileRepository<>(JsonMapperFactory.get(), path, UserStateDto.class);
    }

    public UserState getUserState() {
        return userState;
    }

    public void saveUserState() throws IOException {
        repository.save(UserStateMapper.toDto(userState));
    }
}

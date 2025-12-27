package net.seanv.stonegameserver.mappers;

import net.seanv.stonegameserver.dto.responses.UserDto;
import net.seanv.stonegameserver.entities.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "rank", source = "rank")
    UserDto toDto(User user, int rank);
}

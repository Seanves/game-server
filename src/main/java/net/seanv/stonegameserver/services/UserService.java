package net.seanv.stonegameserver.services;

import net.seanv.stonegameserver.entities.User;
import net.seanv.stonegameserver.entities.GameResult;
import net.seanv.stonegameserver.dto.responses.PersonalizedGameResult;
import net.seanv.stonegameserver.dto.responses.UserDto;
import net.seanv.stonegameserver.mappers.UserMapper;
import net.seanv.stonegameserver.repositories.GameResultRepository;
import net.seanv.stonegameserver.repositories.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.IntStream;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final GameResultRepository resultRepository;

    private final UserMapper mapper;

    private final int RESULT_PAGE_SIZE = 10;


    public UserService(UserRepository userRepository,
                       GameResultRepository resultRepository,
                       UserMapper mapper) {
        this.userRepository = userRepository;
        this.resultRepository = resultRepository;
        this.mapper = mapper;
    }


    public UserDto userToDto(User user) {
        return mapper.toDto(user, getRank(user.getId()));
    }

    public void changeNickname(User user, String newNickname) {
        user.setNickname(newNickname);
        userRepository.save(user);
    }

    public Page<PersonalizedGameResult> getGameResults(int userId, int page) {
        Pageable pageable = PageRequest.of(page, RESULT_PAGE_SIZE);
        Page<GameResult> results = resultRepository.getResultsPage(userId, pageable);

        return results.map(r -> new PersonalizedGameResult(r, userId));
    }

    private int getRank(int id) {
        return userRepository.getRank(id).orElseThrow( () -> new IllegalArgumentException("id: " + id) );
    }

    public List<UserDto> getTop10Ranks() {
        List<User> users = userRepository.getTop10Ranks();

        return IntStream.range(0, users.size())
                        .mapToObj(i -> mapper.toDto(users.get(i), i+1))
                        .toList();
    }

}

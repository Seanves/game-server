package com.gameserver.services;

import com.gameserver.entities.User;
import com.gameserver.entities.responses.Stats;
import com.gameserver.repositories.UserRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;

    private Map<Integer,Integer> cachedIdRankMap;
    private List<Object[/* int rank, String nickname, int rating */]> cachedTop10Ranks;

    private static final long CACHE_UPDATE_FREQUENCY = 1000 * 60;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    {
        Thread ranksUpdatingThread = new Thread( () -> {
            while(true) {
                updateIdRankMap();
                updateTop10Ranks();
                try { Thread.sleep(CACHE_UPDATE_FREQUENCY); } catch (InterruptedException e) { throw new RuntimeException(e); }
            }
        });
        ranksUpdatingThread.start();
    }


    public boolean isExists(String login) { return userRepository.existsByLogin(login); }

    public boolean isExists(int id) { return userRepository.existsById(id); }

    public Optional<User> getUser(String login) {
        return userRepository.findByLogin(login);
    }

    public Optional<User> getUser(int id) { return userRepository.findById(id); }

    public void changeNickname(User user, String nickname) {
        user.setNickname(nickname);
        save(user);
    }

    public int getRank(User user) {
        return cachedIdRankMap.get(user.getId());
    }

    public List<Object[]> getTop10Ranks() {
        return cachedTop10Ranks;
    }

    public Stats getStats(User user) {
        return new Stats(user, getRank(user));
    }


    public void save(User user) {
        userRepository.save(user);
    }


    private void updateIdRankMap() {
        cachedIdRankMap = userRepository.ranks().stream()
                   .collect(Collectors.toMap(arr->arr[0], arr->arr[1]));
    }

    private void updateTop10Ranks() { cachedTop10Ranks = userRepository.top10ranks(); }

}

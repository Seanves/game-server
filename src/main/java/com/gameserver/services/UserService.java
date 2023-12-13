package com.gameserver.services;

import com.gameserver.entities.User;
import com.gameserver.entities.responses.Stats;
import com.gameserver.game.GameSession;
import com.gameserver.repositories.UserRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;

    private Map<Integer,Integer> idRankMap;
    private List<Object[/* int rank, String nickname, int rating */]> top10Ranks;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    {
        Thread ranksUpdateThread = new Thread( () -> {
            while(true) {
                updateIdRankMap();
                updateTop10Ranks();
                try { Thread.sleep(1000 * 60 * 5); } catch (InterruptedException e) { throw new RuntimeException(e); }
            }
        });
        ranksUpdateThread.start();
    }


    public boolean isExists(String login) {
        return userRepository.existsByLogin(login);
    }

    public Optional<User> getUserByLogin(String login) {
        return userRepository.findByLogin(login);
    }

    public void changeNickname(User user, String nickname) {
        user.setNickname(nickname);
        save(user);
    }

    public int getRank(User user) {
        return idRankMap.get(user.getId());
    }

    public List<Object[]> getTop10Ranks() {
        return top10Ranks;
    }

    public Stats stats(User user) { return new Stats(user, getRank(user)); }

    public void save(User user) {
        userRepository.save(user);
    }

    private void updateIdRankMap() {
        idRankMap = userRepository.ranks().stream()
                   .collect(Collectors.toMap(arr->arr[0], arr->arr[1]));
    }

    private void updateTop10Ranks() { top10Ranks = userRepository.top10ranks(); }

}

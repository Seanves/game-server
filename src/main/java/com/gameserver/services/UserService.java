package com.gameserver.services;

import com.gameserver.entities.GameResult;
import com.gameserver.entities.responses.GameResultDTO;
import com.gameserver.entities.User;
import com.gameserver.entities.responses.Response;
import com.gameserver.entities.responses.UserInfo;
import com.gameserver.repositories.GameResultRepository;
import com.gameserver.repositories.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final GameResultRepository gameResultRepository;

    private Map<Integer,Integer> cachedIdRankMap;
    private List<UserInfo> cachedTop10Ranks;

    private final Sort ranksSort;
    private long lastRanksCacheUpdate;
    private final int PAGE_SIZE = 10;
    private final long CACHE_UPDATE_FREQUENCY = 1000 * 60;

    public UserService(UserRepository userRepository, GameResultRepository gameResultRepository) {
        this.userRepository = userRepository;
        this.gameResultRepository = gameResultRepository;
        this.ranksSort = Sort.by(
                Sort.Order.desc("rating"),
                Sort.Order.desc("maxRating"),
                Sort.Order.asc("nickname")
        );
    }


    public boolean isExists(String login) { return userRepository.existsByLogin(login); }

    public boolean isExists(int id) { return userRepository.existsById(id); }

    public Optional<User> getUser(String login) {
        return userRepository.findByLogin(login);
    }

    public Optional<User> getUser(int id) { return userRepository.findById(id); }

    public UserInfo getUserInfo(User user) { return new UserInfo(user, getRank(user)); }

    public Response changeNickname(User user, String newNickname) {
        if(newNickname.length() < 5 || newNickname.length() > 20) {
            return new Response(false, "Nickname must be from 5 to 20 characters long");
        }
        user.setNickname(newNickname);
        userRepository.save(user);
        return Response.OK;
    }

    public Page<GameResultDTO> getGameResults(User user, int page) {
        Pageable pageable = PageRequest.of(page-1, PAGE_SIZE);
        Page<GameResult> results = gameResultRepository.getPageForUserId(user.getId(), pageable);
        return results.map(gr -> new GameResultDTO(gr, user));
    }

    private int getRank(User user) {
        if(System.currentTimeMillis() > lastRanksCacheUpdate + CACHE_UPDATE_FREQUENCY) {
            updateRanksCache();
        }
        return cachedIdRankMap.getOrDefault(user.getId(), 0);
    }

    public List<UserInfo> getTop10Ranks() {
        if(System.currentTimeMillis() > lastRanksCacheUpdate + CACHE_UPDATE_FREQUENCY) {
            updateRanksCache();
        }
        return cachedTop10Ranks;
    }



    public void save(User user) {
        userRepository.save(user);
    }


    private void updateRanksCache() {
        List<User> users = userRepository.findAll(ranksSort);

        List<UserInfo> top10Ranks = new ArrayList<>();
        for(int i=0; i<10 && i<users.size(); i++) {
            top10Ranks.add(new UserInfo(users.get(i), i+1));
        }

        Map<Integer,Integer> idRankMap = new HashMap<>();
        for(int i=0; i<users.size(); i++) {
            User user = users.get(i);
            idRankMap.put(user.getId(), i+1);
        }

        cachedTop10Ranks = top10Ranks;
        cachedIdRankMap = idRankMap;
        lastRanksCacheUpdate = System.currentTimeMillis();
    }

}

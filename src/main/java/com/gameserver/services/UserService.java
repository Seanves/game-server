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
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

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


    public UserInfo getUserInfo(User user) { return new UserInfo(user, getRank(user.getId())); }

    public Response changeNickname(User user, String newNickname) {
        user.setNickname(newNickname);
        userRepository.save(user);
        return Response.OK;
    }

    public Page<GameResultDTO> getGameResults(int userId, int page) {
        Pageable pageable = PageRequest.of(page-1, PAGE_SIZE);
        Page<GameResult> results = gameResultRepository.getPageForUserId(userId, pageable);
        return results.map(gr -> new GameResultDTO(gr, userId));
    }

    private int getRank(int id) {
        if(System.currentTimeMillis() > lastRanksCacheUpdate + CACHE_UPDATE_FREQUENCY) {
            updateRanksCache();
        }
        return cachedIdRankMap.getOrDefault(id, 0);
    }

    public List<UserInfo> getTop10Ranks() {
        if(System.currentTimeMillis() > lastRanksCacheUpdate + CACHE_UPDATE_FREQUENCY) {
            updateRanksCache();
        }
        return cachedTop10Ranks;
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

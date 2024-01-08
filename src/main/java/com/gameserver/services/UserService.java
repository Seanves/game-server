package com.gameserver.services;

import com.gameserver.entities.User;
import com.gameserver.entities.responses.Response;
import com.gameserver.entities.responses.UserInfo;
import com.gameserver.repositories.UserRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class UserService {

    private final UserRepository userRepository;

    private Map<Integer,Integer> cachedIdRankMap;
    private List<UserInfo> cachedTop10Ranks;

    private final Sort sort;
    private long lastCacheUpdate;
    private final long CACHE_UPDATE_FREQUENCY = 1000 * 60;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.sort = Sort.by(
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

    public Response changeNickname(User user, String nickname) {
        if(nickname.length() < 5 || nickname.length() > 20) {
            return new Response(false, "Nickname must be from 5 to 20 characters long");
        }
        user.setNickname(nickname);
        userRepository.save(user);
        return Response.OK;
    }

    private int getRank(User user) {
        if(System.currentTimeMillis() > lastCacheUpdate + CACHE_UPDATE_FREQUENCY) {
            updateRanksCache();
        }
        return cachedIdRankMap.getOrDefault(user.getId(), 0);
    }

    public List<UserInfo> getTop10Ranks() {
        if(System.currentTimeMillis() > lastCacheUpdate + CACHE_UPDATE_FREQUENCY) {
            updateRanksCache();
        }
        return cachedTop10Ranks;
    }



    public void save(User user) {
        userRepository.save(user);
    }


    private void updateRanksCache() {
        List<User> users = userRepository.findAll(sort);

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
        lastCacheUpdate = System.currentTimeMillis();
    }

}

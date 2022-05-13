package com.game.service;

import com.game.exceptions.NotValidIdException;
import com.game.exceptions.UserNotFoundException;
import com.game.controller.PlayerOrder;
import com.game.entity.Player;
import com.game.entity.Profession;
import com.game.entity.Race;
import com.game.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.support.PagedListHolder;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class PlayerService {

    @Autowired
    private PlayerRepository playerRepository;


    @Transactional
    public List<Player> findPlayers(String name, String title, Race race, Profession profession, Long after, Long before,
                                    Integer minExperience, Integer maxExperience, Integer minLevel, Integer maxLevel,
                                    Integer pageNumber, Integer pageSize, Boolean banned, PlayerOrder order) {

        List<Player> players = new ArrayList<>();

        PageRequest pageRequest = PageRequest.of(pageNumber, pageSize);

        for (Player player : playerRepository.findAll()) {
            if ((name == null || player.getName().toLowerCase().contains(name.toLowerCase()))
                    && (title == null || player.getTitle().toLowerCase().contains(title.toLowerCase()))
                    && (race == null || player.getRace().equals(race))
                    && (profession == null || player.getProfession().equals(profession))
                    && (after == null || player.getBirthday().getTime() >= after)
                    && (before == null || player.getBirthday().getTime() <= before)
                    && (minExperience == null || player.getExperience() >= minExperience)
                    && (maxExperience == null || player.getExperience() <= maxExperience)
                    && (minLevel == null || player.getLevel() >= minLevel)
                    && (maxLevel == null || player.getLevel() <= maxLevel)
                    && (banned == null || player.getBanned().equals(banned))
            ) {
                players.add(player);
            }
        }

        switch (order) {
            case ID:
                players.sort(Comparator.comparing(Player::getId));
                break;
            case NAME:
                players.sort(Comparator.comparing(Player::getName));
                break;
            case EXPERIENCE:
                players.sort(Comparator.comparing(Player::getExperience));
                break;
            case BIRTHDAY:
                players.sort(Comparator.comparing(Player::getBirthday));
                break;
        }

        PagedListHolder<Player> page = new PagedListHolder<>(players);
        page.setPageSize(pageSize);
        page.setPage(pageNumber);

        return page.getPageList();
    }

    @Transactional
    public Integer getCount(String name, String title, Race race, Profession profession, Long after, Long before,
                            Integer minExperience, Integer maxExperience, Integer minLevel, Integer maxLevel, Boolean banned) {
        int count = 0;

        for (Player player : playerRepository.findAll()) {
            if ((name == null || player.getName().toLowerCase().contains(name.toLowerCase()))
                    && (title == null || player.getTitle().toLowerCase().contains(title.toLowerCase()))
                    && (race == null || player.getRace().equals(race))
                    && (profession == null || player.getProfession().equals(profession))
                    && (after == null || player.getBirthday().getTime() >= after)
                    && (before == null || player.getBirthday().getTime() <= before)
                    && (minExperience == null || player.getExperience() >= minExperience)
                    && (maxExperience == null || player.getExperience() <= maxExperience)
                    && (minLevel == null || player.getLevel() >= minLevel)
                    && (maxLevel == null || player.getLevel() <= maxLevel)
                    && (banned == null || player.getBanned().equals(banned))
            ) {
                count++;
            }
        }
        return count;
    }

    @Transactional
    public Optional<Player> findById(Long id) {
        return playerRepository.findById(id);
    }

    @Transactional
    public Player createPlayer(Player player) {
        Integer level = (int) (Math.sqrt(2500 + 200 * player.getExperience()) - 50) / 100;
        Integer untilNextLevel = 50 * (level + 1) * (level + 2) - player.getExperience();

        player.setLevel(level);
        player.setUntilNextLevel(untilNextLevel);

        playerRepository.save(player);
        return player;
    }

    @Transactional
    public Player updatePlayer(Player player, String id) {

        long playerId = checkValidIdAndGetLongValue(id);

        Player modifiedPlayer = playerRepository.findById(playerId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь с id = " + id + " не найден"));

        if (player.getName() != null) modifiedPlayer.setName(player.getName());
        if (player.getTitle() != null) modifiedPlayer.setTitle(player.getTitle());
        if (player.getRace() != null) modifiedPlayer.setRace(player.getRace());
        if (player.getProfession() != null) modifiedPlayer.setProfession(player.getProfession());
        if (player.getBanned() != null) modifiedPlayer.setBanned(player.getBanned());

        if (player.getExperience() != null) {
            if (player.getExperience() > 10000000 || player.getExperience() < 0) {
                throw new NotValidIdException("");
            } else {
                modifiedPlayer.setExperience(player.getExperience());
                int level = (int) (Math.sqrt(2500 + 200 * player.getExperience()) - 50) / 100;
                int untilNextLevel = 50 * (level + 1) * (level + 2) - player.getExperience();
                modifiedPlayer.setLevel(level);
                modifiedPlayer.setUntilNextLevel(untilNextLevel);
            }
        }

        if (player.getBirthday() != null) {
            if (player.getBirthday().getTime() > 3253521599900L || player.getBirthday().getTime() < 946684800000L) {
                throw new NotValidIdException("");
            } else {
                modifiedPlayer.setBirthday(player.getBirthday());
            }
        }

        playerRepository.save(modifiedPlayer);
        return modifiedPlayer;
    }

    @Transactional
    public void deletePlayer(String strId) {

        long id = checkValidIdAndGetLongValue(strId);

        Optional<Player> player = playerRepository.findById(id);

        if (!player.isPresent()) {
            throw new UserNotFoundException("");
        }

        playerRepository.delete(player.get());
    }

    public Long getQuantityPlayers() {
        return playerRepository.count();
    }


    private long checkValidIdAndGetLongValue(String id) {
        long playerId;
        try {
            playerId = Long.parseLong(id);
            if (playerId <= 0) {
                throw new NotValidIdException("");
            }
        } catch (NumberFormatException e) {
            throw new NotValidIdException("");
        }
        return playerId;
    }

}

package com.game.controller;

import com.game.exceptions.NotValidIdException;
import com.game.exceptions.UserNotFoundException;
import com.game.entity.Player;
import com.game.entity.Profession;
import com.game.entity.Race;
import com.game.repository.PlayerRepository;
import com.game.service.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/rest/players")
public class PlayerController {

    private final PlayerService playerService;

    @Autowired
    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    @GetMapping()
    public List<Player> getPlayersList(@RequestParam(value = "name", required = false) String name,
                                       @RequestParam(value = "title", required = false) String title,
                                       @RequestParam(value = "race", required = false) Race race,
                                       @RequestParam(value = "profession", required = false) Profession profession,
                                       @RequestParam(value = "after", required = false) Long after,
                                       @RequestParam(value = "before", required = false) Long before,
                                       @RequestParam(value = "minExperience", required = false) Integer minExperience,
                                       @RequestParam(value = "maxExperience", required = false) Integer maxExperience,
                                       @RequestParam(value = "minLevel", required = false) Integer minLevel,
                                       @RequestParam(value = "maxLevel", required = false) Integer maxLevel,
                                       @RequestParam(value = "pageNumber", defaultValue = "0") Integer pageNumber,
                                       @RequestParam(value = "pageSize", defaultValue = "3") Integer pageSize,
                                       @RequestParam(value = "banned", required = false) Boolean banned,
                                       @RequestParam(value = "order", defaultValue = "ID") PlayerOrder order) {

        return playerService.findPlayers(name, title, race, profession, after, before,
                minExperience, maxExperience, minLevel, maxLevel, pageNumber, pageSize, banned, order);

    }

    @GetMapping("/count")
    public Integer getPlayersCount(@RequestParam(value = "name", required = false) String name,
                                   @RequestParam(value = "title", required = false) String title,
                                   @RequestParam(value = "race", required = false) Race race,
                                   @RequestParam(value = "profession", required = false) Profession profession,
                                   @RequestParam(value = "after", required = false) Long after,
                                   @RequestParam(value = "before", required = false) Long before,
                                   @RequestParam(value = "minExperience", required = false) Integer minExperience,
                                   @RequestParam(value = "maxExperience", required = false) Integer maxExperience,
                                   @RequestParam(value = "minLevel", required = false) Integer minLevel,
                                   @RequestParam(value = "maxLevel", required = false) Integer maxLevel,
                                   @RequestParam(value = "banned", required = false) Boolean banned) {
        Integer count = playerService.getCount(name, title, race, profession, after, before,
                minExperience, maxExperience, minLevel, maxLevel, banned);
        return count;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Player> getPlayer(@PathVariable("id") Long id) {
        if (id <= 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        if (id > playerService.getQuantityPlayers()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        Optional<Player> player = playerService.findById(id);
        if (!player.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok().body(player.get());
    }

    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Player> createPlayer(@RequestBody Player newPlayer) {
        if (newPlayer.getName() == null || newPlayer.getTitle() == null || newPlayer.getBirthday() == null || newPlayer.getExperience() == null ||
                newPlayer.getName().length() > 12 || newPlayer.getTitle().length() > 30 ||
                newPlayer.getExperience() > 10000000 || newPlayer.getBirthday().getTime() < 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        return ResponseEntity.ok().body(playerService.createPlayer(newPlayer));
    }

    @PostMapping("/{id}")
    public ResponseEntity<Player> updatePlayer(@RequestBody Player newPlayer, @PathVariable("id") String id) {

        if (newPlayer.getName() == null && newPlayer.getTitle() == null && newPlayer.getRace() == null &&
                newPlayer.getProfession() == null && newPlayer.getExperience() == null && newPlayer.getLevel() == null &&
                newPlayer.getBirthday() == null && newPlayer.getBanned() == null) {
            return ResponseEntity.ok().body(playerService.findById(Long.parseLong(id)).get());
        }

       try {
            return ResponseEntity.ok().body(playerService.updatePlayer(newPlayer, id));
       } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (NotValidIdException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Player> deletePlayer(@PathVariable("id") String id) {
        try {
            playerService.deletePlayer(id);
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (NotValidIdException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        return ResponseEntity.ok().build();
    }

}

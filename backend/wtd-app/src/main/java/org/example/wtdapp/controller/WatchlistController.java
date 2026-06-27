package org.example.wtdapp.controller;

import org.example.wtdapp.entity.WatchlistItem;
import org.example.wtdapp.repository.WatchlistItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/watchlist")
@CrossOrigin(origins = "*")
public class WatchlistController {

    @Autowired
    private WatchlistItemRepository watchlistItemRepository;

    @GetMapping
    public List<WatchlistItem> getAll() {
        return watchlistItemRepository.findAll();
    }

    @PostMapping
    public WatchlistItem create(@RequestBody WatchlistItem item) {
        item.setId(null);
        return watchlistItemRepository.save(item);
    }

    @PutMapping("/{id}")
    public ResponseEntity<WatchlistItem> update(@PathVariable Long id, @RequestBody WatchlistItem details) {
        Optional<WatchlistItem> existing = watchlistItemRepository.findById(id);
        if (existing.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        WatchlistItem item = existing.get();
        item.setTitle(details.getTitle());
        item.setType(details.getType());
        item.setStatus(details.getStatus());
        return ResponseEntity.ok(watchlistItemRepository.save(item));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        watchlistItemRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}

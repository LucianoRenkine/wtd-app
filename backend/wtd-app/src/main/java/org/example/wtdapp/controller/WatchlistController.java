package org.example.wtdapp.controller;

import org.example.wtdapp.entity.WatchlistItem;
import org.example.wtdapp.repository.WatchlistItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/* Watchlist compartida: una sola lista para todos los perfiles. Cada item guarda
   en `data` el JSON completo que arma el frontend. */
@RestController
@RequestMapping("/watchlist")
@CrossOrigin(origins = "*")
public class WatchlistController {

    @Autowired
    private WatchlistItemRepository watchlistItemRepository;

    @GetMapping
    public List<WatchlistItem> getAll() {
        return watchlistItemRepository.findAllByOrderByIdAsc();
    }

    @PostMapping
    public WatchlistItem create(@RequestBody WatchlistItem item) {
        item.setId(null);
        // El frontend actual no manda status ni type siempre: sin esto llegan en null y la base los rechaza.
        if (item.getStatus() == null) item.setStatus(WatchlistItem.WatchStatus.SOON);
        if (item.getType() == null) item.setType(WatchlistItem.MediaType.MOVIE);
        if (item.getType() == WatchlistItem.MediaType.SERIES && item.getSeason() == null) {
            item.setSeason(1);
        }
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
        // Un campo ausente no pisa el valor guardado (status/type son NOT NULL).
        if (details.getType() != null) item.setType(details.getType());
        if (details.getStatus() != null) item.setStatus(details.getStatus());
        item.setReleaseDate(details.getReleaseDate());
        item.setSeason(details.getSeason());
        item.setYear(details.getYear());
        item.setData(details.getData());
        return ResponseEntity.ok(watchlistItemRepository.save(item));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        watchlistItemRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}

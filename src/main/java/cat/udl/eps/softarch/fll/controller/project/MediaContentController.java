package cat.udl.eps.softarch.fll.controller.project; // Assegura't que el paquet sigui correcte

import cat.udl.eps.softarch.fll.dto.MediaBatchDTO;
import cat.udl.eps.softarch.fll.service.projectroom.MediaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/editions/{editionId}/media")
@CrossOrigin(origins = "http://localhost:3000")

public class MediaContentController {
    @Autowired private MediaService mediaService;

    @PostMapping("/batch")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> batchUpload(
            @PathVariable Long editionId, 
            @RequestBody List<MediaBatchDTO> batch) {
        
        mediaService.createMediaBatch(editionId, batch);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
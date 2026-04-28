package cat.udl.eps.softarch.fll.controller.project;

@RestController
@RequestMapping("/editions/{editionId}/media")
public class MediaContentController {
    @Autowired private MediaService mediaService;

    @PostMapping("/batch")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> batchUpload(@PathVariable Long editionId, @RequestBody List<MediaBatchDTO> batch) {
        mediaService.createMediaBatch(editionId, batch);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}

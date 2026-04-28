package cat.udl.eps.softarch.fll.service.projectroom;

@Service
public class MediaService {
    @Autowired private MediaContentRepository mediaRepository;
    @Autowired private EditionRepository editionRepository;

    @Transactional
    public void createMediaBatch(Long editionId, List<MediaBatchDTO> batch) {
        Edition edition = editionRepository.findById(editionId).orElseThrow();
        for (MediaBatchDTO item : batch) {
            MediaContent media = MediaContent.create(item.url(), item.type());
            media.setEdition(edition);
            mediaRepository.save(media);
        }
    }
}
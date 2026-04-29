package cat.udl.eps.softarch.fll.service.projectroom;

import org.springframework.stereotype.Service;
import cat.udl.eps.softarch.fll.dto.MediaBatchDTO;
import cat.udl.eps.softarch.fll.domain.project.MediaContent;
import cat.udl.eps.softarch.fll.repository.project.MediaContentRepository;
import cat.udl.eps.softarch.fll.repository.edition.EditionRepository;
import cat.udl.eps.softarch.fll.domain.edition.Edition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class MediaService {
    @Autowired private MediaContentRepository mediaRepository;
    @Autowired private EditionRepository editionRepository;

    @Transactional
    public void createMediaBatch(Long editionId, List<MediaBatchDTO> batch) {
		List<Edition> allEditions = editionRepository.findAll();
		System.out.println("Edicions disponibles a la BD: " + allEditions.size());
		for(Edition e : allEditions) {
			System.out.println("ID disponible: " + e.getId());
		}	
		
		Edition edition = editionRepository.findById(editionId)
        		.orElseThrow(() -> new RuntimeException("No existeix cap edició amb ID: " + editionId));        
			for (MediaBatchDTO item : batch) {
            MediaContent media = MediaContent.create(item.url(), item.type());
            media.setEdition(edition);
            mediaRepository.save(media);
        }
    }
}
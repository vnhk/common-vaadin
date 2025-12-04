package com.bervan.lowcode;

import com.bervan.common.search.SearchService;
import com.bervan.common.service.BaseService;
import com.bervan.history.model.BaseRepository;
import com.bervan.logging.JsonLogger;
import com.bervan.lowcode.generator.LowCodeGenerator;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class LowCodeClassService extends BaseService<UUID, LowCodeClass> {
    private final JsonLogger log = JsonLogger.getLogger(getClass(), "common");
    private final LowCodeGenerator lowCodeGenerator;
    private final LowCodeClassDetailsService lowCodeClassDetailsService;

    protected LowCodeClassService(BaseRepository<LowCodeClass, UUID> repository, SearchService searchService, LowCodeGenerator lowCodeGenerator, LowCodeClassDetailsService lowCodeClassDetailsService) {
        super(repository, searchService);
        this.lowCodeGenerator = lowCodeGenerator;
        this.lowCodeClassDetailsService = lowCodeClassDetailsService;
    }

    public void generateCode(LowCodeClass obj) {
        try {
            obj = repository.findById(obj.getId()).get();
            lowCodeGenerator.generate(obj);
            obj.setStatus("DONE");
        } catch (Exception e) {
            log.error("Error generating low code class!", e);
            obj.setStatus("FAILED");
        }

        this.save(obj);
    }

    public void loadDetails(LowCodeClass lowCodeClass) {
        List<LowCodeClassDetails> load = lowCodeClassDetailsService.loadByParentId(lowCodeClass);
        lowCodeClass.setLowCodeClassDetails(load);
    }
}

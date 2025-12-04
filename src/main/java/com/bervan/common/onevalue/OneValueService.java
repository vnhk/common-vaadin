package com.bervan.common.onevalue;

import com.bervan.common.service.AuthService;
import com.bervan.common.service.BaseOneValueService;
import com.bervan.ieentities.ExcelIEEntity;
import com.bervan.logging.JsonLogger;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class OneValueService implements BaseOneValueService<OneValue> {
    private final JsonLogger log = JsonLogger.getLogger(getClass(), "common");
    private final OneValueRepository repository;
    private final OneValueHistoryRepository historyRepository;

    public OneValueService(OneValueRepository repository, OneValueHistoryRepository historyRepository) {
        this.repository = repository;
        this.historyRepository = historyRepository;
    }

    public void save(OneValue item) {
        repository.save(item);
    }

    @PostFilter("(T(com.bervan.common.service.AuthService).hasAccess(filterObject.owners))")
    public List<OneValue> loadByKey(String name) {
        return repository.findByNameAndOwnersId(name, AuthService.getLoggedUserId());
    }

    @PostFilter("(T(com.bervan.common.service.AuthService).hasAccess(filterObject.owners))")
    public List<OneValue> load() {
        return repository.findAll();
    }

    @PostFilter("(T(com.bervan.common.service.AuthService).hasAccess(filterObject.owners))")
    public List<HistoryOneValue> loadHistory() {
        return historyRepository.findAll();
    }

    public void saveIfValid(List<? extends ExcelIEEntity> objects) {
        //prevent saving objects with the same names that should be unique
        List<OneValue> all = load();
        Set<String> names = all.stream().map(OneValue::getName).collect(Collectors.toSet());
        List<? extends ExcelIEEntity> list = objects.stream().filter(e -> e instanceof OneValue)
                .filter(e -> !names.contains(((OneValue) e).getName()))
                .toList();

        log.debug("Filtered One Values to be imported: " + list.size());
        for (ExcelIEEntity excelIEEntity : list) {
            repository.save(((OneValue) excelIEEntity));
        }
    }
}

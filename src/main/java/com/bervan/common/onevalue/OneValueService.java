package com.bervan.common.onevalue;

import com.bervan.common.service.AuthService;
import com.bervan.common.service.BaseOneValueService;
import com.bervan.core.model.BervanLogger;
import com.bervan.ieentities.ExcelIEEntity;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class OneValueService implements BaseOneValueService<OneValue> {
    private final OneValueRepository repository;
    private final OneValueHistoryRepository historyRepository;
    private final BervanLogger logger;

    public OneValueService(OneValueRepository repository, OneValueHistoryRepository historyRepository, BervanLogger logger) {
        this.repository = repository;
        this.historyRepository = historyRepository;
        this.logger = logger;
    }

    public void save(OneValue item) {
        repository.save(item);
    }

    public Optional<OneValue> loadByKey(String name) {
        return repository.findByNameAndOwnerId(name, AuthService.getLoggedUserId());
    }

    @PostFilter("filterObject.owner != null && filterObject.owner.getId().equals(T(com.bervan.common.service.AuthService).getLoggedUserId())")
    public List<OneValue> load() {
        return repository.findAll();
    }

    @PostFilter("filterObject.owner != null && filterObject.owner.getId().equals(T(com.bervan.common.service.AuthService).getLoggedUserId())")
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

        logger.debug("Filtered One Values to be imported: " + list.size());
        for (ExcelIEEntity excelIEEntity : list) {
            repository.save(((OneValue) excelIEEntity));
        }
    }
}

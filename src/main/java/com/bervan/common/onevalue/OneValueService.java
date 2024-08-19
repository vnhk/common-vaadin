package com.bervan.common.onevalue;

import com.bervan.common.service.BaseOneValueService;
import com.bervan.ieentities.ExcelIEEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class OneValueService implements BaseOneValueService<OneValue> {
    private final OneValueRepository repository;
    private final OneValueHistoryRepository historyRepository;

    public OneValueService(OneValueRepository repository, OneValueHistoryRepository historyRepository) {
        this.repository = repository;
        this.historyRepository = historyRepository;
    }

    public void save(OneValue item) {
        repository.save(item);
    }

    public Optional<OneValue> loadByKey(String name) {
        return repository.findByName(name);
    }

    public List<OneValue> load() {
        return repository.findAll();
    }

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
        for (ExcelIEEntity excelIEEntity : list) {
            repository.save(((OneValue) excelIEEntity));
        }
    }
}

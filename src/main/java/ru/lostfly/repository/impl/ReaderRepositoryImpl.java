package ru.lostfly.repository.impl;

import ru.lostfly.domain.reader.Reader;
import ru.lostfly.repository.ReaderRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReaderRepositoryImpl implements ReaderRepository {
    private final Map<String, Reader> readers = new HashMap<>();

    public void save(Reader reader) {
        readers.put(reader.getId(), reader);
    }

    public Reader findById(String id) {
        return readers.get(id);
    }

    public List<Reader> findAll() {
        return new ArrayList<>(readers.values());
    }

    public int getTotalReaders() {
        return readers.size();
    }
}

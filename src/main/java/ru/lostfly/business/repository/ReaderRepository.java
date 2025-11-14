package ru.lostfly.business.repository;

import ru.lostfly.business.domain.reader.Reader;

import java.util.List;

public interface ReaderRepository {

    /**
     * Сохранить читателя в хранилище
     */
    void save(Reader reader);

    /**
     * Найти читателя по ID
     * @return читатель или null, если не найден
     */
    Reader findById(String id);

    /**
     * Получить всех читателей
     */
    List<Reader> findAll();

    /**
     * Получить общее количество читателей
     */
    int getTotalReaders();
}

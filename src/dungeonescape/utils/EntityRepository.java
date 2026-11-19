package dungeonescape.utils;

import dungeonescape.entities.Entity;
import dungeonescape.map.Position;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Generic repository container demonstrating Parametric Polymorphism (Generics with upper bound).
 * Provides type-safe filtering, querying, and spatial lookups for any Entity subtype.
 * 
 * @param <T> Any subclass of Entity.
 */
public class EntityRepository<T extends Entity> implements Serializable {
    private static final long serialVersionUID = 1L;

    private final List<T> entities;

    public EntityRepository() {
        this.entities = new ArrayList<>();
    }

    public void add(T entity) {
        if (entity != null && !entities.contains(entity)) {
            entities.add(entity);
        }
    }

    public boolean remove(T entity) {
        return entities.remove(entity);
    }

    public void clear() {
        entities.clear();
    }

    public int size() {
        return entities.size();
    }

    public boolean isEmpty() {
        return entities.isEmpty();
    }

    /**
     * Finds the first entity matching the given coordinate.
     */
    public Optional<T> findAt(Position position) {
        if (position == null) return Optional.empty();
        return entities.stream()
                .filter(e -> e.getPosition().equals(position))
                .findFirst();
    }

    /**
     * Finds the first entity with the specified name (case-insensitive).
     */
    public Optional<T> findByName(String name) {
        if (name == null) return Optional.empty();
        return entities.stream()
                .filter(e -> e.getName().equalsIgnoreCase(name))
                .findFirst();
    }

    /**
     * Filters entities matching the given predicate.
     * Demonstrates higher-order functional programming and generics.
     */
    public List<T> filter(Predicate<? super T> predicate) {
        return entities.stream()
                .filter(predicate)
                .collect(Collectors.toList());
    }

    /**
     * Returns an unmodifiable view of stored entities, protecting internal list encapsulation.
     */
    public List<T> getAll() {
        return Collections.unmodifiableList(entities);
    }
}

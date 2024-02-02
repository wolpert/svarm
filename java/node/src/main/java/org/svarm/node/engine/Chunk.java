package org.svarm.node.engine;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * A chunk represents hashes, and each hash can have the set of rows. It does not do any
 * unique checks on data except for the hashes themselves. It is the responsibility of the
 * caller to ensure that the data is unique.
 *
 * @param <T> the type parameter
 */
public class Chunk<T> implements Serializable {

  /**
   * The sets of rows in this chunk, aligned by the hash value.
   */
  private final Map<Integer, List<T>> sets; // hash -> list of rows

  /**
   * The total number of rows in this chunk across all sets.
   */
  private int rows;

  /**
   * Instantiates a new Chunk.
   */
  public Chunk() {
    this.sets = new HashMap<>();
    this.rows = 0;
  }

  /**
   * Add.
   *
   * @param hash the hash
   * @param rows the rows
   */
  public void add(final int hash, final List<T> rows) {
    final List<T> existing = sets.computeIfAbsent(hash, k -> new LinkedList<>());
    existing.addAll(rows);
    this.rows += rows.size();
  }

  /**
   * Add.
   *
   * @param hash the hash
   * @param row  the row
   */
  public void add(final int hash, final T row) {
    final List<T> existing = sets.computeIfAbsent(hash, k -> new LinkedList<>());
    existing.add(row);
    this.rows++;
  }

  /**
   * Gets rows.
   *
   * @return the rows
   */
  public int getRows() {
    return rows;
  }

  /**
   * Hashes set.
   *
   * @return the set
   */
  public Set<Integer> hashes() {
    return sets.keySet();
  }

  /**
   * Get list.
   *
   * @param hash the hash
   * @return the list
   */
  public Optional<List<T>> get(final int hash) {
    return Optional.ofNullable(sets.get(hash));
  }

  /**
   * Is empty boolean.
   *
   * @return the boolean
   */
  public boolean isEmpty() {
    return sets.isEmpty();
  }

  /**
   * Remove list.
   *
   * @param hash the hash
   * @return the list
   */
  public List<T> remove(final int hash) {
    final List<T> removed = sets.remove(hash);
    if (removed != null) {
      rows -= removed.size();
    }
    return removed;
  }

  /**
   * Clear.
   */
  public void clear() {
    sets.clear();
    rows = 0;
  }

  /**
   * All list.
   *
   * @return the list
   */
  public List<T> all() {
    return sets.values().stream().flatMap(List::stream).toList();
  }

}

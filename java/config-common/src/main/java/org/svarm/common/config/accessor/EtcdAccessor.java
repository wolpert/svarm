package org.svarm.common.config.accessor;

import io.etcd.jetcd.Watch;
import java.util.Map;
import java.util.Optional;

/**
 * The interface Etcd accessor.
 */
public interface EtcdAccessor {
  /**
   * Put.
   *
   * @param namespace the namespace
   * @param key       the key
   * @param value     the value
   */
  void put(String namespace, String key, String value);

  /**
   * Put all.
   *
   * @param namespace the namespace
   * @param map       the map
   */
  void putAll(String namespace, Map<String, String> map);

  /**
   * Delete.
   *
   * @param namespace the namespace
   * @param key       the key
   */
  void delete(String namespace, String key);

  /**
   * Watch watch . watcher.
   *
   * @param namespace the namespace
   * @param key       the key
   * @param listener  the listener
   * @return the watch . watcher
   */
  Watch.Watcher watch(String namespace,
                      String key,
                      Watch.Listener listener);

  /**
   * Get optional.
   *
   * @param namespace the namespace
   * @param key       the key
   * @return the optional
   */
  Optional<String> get(String namespace, String key);

  /**
   * Gets all.
   *
   * @param namespace the namespace
   * @param key       the key
   * @return the all
   */
  Map<String, String> getAll(String namespace, String key);
}

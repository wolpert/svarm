package org.svarm.node.engine.impl.v1singleentry;


import java.util.List;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindPojo;
import org.jdbi.v3.sqlobject.statement.SqlBatch;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.jdbi.v3.sqlobject.transaction.Transactional;

/**
 * The dao for the V1 Single Entry.
 */
public interface V1RowDao extends Transactional<V1RowDao> {

  /**
   * Insert.
   *
   * @param instance the instance
   */
  @SqlUpdate("insert into TENANT_DATA (ID, C_COL, HASH, TIMESTAMP, C_DATA_TYPE, C_DATA, EXPIRY) "
      + "values (:id, :cCol, :hash, :timestamp, :cDataType, :cData, :expiry)")
  void insert(@BindPojo final V1Row instance);

  /**
   * Insert.
   *
   * @param instances the instances
   */
  @SqlUpdate("insert into TENANT_DATA (ID, C_COL, HASH, TIMESTAMP, C_DATA_TYPE, C_DATA, EXPIRY) "
      + "values (:id, :cCol, :hash, :timestamp, :cDataType, :cData, :expiry)")
  void insert(@BindPojo final List<V1Row> instances);

  /**
   * Delete all rows for the entry.
   *
   * @param id the entity to delete
   * @return the count
   */
  @SqlUpdate("delete from TENANT_DATA where ID = :id")
  int delete(@Bind("id") String id);

  /**
   * Read list.
   *
   * @param hash the hash
   * @return the list
   */
  @SqlQuery("select * from TENANT_DATA where hash = :hash")
  List<V1Row> read(@Bind("hash") Integer hash);

  /**
   * Read list.
   *
   * @param hashLow  the hash low
   * @param hashHigh the hash high
   * @return the list
   */
  @SqlQuery("select * from TENANT_DATA where hash between :hashLow and :hashHigh order by hash")
  List<V1Row> read(@Bind("hashLow") Integer hashLow, @Bind("hashHigh") Integer hashHigh);

  /**
   * Read list.
   *
   * @param id the entry id.
   * @return the list
   */
  @SqlQuery("select * from TENANT_DATA where id = :id")
  List<V1Row> readEntry(@Bind("id") String id);

  /**
   * Read keys for the entry.
   *
   * @param id the entry id.
   * @return the list of keys
   */
  @SqlQuery("select C_COL from TENANT_DATA where :id = id")
  List<String> keys(@Bind("id") String id);

  // ---- BATCH ----

  /**
   * Batch insert.
   *
   * @param instances the instances
   */
  @SqlBatch("insert into TENANT_DATA (ID, C_COL, HASH, C_DATA_TYPE, C_DATA, TIMESTAMP, EXPIRY) "
      + "values (:id, :cCol, :hash, :cDataType, :cData, :timestamp, :expiry)")
  void batchInsert(@BindPojo List<V1Row> instances);

  /**
   * Batch update.
   *
   * @param instances the instances
   */
  @SqlBatch("update TENANT_DATA set C_DATA_TYPE = :cDataType, C_DATA = :cData, EXPIRY = :expiry, "
      + "TIMESTAMP = :timestamp where ID = :id and C_COL = :cCol")
  void batchUpdate(@BindPojo List<V1Row> instances);


  /**
   * Batch delete keys.
   *
   * @param id   the id
   * @param keys the keys
   */
  @SqlBatch("delete from TENANT_DATA where ID = :id and C_COL = :cCol")
  void batchDeleteKeys(@Bind("id") String id, @Bind("cCol") List<String> keys);

}

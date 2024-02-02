package org.svarm.node.engine.impl.v1singleentry;


import java.util.List;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindPojo;
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
  @SqlUpdate("insert into TENANT_DATA (ID, C_COL, HASH, TIMESTAMP,C_DATA_TYPE,C_DATA) values (:id, :cCol, :hash, :timestamp, :cDataType, :cData)")
  void insert(@BindPojo final V1Row instance);

  /**
   * Insert.
   *
   * @param instances the instances
   */
  @SqlUpdate("insert into TENANT_DATA (ID, C_COL, HASH, TIMESTAMP,C_DATA_TYPE,C_DATA) values (:id, :cCol, :hash, :timestamp, :cDataType, :cData)")
  void insert(@BindPojo final List<V1Row> instances);

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

}

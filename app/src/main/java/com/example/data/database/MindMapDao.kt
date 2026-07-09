package com.example.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MindMapDao {
    @Query("SELECT * FROM mind_maps ORDER BY updatedAt DESC")
    fun getAllMindMapsFlow(): Flow<List<MindMapEntity>>

    @Query("SELECT * FROM mind_maps ORDER BY updatedAt DESC")
    suspend fun getAllMindMaps(): List<MindMapEntity>

    @Query("SELECT * FROM mind_maps WHERE id = :mapId")
    suspend fun getMindMapById(mapId: Long): MindMapEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMindMap(mindMap: MindMapEntity): Long

    @Update
    suspend fun updateMindMap(mindMap: MindMapEntity)

    @Query("DELETE FROM mind_maps WHERE id = :mapId")
    suspend fun deleteMindMap(mapId: Long)

    // Nodes operations
    @Query("SELECT * FROM mind_map_nodes WHERE mapId = :mapId ORDER BY createdAt ASC")
    fun getNodesForMapFlow(mapId: Long): Flow<List<MindMapNodeEntity>>

    @Query("SELECT * FROM mind_map_nodes WHERE mapId = :mapId ORDER BY createdAt ASC")
    suspend fun getNodesForMap(mapId: Long): List<MindMapNodeEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNodes(nodes: List<MindMapNodeEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNode(node: MindMapNodeEntity)

    @Update
    suspend fun updateNode(node: MindMapNodeEntity)

    @Query("DELETE FROM mind_map_nodes WHERE id = :nodeId")
    suspend fun deleteNode(nodeId: String)

    @Query("DELETE FROM mind_map_nodes WHERE mapId = :mapId")
    suspend fun deleteAllNodesForMap(mapId: Long)
}

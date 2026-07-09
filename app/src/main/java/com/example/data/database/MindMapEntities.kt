package com.example.data.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "mind_maps")
data class MindMapEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val layoutType: String = "ORGANIC" // ORGANIC, ORTHOGONAL, TREE
)

@Entity(
    tableName = "mind_map_nodes",
    foreignKeys = [
        ForeignKey(
            entity = MindMapEntity::class,
            parentColumns = ["id"],
            childColumns = ["mapId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["mapId"]), Index(value = ["parentId"])]
)
data class MindMapNodeEntity(
    @PrimaryKey val id: String, // UUID
    val mapId: Long,
    val parentId: String?, // Null for root node
    val text: String,
    val colorHex: String, // hex representation like #6750A4
    val isCompleted: Boolean = false,
    val isCollapsed: Boolean = false,
    val offsetX: Float = 0f,
    val offsetY: Float = 0f,
    val createdAt: Long = System.currentTimeMillis()
)

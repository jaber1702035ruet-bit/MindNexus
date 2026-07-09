package com.example.data.repository

import com.example.data.database.MindMapDao
import com.example.data.database.MindMapEntity
import com.example.data.database.MindMapNodeEntity
import com.example.data.model.LayoutType
import com.example.data.model.MindMap
import com.example.data.model.MindMapNode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class MindMapRepository(private val dao: MindMapDao) {

    fun getAllMindMapsFlow(): Flow<List<MindMap>> {
        return dao.getAllMindMapsFlow().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun getAllMindMaps(): List<MindMap> {
        return dao.getAllMindMaps().map { it.toDomain() }
    }

    fun getNodesForMapFlow(mapId: Long): Flow<List<MindMapNode>> {
        return dao.getNodesForMapFlow(mapId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun getNodesForMap(mapId: Long): List<MindMapNode> {
        return dao.getNodesForMap(mapId).map { it.toDomain() }
    }

    suspend fun insertMindMap(mindMap: MindMap): Long {
        val entity = MindMapEntity(
            id = mindMap.id,
            title = mindMap.title,
            description = mindMap.description,
            createdAt = mindMap.createdAt,
            updatedAt = mindMap.updatedAt,
            layoutType = mindMap.layoutType.name
        )
        return dao.insertMindMap(entity)
    }

    suspend fun updateMindMap(mindMap: MindMap) {
        val entity = MindMapEntity(
            id = mindMap.id,
            title = mindMap.title,
            description = mindMap.description,
            createdAt = mindMap.createdAt,
            updatedAt = System.currentTimeMillis(),
            layoutType = mindMap.layoutType.name
        )
        dao.updateMindMap(entity)
    }

    suspend fun deleteMindMap(mapId: Long) {
        dao.deleteMindMap(mapId)
    }

    suspend fun saveNode(node: MindMapNode) {
        dao.insertNode(node.toEntity())
    }

    suspend fun saveNodes(nodes: List<MindMapNode>) {
        dao.insertNodes(nodes.map { it.toEntity() })
    }

    suspend fun updateNode(node: MindMapNode) {
        dao.updateNode(node.toEntity())
    }

    suspend fun deleteNode(nodeId: String) {
        dao.deleteNode(nodeId)
    }

    suspend fun prepopulateDefaultDataIfEmpty() {
        val maps = dao.getAllMindMaps()
        if (maps.isEmpty()) {
            // Create default Roadmap mind map
            val roadmapMapId = dao.insertMindMap(
                MindMapEntity(
                    title = "Q4 Product Roadmap",
                    description = "Coggle-style logic flow with 42 nodes synced across 5 devices.",
                    layoutType = "ORGANIC"
                )
            )

            // Nodes for Roadmap
            val rootId = UUID.randomUUID().toString()
            val nodes = mutableListOf<MindMapNodeEntity>()

            // Root Node
            nodes.add(
                MindMapNodeEntity(
                    id = rootId,
                    mapId = roadmapMapId,
                    parentId = null,
                    text = "Q4 Product Roadmap",
                    colorHex = "#6750A4", // primary purple
                    offsetX = 0f,
                    offsetY = 0f
                )
            )

            // Main Branches
            val featuresBranchId = UUID.randomUUID().toString()
            nodes.add(
                MindMapNodeEntity(
                    id = featuresBranchId,
                    mapId = roadmapMapId,
                    parentId = rootId,
                    text = "Features & UX",
                    colorHex = "#0284C7", // sky blue
                    offsetX = -220f,
                    offsetY = -120f
                )
            )

            val techBranchId = UUID.randomUUID().toString()
            nodes.add(
                MindMapNodeEntity(
                    id = techBranchId,
                    mapId = roadmapMapId,
                    parentId = rootId,
                    text = "Tech & Architecture",
                    colorHex = "#16A34A", // green
                    offsetX = 220f,
                    offsetY = -120f
                )
            )

            val marketingBranchId = UUID.randomUUID().toString()
            nodes.add(
                MindMapNodeEntity(
                    id = marketingBranchId,
                    mapId = roadmapMapId,
                    parentId = rootId,
                    text = "Marketing & Launch",
                    colorHex = "#EA580C", // orange
                    offsetX = -180f,
                    offsetY = 160f
                )
            )

            val designBranchId = UUID.randomUUID().toString()
            nodes.add(
                MindMapNodeEntity(
                    id = designBranchId,
                    mapId = roadmapMapId,
                    parentId = rootId,
                    text = "Design System",
                    colorHex = "#DB2777", // pink/rose
                    offsetX = 180f,
                    offsetY = 160f
                )
            )

            // Features branches
            nodes.add(
                MindMapNodeEntity(
                    id = UUID.randomUUID().toString(),
                    mapId = roadmapMapId,
                    parentId = featuresBranchId,
                    text = "Coggle style curved lines",
                    colorHex = "#38BDF8",
                    offsetX = -380f,
                    offsetY = -220f
                )
            )
            nodes.add(
                MindMapNodeEntity(
                    id = UUID.randomUUID().toString(),
                    mapId = roadmapMapId,
                    parentId = featuresBranchId,
                    text = "Real-time cursor sync",
                    colorHex = "#38BDF8",
                    offsetX = -400f,
                    offsetY = -140f
                )
            )
            nodes.add(
                MindMapNodeEntity(
                    id = UUID.randomUUID().toString(),
                    mapId = roadmapMapId,
                    parentId = featuresBranchId,
                    text = "AI Assist (Auto-expand)",
                    colorHex = "#38BDF8",
                    offsetX = -380f,
                    offsetY = -60f,
                    isCompleted = true
                )
            )

            // Tech branches
            nodes.add(
                MindMapNodeEntity(
                    id = UUID.randomUUID().toString(),
                    mapId = roadmapMapId,
                    parentId = techBranchId,
                    text = "Jetpack Compose UI",
                    colorHex = "#4ADE80",
                    offsetX = 380f,
                    offsetY = -220f,
                    isCompleted = true
                )
            )
            nodes.add(
                MindMapNodeEntity(
                    id = UUID.randomUUID().toString(),
                    mapId = roadmapMapId,
                    parentId = techBranchId,
                    text = "Room SQLite Database",
                    colorHex = "#4ADE80",
                    offsetX = 400f,
                    offsetY = -140f,
                    isCompleted = true
                )
            )
            nodes.add(
                MindMapNodeEntity(
                    id = UUID.randomUUID().toString(),
                    mapId = roadmapMapId,
                    parentId = techBranchId,
                    text = "Direct Gemini REST API",
                    colorHex = "#4ADE80",
                    offsetX = 380f,
                    offsetY = -60f
                )
            )

            // Marketing branches
            nodes.add(
                MindMapNodeEntity(
                    id = UUID.randomUUID().toString(),
                    mapId = roadmapMapId,
                    parentId = marketingBranchId,
                    text = "SEO Blog Outreach",
                    colorHex = "#F97316",
                    offsetX = -340f,
                    offsetY = 220f
                )
            )
            nodes.add(
                MindMapNodeEntity(
                    id = UUID.randomUUID().toString(),
                    mapId = roadmapMapId,
                    parentId = marketingBranchId,
                    text = "Product Hunt Hunted",
                    colorHex = "#F97316",
                    offsetX = -220f,
                    offsetY = 260f
                )
            )

            // Design branches
            nodes.add(
                MindMapNodeEntity(
                    id = UUID.randomUUID().toString(),
                    mapId = roadmapMapId,
                    parentId = designBranchId,
                    text = "Material 3 Palette",
                    colorHex = "#F472B6",
                    offsetX = 220f,
                    offsetY = 260f,
                    isCompleted = true
                )
            )
            nodes.add(
                MindMapNodeEntity(
                    id = UUID.randomUUID().toString(),
                    mapId = roadmapMapId,
                    parentId = designBranchId,
                    text = "Sleek Custom Layout",
                    colorHex = "#F472B6",
                    offsetX = 340f,
                    offsetY = 220f,
                    isCompleted = true
                )
            )

            dao.insertNodes(nodes)

            // Create secondary brainstorm mind map
            val brainstormMapId = dao.insertMindMap(
                MindMapEntity(
                    title = "User Personas Study",
                    description = "Comprehensive user study and demographic flow maps for Nexus user personas.",
                    layoutType = "TREE"
                )
            )

            val bRootId = UUID.randomUUID().toString()
            dao.insertNodes(
                listOf(
                    MindMapNodeEntity(
                        id = bRootId,
                        mapId = brainstormMapId,
                        parentId = null,
                        text = "User Personas Study",
                        colorHex = "#6750A4",
                        offsetX = 0f,
                        offsetY = 0f
                    ),
                    MindMapNodeEntity(
                        id = UUID.randomUUID().toString(),
                        mapId = brainstormMapId,
                        parentId = bRootId,
                        text = "Developer Dave (Heavy AI)",
                        colorHex = "#0284C7",
                        offsetX = 180f,
                        offsetY = -100f
                    ),
                    MindMapNodeEntity(
                        id = UUID.randomUUID().toString(),
                        mapId = brainstormMapId,
                        parentId = bRootId,
                        text = "Designer Diana (Curved Links)",
                        colorHex = "#16A34A",
                        offsetX = 180f,
                        offsetY = 0f
                    ),
                    MindMapNodeEntity(
                        id = UUID.randomUUID().toString(),
                        mapId = brainstormMapId,
                        parentId = bRootId,
                        text = "Manager Mike (Orthogonal grid)",
                        colorHex = "#EA580C",
                        offsetX = 180f,
                        offsetY = 100f
                    )
                )
            )
        }
    }

    // Helper conversion extensions
    private fun MindMapEntity.toDomain() = MindMap(
        id = id,
        title = title,
        description = description,
        createdAt = createdAt,
        updatedAt = updatedAt,
        layoutType = try { LayoutType.valueOf(layoutType) } catch (e: Exception) { LayoutType.ORGANIC }
    )

    private fun MindMapNodeEntity.toDomain() = MindMapNode(
        id = id,
        mapId = mapId,
        parentId = parentId,
        text = text,
        colorHex = colorHex,
        isCompleted = isCompleted,
        isCollapsed = isCollapsed,
        offsetX = offsetX,
        offsetY = offsetY,
        createdAt = createdAt
    )

    private fun MindMapNode.toEntity() = MindMapNodeEntity(
        id = id,
        mapId = mapId,
        parentId = parentId,
        text = text,
        colorHex = colorHex,
        isCompleted = isCompleted,
        isCollapsed = isCollapsed,
        offsetX = offsetX,
        offsetY = offsetY,
        createdAt = createdAt
    )
}

package com.example.data.model

data class MindMap(
    val id: Long = 0,
    val title: String,
    val description: String,
    val createdAt: Long,
    val updatedAt: Long,
    val layoutType: LayoutType = LayoutType.ORGANIC
)

enum class LayoutType {
    ORGANIC,    // Coggle style curved links, radial layout
    TREE,       // Left-to-right hierarchy layout
    ORTHOGONAL  // EdrawMind style right-angle links, grid layout
}

data class MindMapNode(
    val id: String,
    val mapId: Long,
    val parentId: String?,
    val text: String,
    val colorHex: String,
    val isCompleted: Boolean = false,
    val isCollapsed: Boolean = false,
    val offsetX: Float = 0f,
    val offsetY: Float = 0f,
    val createdAt: Long = System.currentTimeMillis()
)

data class Collaborator(
    val id: String,
    val name: String,
    val colorHex: String,
    val cursorX: Float,
    val cursorY: Float,
    val activeNodeId: String?,
    val status: String // "Editing...", "Idle", "Moving"
)

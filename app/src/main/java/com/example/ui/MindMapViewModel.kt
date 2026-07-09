package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.GeminiApiClient
import com.example.data.database.MindMapDatabase
import com.example.data.model.Collaborator
import com.example.data.model.LayoutType
import com.example.data.model.MindMap
import com.example.data.model.MindMapNode
import com.example.data.repository.MindMapRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.random.Random

class MindMapViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: MindMapRepository
    
    // UI state flows
    val mindMaps = MutableStateFlow<List<MindMap>>(emptyList())
    val selectedMap = MutableStateFlow<MindMap?>(null)
    val nodes = MutableStateFlow<List<MindMapNode>>(emptyList())
    val selectedNode = MutableStateFlow<MindMapNode?>(null)
    
    // Status states
    val isAiExpanding = MutableStateFlow(false)
    val syncStatus = MutableStateFlow("Real-time sync active")
    val collabSimulationActive = MutableStateFlow(true)
    
    // Custom user identity
    val currentUserName = MutableStateFlow("Me")
    val currentRoomCode = MutableStateFlow("NEXUS-42")

    // Collaborators list
    private val _collaborators = MutableStateFlow<List<Collaborator>>(emptyList())
    val collaborators = _collaborators.asStateFlow()

    // Activity feed/Notifications log
    private val _activities = MutableStateFlow<List<String>>(emptyList())
    val activities = _activities.asStateFlow()

    private var dbFlowJob: Job? = null
    private var simulationJob: Job? = null

    init {
        val database = MindMapDatabase.getDatabase(application)
        repository = MindMapRepository(database.mindMapDao())
        
        viewModelScope.launch {
            repository.prepopulateDefaultDataIfEmpty()
            loadAllMaps()
        }
        
        // Start live collaborator simulation
        startCollaboratorSimulation()
    }

    private suspend fun loadAllMaps() {
        repository.getAllMindMapsFlow().collect { maps ->
            mindMaps.value = maps
            if (selectedMap.value == null && maps.isNotEmpty()) {
                selectMap(maps.first())
            }
        }
    }

    fun selectMap(map: MindMap) {
        selectedMap.value = map
        selectedNode.value = null
        
        // Cancel old database observation job
        dbFlowJob?.cancel()
        
        // Observe nodes for the newly selected map
        dbFlowJob = viewModelScope.launch {
            repository.getNodesForMapFlow(map.id).collect { updatedNodes ->
                nodes.value = updatedNodes
                // Re-sync selected node if it changed
                selectedNode.value = updatedNodes.find { it.id == selectedNode.value?.id }
            }
        }
        
        addActivity("Switched to canvas: ${map.title}")
        
        // Re-initialize mock collaborators for this map
        initializeMockCollaborators()
    }

    private fun initializeMockCollaborators() {
        _collaborators.value = listOf(
            Collaborator(
                id = "c1",
                name = "Sarah Jenkins",
                colorHex = "#3B82F6", // blue
                cursorX = Random.nextInt(-300, 300).toFloat(),
                cursorY = Random.nextInt(-200, 200).toFloat(),
                activeNodeId = null,
                status = "Idle"
            ),
            Collaborator(
                id = "c2",
                name = "David K.",
                colorHex = "#10B981", // green
                cursorX = Random.nextInt(-300, 300).toFloat(),
                cursorY = Random.nextInt(-200, 200).toFloat(),
                activeNodeId = null,
                status = "Idle"
            ),
            Collaborator(
                id = "c3",
                name = "Emily Rose",
                colorHex = "#F59E0B", // amber
                cursorX = Random.nextInt(-300, 300).toFloat(),
                cursorY = Random.nextInt(-200, 200).toFloat(),
                activeNodeId = null,
                status = "Idle"
            )
        )
    }

    fun toggleCollabSimulation() {
        collabSimulationActive.value = !collabSimulationActive.value
        if (collabSimulationActive.value) {
            startCollaboratorSimulation()
            addActivity("Collaboration simulation started.")
        } else {
            simulationJob?.cancel()
            _collaborators.value = emptyList()
            addActivity("Collaboration simulation paused.")
        }
    }

    private fun startCollaboratorSimulation() {
        simulationJob?.cancel()
        simulationJob = viewModelScope.launch {
            while (collabSimulationActive.value) {
                delay(Random.nextLong(4000, 8000))
                val currentNodes = nodes.value
                val currentCollabs = _collaborators.value
                val map = selectedMap.value
                
                if (currentNodes.isNotEmpty() && currentCollabs.isNotEmpty() && map != null) {
                    // Choose random collaborator to do something
                    val chosenCollab = currentCollabs.random()
                    val actionIndex = Random.nextInt(0, 3)
                    
                    val updatedCollabs = currentCollabs.map { col ->
                        if (col.id == chosenCollab.id) {
                            when (actionIndex) {
                                0 -> { // Move cursor
                                    col.copy(
                                        cursorX = col.cursorX + Random.nextInt(-60, 60),
                                        cursorY = col.cursorY + Random.nextInt(-60, 60),
                                        status = "Moving"
                                    )
                                }
                                1 -> { // Select node
                                    val randomNode = currentNodes.random()
                                    col.copy(
                                        cursorX = randomNode.offsetX + Random.nextInt(-20, 20),
                                        cursorY = randomNode.offsetY + Random.nextInt(-20, 20),
                                        activeNodeId = randomNode.id,
                                        status = "Reading '${randomNode.text.take(15)}...'"
                                    )
                                }
                                else -> { // Edit node or do something
                                    val randomNode = currentNodes.random()
                                    col.copy(
                                        cursorX = randomNode.offsetX,
                                        cursorY = randomNode.offsetY,
                                        activeNodeId = randomNode.id,
                                        status = "Editing node"
                                    )
                                }
                            }
                        } else {
                            col
                        }
                    }
                    _collaborators.value = updatedCollabs

                    // Perform a mock node creation/modification 15% of the time to show actual live syncing
                    if (actionIndex == 2 && Random.nextFloat() < 0.25f && currentNodes.size < 20) {
                        val parentNode = currentNodes.random()
                        val mockWords = listOf("Refined specs", "Competitive audit", "User flow v2", "Budget review", "QA tests", "Beta feedback")
                        val newText = mockWords.random()
                        val newId = UUID.randomUUID().toString()
                        
                        // Position new node relative to parent
                        val offsetAngle = Random.nextDouble(0.0, 2 * Math.PI)
                        val distance = 160f
                        val newX = parentNode.offsetX + (distance * Math.cos(offsetAngle)).toFloat()
                        val newY = parentNode.offsetY + (distance * Math.sin(offsetAngle)).toFloat()
                        
                        val newNode = MindMapNode(
                            id = newId,
                            mapId = map.id,
                            parentId = parentNode.id,
                            text = newText,
                            colorHex = parentNode.colorHex,
                            offsetX = newX,
                            offsetY = newY
                        )
                        
                        repository.saveNode(newNode)
                        addActivity("${chosenCollab.name} added node: '$newText'")
                    }
                }
            }
        }
    }

    fun addActivity(activity: String) {
        val currentList = _activities.value.toMutableList()
        currentList.add(0, "[${System.currentTimeMillis().toTimeFormat()}] $activity")
        _activities.value = currentList.take(30) // limit to 30 entries
    }

    private fun Long.toTimeFormat(): String {
        val date = java.util.Date(this)
        val format = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
        return format.format(date)
    }

    // --- Core Map Operations ---

    fun createNewMap(title: String, description: String, layoutType: LayoutType) {
        viewModelScope.launch {
            val newMapId = repository.insertMindMap(
                MindMap(
                    title = title,
                    description = description,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis(),
                    layoutType = layoutType
                )
            )

            // Create initial root node for the map
            val rootNode = MindMapNode(
                id = UUID.randomUUID().toString(),
                mapId = newMapId,
                parentId = null,
                text = title,
                colorHex = "#6750A4", // primary purple
                offsetX = 0f,
                offsetY = 0f
            )
            repository.saveNode(rootNode)
            
            // Reload and select this new map
            val mapsList = repository.getAllMindMaps()
            mindMaps.value = mapsList
            val newlyCreatedMap = mapsList.find { it.id == newMapId }
            if (newlyCreatedMap != null) {
                selectMap(newlyCreatedMap)
            }
            addActivity("Created new mind map: '$title'")
        }
    }

    fun deleteCurrentMap() {
        val map = selectedMap.value ?: return
        viewModelScope.launch {
            repository.deleteMindMap(map.id)
            selectedMap.value = null
            loadAllMaps()
            addActivity("Deleted map: '${map.title}'")
        }
    }

    fun updateCurrentMapLayout(layoutType: LayoutType) {
        val map = selectedMap.value ?: return
        viewModelScope.launch {
            val updated = map.copy(layoutType = layoutType)
            repository.updateMindMap(updated)
            selectedMap.value = updated
            addActivity("Changed layout style to: ${layoutType.name}")
        }
    }

    // --- Node operations ---

    fun selectNode(node: MindMapNode?) {
        selectedNode.value = node
    }

    fun addNode(parent: MindMapNode, text: String, colorHex: String? = null) {
        val map = selectedMap.value ?: return
        viewModelScope.launch {
            // position slightly offset from parent
            val offsetAngle = Random.nextDouble(0.0, 2 * Math.PI)
            val distance = 160f
            val ox = parent.offsetX + (distance * Math.cos(offsetAngle)).toFloat()
            val oy = parent.offsetY + (distance * Math.sin(offsetAngle)).toFloat()

            val newNode = MindMapNode(
                id = UUID.randomUUID().toString(),
                mapId = map.id,
                parentId = parent.id,
                text = text,
                colorHex = colorHex ?: parent.colorHex,
                offsetX = ox,
                offsetY = oy
            )
            repository.saveNode(newNode)
            addActivity("Added child node: '$text'")
        }
    }

    fun updateNodeText(node: MindMapNode, newText: String) {
        viewModelScope.launch {
            val updated = node.copy(text = newText)
            repository.updateNode(updated)
            addActivity("Updated node text to: '$newText'")
        }
    }

    fun updateNodeColor(node: MindMapNode, newColorHex: String) {
        viewModelScope.launch {
            val updated = node.copy(colorHex = newColorHex)
            repository.updateNode(updated)
            addActivity("Updated node color of '${node.text.take(15)}'")
        }
    }

    fun toggleNodeCompletion(node: MindMapNode) {
        viewModelScope.launch {
            val updated = node.copy(isCompleted = !node.isCompleted)
            repository.updateNode(updated)
            val stateText = if (updated.isCompleted) "completed" else "active"
            addActivity("Marked node '${node.text.take(15)}' as $stateText")
        }
    }

    fun toggleNodeCollapse(node: MindMapNode) {
        viewModelScope.launch {
            val updated = node.copy(isCollapsed = !node.isCollapsed)
            repository.updateNode(updated)
        }
    }

    fun updateNodePosition(node: MindMapNode, x: Float, y: Float) {
        viewModelScope.launch {
            val updated = node.copy(offsetX = x, offsetY = y)
            repository.updateNode(updated)
        }
    }

    fun deleteNode(node: MindMapNode) {
        if (node.parentId == null) {
            // Cannot delete root node directly, offer to delete mind map or clear branches
            return
        }
        viewModelScope.launch {
            // Delete the node and all of its descendants recursively
            deleteNodeAndChildrenRecursive(node.id)
            selectedNode.value = null
            addActivity("Deleted node: '${node.text}'")
        }
    }

    private suspend fun deleteNodeAndChildrenRecursive(nodeId: String) {
        val childNodes = nodes.value.filter { it.parentId == nodeId }
        for (child in childNodes) {
            deleteNodeAndChildrenRecursive(child.id)
        }
        repository.deleteNode(nodeId)
    }

    // --- AI Operations via Gemini API ---

    fun triggerAiExpand(node: MindMapNode) {
        val map = selectedMap.value ?: return
        isAiExpanding.value = true
        addActivity("AI is brainstorming expansions for '${node.text}'...")
        
        viewModelScope.launch {
            val parentNode = nodes.value.find { it.id == node.parentId }
            val suggestions = GeminiApiClient.expandNode(node.text, parentNode?.text)
            
            if (suggestions.isNotEmpty()) {
                // Insert the generated sub-nodes
                val mapId = map.id
                var startAngle = Random.nextDouble(0.0, 2 * Math.PI)
                val distance = 160f
                val slice = (2 * Math.PI) / suggestions.size

                val newNodes = suggestions.mapIndexed { idx, text ->
                    val angle = startAngle + (idx * slice)
                    val ox = node.offsetX + (distance * Math.cos(angle)).toFloat()
                    val oy = node.offsetY + (distance * Math.sin(angle)).toFloat()
                    
                    MindMapNode(
                        id = UUID.randomUUID().toString(),
                        mapId = mapId,
                        parentId = node.id,
                        text = text,
                        colorHex = node.colorHex,
                        offsetX = ox,
                        offsetY = oy
                    )
                }
                
                repository.saveNodes(newNodes)
                addActivity("AI brainstormed ${suggestions.size} ideas for '${node.text}' successfully!")
            } else {
                addActivity("AI Expand returned no responses. Check your internet connection or secrets key.")
            }
            isAiExpanding.value = false
        }
    }

    fun autoArrangeCurrentMap() {
        val map = selectedMap.value ?: return
        val currentNodes = nodes.value
        val root = currentNodes.find { it.parentId == null } ?: return
        
        viewModelScope.launch {
            // Reset root to center
            val updatedRoot = root.copy(offsetX = 0f, offsetY = 0f)
            repository.updateNode(updatedRoot)
            
            // Arrange children depending on layout type
            when (map.layoutType) {
                LayoutType.ORGANIC -> arrangeRadially(updatedRoot.id, currentNodes, 0f, 0f, 180f)
                LayoutType.TREE -> arrangeTree(updatedRoot.id, currentNodes, 0f, 0f, 1)
                LayoutType.ORTHOGONAL -> arrangeOrthogonal(updatedRoot.id, currentNodes, 0f, 0f, 1)
            }
            addActivity("Auto-arranged board layout.")
        }
    }

    private suspend fun arrangeRadially(parentId: String, allNodes: List<MindMapNode>, px: Float, py: Float, radius: Float) {
        val children = allNodes.filter { it.parentId == parentId }
        if (children.isEmpty()) return

        val slice = (2 * Math.PI) / children.size
        children.forEachIndexed { index, node ->
            val angle = index * slice
            val cx = px + (radius * Math.cos(angle)).toFloat()
            val cy = py + (radius * Math.sin(angle)).toFloat()
            
            val updatedNode = node.copy(offsetX = cx, offsetY = cy)
            repository.updateNode(updatedNode)
            
            // Sub-branches go further out with smaller radius
            arrangeRadially(node.id, allNodes, cx, cy, radius * 0.85f)
        }
    }

    private suspend fun arrangeTree(parentId: String, allNodes: List<MindMapNode>, px: Float, py: Float, depth: Int) {
        val children = allNodes.filter { it.parentId == parentId }
        if (children.isEmpty()) return

        val spacingY = 120f
        val startY = py - ((children.size - 1) * spacingY) / 2f
        val nextX = px + 220f

        children.forEachIndexed { index, node ->
            val cy = startY + (index * spacingY)
            val updatedNode = node.copy(offsetX = nextX, offsetY = cy)
            repository.updateNode(updatedNode)
            arrangeTree(node.id, allNodes, nextX, cy, depth + 1)
        }
    }

    private suspend fun arrangeOrthogonal(parentId: String, allNodes: List<MindMapNode>, px: Float, py: Float, depth: Int) {
        // Orthogonal behaves similarly to Tree but nodes have a grid alignment
        val children = allNodes.filter { it.parentId == parentId }
        if (children.isEmpty()) return

        val spacingY = 130f
        val startY = py - ((children.size - 1) * spacingY) / 2f
        val nextX = px + 240f

        children.forEachIndexed { index, node ->
            val cy = startY + (index * spacingY)
            val updatedNode = node.copy(offsetX = nextX, offsetY = cy)
            repository.updateNode(updatedNode)
            arrangeOrthogonal(node.id, allNodes, nextX, cy, depth + 1)
        }
    }
}

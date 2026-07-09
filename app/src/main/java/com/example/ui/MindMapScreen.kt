package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.BorderStroke
import com.example.data.model.Collaborator
import com.example.data.model.LayoutType
import com.example.data.model.MindMap
import com.example.data.model.MindMapNode
import kotlin.math.roundToInt

@Composable
fun MindNexusApp(viewModel: MindMapViewModel) {
    var activeTab by remember { mutableStateOf("board") }
    val selectedMap by viewModel.selectedMap.collectAsState()
    val mindMaps by viewModel.mindMaps.collectAsState()
    
    // Dialog states
    var showCreateMapDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Color(0xFFFEF7FF),
        topBar = {
            HeaderSection(
                selectedMap = selectedMap,
                mindMaps = mindMaps,
                onMapSelected = { viewModel.selectMap(it) },
                onCreateMapClick = { showCreateMapDialog = true }
            )
        },
        bottomBar = {
            BottomNavBar(
                activeTab = activeTab,
                onTabSelected = { activeTab = it }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (activeTab) {
                "board" -> WorkspaceBoard(viewModel)
                "team" -> TeamTab(viewModel)
                "alerts" -> AlertsTab(viewModel)
                "config" -> ConfigTab(viewModel)
            }
        }
    }

    if (showCreateMapDialog) {
        CreateMapDialog(
            onDismiss = { showCreateMapDialog = false },
            onCreate = { title, desc, layout ->
                viewModel.createNewMap(title, desc, layout)
                showCreateMapDialog = false
            }
        )
    }
}

// --- Header Component ---
@Composable
fun HeaderSection(
    selectedMap: MindMap?,
    mindMaps: List<MindMap>,
    onMapSelected: (MindMap) -> Unit,
    onCreateMapClick: () -> Unit
) {
    var expandedMenu by remember { mutableStateOf(false) }

    Surface(
        color = Color(0xFFFEF7FF),
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Purple MindNexus Logo
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(0xFF6750A4), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Hub,
                        contentDescription = "MindNexus Logo",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Title Selector
                Column(
                    modifier = Modifier.clickable { expandedMenu = true }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = selectedMap?.title ?: "No Workspace",
                            fontWeight = FontWeight.Medium,
                            fontSize = 18.sp,
                            color = Color(0xFF1D1B20),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Switch Workspace",
                            tint = Color(0xFF49454F)
                        )
                    }
                    Text(
                        text = "Active Canvas",
                        fontSize = 11.sp,
                        color = Color(0xFF6750A4),
                        fontWeight = FontWeight.Bold
                    )
                }

                DropdownMenu(
                    expanded = expandedMenu,
                    onDismissRequest = { expandedMenu = false },
                    modifier = Modifier.background(Color.White)
                ) {
                    Text(
                        text = "Switch Canvas",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF6750A4),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                    Divider(color = Color(0xFFCAC4D0))
                    mindMaps.forEach { map ->
                        DropdownMenuItem(
                            text = { Text(map.title, fontWeight = FontWeight.Medium) },
                            onClick = {
                                onMapSelected(map)
                                expandedMenu = false
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Schema, contentDescription = null, tint = Color(0xFF6750A4))
                            }
                        )
                    }
                    Divider(color = Color(0xFFCAC4D0))
                    DropdownMenuItem(
                        text = { Text("Create New Canvas...", color = Color(0xFF6750A4), fontWeight = FontWeight.Bold) },
                        onClick = {
                            expandedMenu = false
                            onCreateMapClick()
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFF6750A4))
                        }
                    )
                }
            }

            // Right side profile and status indicators
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Real-time sync indicator dot
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(Color(0xFF16A34A), CircleShape)
                )
                
                // User Avatar Badge
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFFFB923C), Color(0xFFF43F5E))
                            )
                        )
                        .border(1.5.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "ME",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// --- Bottom Navigation Bar ---
@Composable
fun BottomNavBar(
    activeTab: String,
    onTabSelected: (String) -> Unit
) {
    NavigationBar(
        containerColor = Color(0xFFF3EDF7),
        tonalElevation = 0.dp,
        modifier = Modifier.height(80.dp)
    ) {
        NavigationBarItem(
            selected = activeTab == "board",
            onClick = { onTabSelected("board") },
            label = { Text("Board", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
            icon = { Icon(Icons.Default.GridView, contentDescription = "Board view") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF21005D),
                selectedTextColor = Color(0xFF21005D),
                indicatorColor = Color(0xFFE8DEF8),
                unselectedIconColor = Color(0xFF49454F),
                unselectedTextColor = Color(0xFF49454F)
            )
        )
        NavigationBarItem(
            selected = activeTab == "team",
            onClick = { onTabSelected("team") },
            label = { Text("Team", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
            icon = { Icon(Icons.Default.Groups, contentDescription = "Team views") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF21005D),
                selectedTextColor = Color(0xFF21005D),
                indicatorColor = Color(0xFFE8DEF8),
                unselectedIconColor = Color(0xFF49454F),
                unselectedTextColor = Color(0xFF49454F)
            )
        )
        NavigationBarItem(
            selected = activeTab == "alerts",
            onClick = { onTabSelected("alerts") },
            label = { Text("Alerts", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
            icon = { Icon(Icons.Default.Notifications, contentDescription = "Real-time Alerts") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF21005D),
                selectedTextColor = Color(0xFF21005D),
                indicatorColor = Color(0xFFE8DEF8),
                unselectedIconColor = Color(0xFF49454F),
                unselectedTextColor = Color(0xFF49454F)
            )
        )
        NavigationBarItem(
            selected = activeTab == "config",
            onClick = { onTabSelected("config") },
            label = { Text("Config", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
            icon = { Icon(Icons.Default.Settings, contentDescription = "Layout settings") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF21005D),
                selectedTextColor = Color(0xFF21005D),
                indicatorColor = Color(0xFFE8DEF8),
                unselectedIconColor = Color(0xFF49454F),
                unselectedTextColor = Color(0xFF49454F)
            )
        )
    }
}

// ==================== WORKSPACE BOARD TAB ====================
@Composable
fun WorkspaceBoard(viewModel: MindMapViewModel) {
    val selectedMap by viewModel.selectedMap.collectAsState()
    val nodes by viewModel.nodes.collectAsState()
    val selectedNode by viewModel.selectedNode.collectAsState()
    val collaborators by viewModel.collaborators.collectAsState()
    val isAiExpanding by viewModel.isAiExpanding.collectAsState()
    
    // Canvas pan offset
    var panOffset by remember { mutableStateOf(Offset.Zero) }
    
    // Dialog / sheet states
    var showEditNodeDialog by remember { mutableStateOf<MindMapNode?>(null) }
    var showColorPickerDialog by remember { mutableStateOf<MindMapNode?>(null) }
    var showAddNodeDialog by remember { mutableStateOf<MindMapNode?>(null) }

    if (selectedMap == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Please select or create a mind map canvas to begin.")
        }
        return
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Quick Live banner
        LiveStatusBanner(viewModel, collaborators)

        // Interactive Infinite Mind Map Canvas
        Box(
            modifier = Modifier
                .fillMaxHeight(0.72f)
                .weight(1f)
                .background(Color(0xFFFEF7FF))
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        panOffset = Offset(panOffset.x + dragAmount.x, panOffset.y + dragAmount.y)
                    }
                }
        ) {
            // 1. Draw connection lines on Canvas layer
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cx = size.width / 2f
                val cy = size.height / 2f
                
                nodes.forEach { node ->
                    if (node.parentId != null) {
                        val parentNode = nodes.find { it.id == node.parentId }
                        if (parentNode != null && !parentNode.isCollapsed) {
                            val px = cx + parentNode.offsetX + panOffset.x
                            val py = cy + parentNode.offsetY + panOffset.y
                            val kx = cx + node.offsetX + panOffset.x
                            val ky = cy + node.offsetY + panOffset.y
                            
                            val pathColor = Color(android.graphics.Color.parseColor(node.colorHex))
                            
                            when (selectedMap?.layoutType) {
                                LayoutType.ORGANIC -> {
                                    // Beautiful Bezier Organic curve (Coggle style)
                                    val path = Path().apply {
                                        moveTo(px, py)
                                        // control points half way
                                        val ctrlX1 = px + (kx - px) * 0.5f
                                        val ctrlY1 = py
                                        val ctrlX2 = kx - (kx - px) * 0.5f
                                        val ctrlY2 = ky
                                        cubicTo(ctrlX1, ctrlY1, ctrlX2, ctrlY2, kx, ky)
                                    }
                                    drawPath(
                                        path = path,
                                        color = pathColor,
                                        style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
                                    )
                                }
                                LayoutType.TREE -> {
                                    // Direct connections
                                    drawLine(
                                        color = pathColor,
                                        start = Offset(px, py),
                                        end = Offset(kx, ky),
                                        strokeWidth = 3.dp.toPx(),
                                        cap = StrokeCap.Round
                                    )
                                }
                                LayoutType.ORTHOGONAL -> {
                                    // Right angle orthogonal connections (Edraw style)
                                    val midX = px + (kx - px) * 0.5f
                                    drawLine(
                                        color = pathColor,
                                        start = Offset(px, py),
                                        end = Offset(midX, py),
                                        strokeWidth = 3.dp.toPx()
                                    )
                                    drawLine(
                                        color = pathColor,
                                        start = Offset(midX, py),
                                        end = Offset(midX, ky),
                                        strokeWidth = 3.dp.toPx()
                                    )
                                    drawLine(
                                        color = pathColor,
                                        start = Offset(midX, ky),
                                        end = Offset(kx, ky),
                                        strokeWidth = 3.dp.toPx()
                                    )
                                }
                                null -> {}
                            }
                        }
                    }
                }
            }

            // 2. Render Nodes layer
            val density = LocalDensity.current
            val panOffsetDpX = with(density) { panOffset.x.toDp().value }
            val panOffsetDpY = with(density) { panOffset.y.toDp().value }

            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val cx = maxWidth.value / 2f
                val cy = maxHeight.value / 2f

                nodes.forEach { node ->
                    // Determine if node is visible (i.e. parent is not collapsed)
                    if (isNodeVisible(node, nodes)) {
                        val xPos = cx + node.offsetX + panOffsetDpX
                        val yPos = cy + node.offsetY + panOffsetDpY
                        
                        NodeItem(
                            node = node,
                            isSelected = selectedNode?.id == node.id,
                            onNodeClick = { viewModel.selectNode(node) },
                            onNodeDrag = { dx, dy ->
                                val dxDp = with(density) { dx.toDp().value }
                                val dyDp = with(density) { dy.toDp().value }
                                viewModel.updateNodePosition(node, node.offsetX + dxDp, node.offsetY + dyDp)
                            },
                            modifier = Modifier
                                .absoluteOffset {
                                    IntOffset(
                                        (xPos.dp.toPx()).roundToInt(),
                                        (yPos.dp.toPx()).roundToInt()
                                    )
                                }
                        )
                    }
                }

                // 3. Render Simulated Live Cursors
                collaborators.forEach { collab ->
                    val cursorX = cx + collab.cursorX + panOffsetDpX
                    val cursorY = cy + collab.cursorY + panOffsetDpY
                    
                    CollaboratorCursor(
                        collaborator = collab,
                        modifier = Modifier
                            .absoluteOffset {
                                IntOffset(
                                    (cursorX.dp.toPx()).roundToInt(),
                                    (cursorY.dp.toPx()).roundToInt()
                                )
                            }
                    )
                }
            }
            
            // Quick Auto-Arrange and AI Status floating indicators
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Reset View Button
                    FloatingActionButton(
                        onClick = { panOffset = Offset.Zero },
                        containerColor = Color.White,
                        contentColor = Color(0xFF6750A4),
                        modifier = Modifier.size(44.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.FilterCenterFocus, "Reset Canvas View")
                    }

                    // Auto Arrange button
                    FloatingActionButton(
                        onClick = { viewModel.autoArrangeCurrentMap() },
                        containerColor = Color(0xFFEADDFF),
                        contentColor = Color(0xFF21005D),
                        modifier = Modifier.size(44.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.AutoAwesome, "Auto Layout Grid")
                    }
                }
            }
            
            if (isAiExpanding) {
                Card(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(24.dp)
                        .shadow(12.dp, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFD0BCFF))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color(0xFF21005D))
                        Text("AI is generating nodes...", fontWeight = FontWeight.Bold, color = Color(0xFF21005D))
                    }
                }
            }
        }

        // Quick Context Action Bar at the bottom
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .shadow(8.dp, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
            color = Color.White,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            if (selectedNode != null) {
                val node = selectedNode!!
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = node.text,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1D1B20),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.fillMaxWidth(0.6f)
                            )
                            Text("Selected Node Actions", fontSize = 11.sp, color = Color(0xFF49454F))
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            // Expand/Collapse Child branches button
                            val hasChildren = nodes.any { it.parentId == node.id }
                            if (hasChildren) {
                                TextButton(
                                    onClick = { viewModel.toggleNodeCollapse(node) },
                                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF6750A4))
                                ) {
                                    Icon(
                                        imageVector = if (node.isCollapsed) Icons.Default.ExpandMore else Icons.Default.ExpandLess,
                                        contentDescription = "Collapse children",
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(if (node.isCollapsed) "Expand" else "Collapse", fontSize = 11.sp)
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        // Edit
                        IconButtonWithLabel(
                            imageVector = Icons.Default.Edit,
                            label = "Edit",
                            onClick = { showEditNodeDialog = node },
                            tag = "edit_node_button"
                        )
                        // Add Child
                        IconButtonWithLabel(
                            imageVector = Icons.Default.Add,
                            label = "Add Child",
                            onClick = { showAddNodeDialog = node },
                            tag = "add_child_button"
                        )
                        // AI Expand
                        IconButtonWithLabel(
                            imageVector = Icons.Default.AutoAwesome,
                            label = "AI Expand",
                            onClick = { viewModel.triggerAiExpand(node) },
                            tint = Color(0xFF6750A4),
                            tag = "ai_expand_button"
                        )
                        // Colors
                        IconButtonWithLabel(
                            imageVector = Icons.Default.Palette,
                            label = "Color",
                            onClick = { showColorPickerDialog = node },
                            tag = "color_picker_button"
                        )
                        // Done state toggle
                        IconButtonWithLabel(
                            imageVector = if (node.isCompleted) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
                            label = if (node.isCompleted) "Done" else "Active",
                            onClick = { viewModel.toggleNodeCompletion(node) },
                            tint = if (node.isCompleted) Color(0xFF16A34A) else Color(0xFF49454F),
                            tag = "complete_node_button"
                        )
                        // Delete node
                        if (node.parentId != null) {
                            IconButtonWithLabel(
                                imageVector = Icons.Default.Delete,
                                label = "Delete",
                                onClick = { viewModel.deleteNode(node) },
                                tint = Color.Red,
                                tag = "delete_node_button"
                            )
                        }
                    }
                }
            } else {
                // Empty state instructions
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.TouchApp,
                            contentDescription = null,
                            tint = Color(0xFF6750A4),
                            modifier = Modifier.size(32.dp)
                        )
                        Text(
                            text = "Tap on any node to select actions, or press & drag to reposition nodes.",
                            fontSize = 13.sp,
                            color = Color(0xFF49454F),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }

    // Modal dialog overlays
    if (showEditNodeDialog != null) {
        EditTextDialog(
            node = showEditNodeDialog!!,
            onDismiss = { showEditNodeDialog = null },
            onSave = { nodeText ->
                viewModel.updateNodeText(showEditNodeDialog!!, nodeText)
                showEditNodeDialog = null
            }
        )
    }

    if (showAddNodeDialog != null) {
        AddChildDialog(
            parentNode = showAddNodeDialog!!,
            onDismiss = { showAddNodeDialog = null },
            onAdd = { childText ->
                viewModel.addNode(showAddNodeDialog!!, childText)
                showAddNodeDialog = null
            }
        )
    }

    if (showColorPickerDialog != null) {
        ColorPickerDialog(
            node = showColorPickerDialog!!,
            onDismiss = { showColorPickerDialog = null },
            onColorSelected = { hex ->
                viewModel.updateNodeColor(showColorPickerDialog!!, hex)
                showColorPickerDialog = null
            }
        )
    }
}

// Live sync top status banner
@Composable
fun LiveStatusBanner(viewModel: MindMapViewModel, collaborators: List<Collaborator>) {
    val syncStatus by viewModel.syncStatus.collectAsState()
    val simulationActive by viewModel.collabSimulationActive.collectAsState()

    Surface(
        color = Color(0xFFEADDFF),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(Color(0xFF6750A4), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Sync,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
                Column {
                    Text(syncStatus, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF21005D))
                    Text(
                        text = if (simulationActive) "3 users in room • Sync active" else "Solo session • Local mode",
                        fontSize = 10.sp,
                        color = Color(0xFF49454F)
                    )
                }
            }

            // Small participant bubble list
            if (simulationActive && collaborators.isNotEmpty()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy((-6).dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    collaborators.forEach { collab ->
                        val col = Color(android.graphics.Color.parseColor(collab.colorHex))
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(col)
                                .border(1.5.dp, Color(0xFFEADDFF), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = collab.name.take(1),
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(1.5.dp, Color(0xFFEADDFF), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("+1", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF49454F))
                    }
                }
            } else {
                TextButton(
                    onClick = { viewModel.toggleCollabSimulation() },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF6750A4))
                ) {
                    Text("Go Live", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// Single mind map node visual component
@Composable
fun NodeItem(
    node: MindMapNode,
    isSelected: Boolean,
    onNodeClick: () -> Unit,
    onNodeDrag: (Float, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val nodeBgColor = Color(android.graphics.Color.parseColor(node.colorHex))
    val isRoot = node.parentId == null
    
    Card(
        shape = RoundedCornerShape(if (isRoot) 16.dp else 12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isRoot) Color(0xFF6750A4) else Color.White
        ),
        modifier = modifier
            .widthIn(min = 100.dp, max = 220.dp)
            .pointerInput(node.id) {
                detectDragGestures(
                    onDragStart = { onNodeClick() }
                ) { change, dragAmount ->
                    change.consume()
                    onNodeDrag(dragAmount.x, dragAmount.y)
                }
            }
            .pointerInput(node.id) {
                detectTapGestures(onTap = { onNodeClick() })
            }
            .border(
                width = if (isSelected) 3.dp else if (isRoot) 0.dp else 1.5.dp,
                color = if (isSelected) Color(0xFF21005D) else if (isRoot) Color.Transparent else nodeBgColor,
                shape = RoundedCornerShape(if (isRoot) 16.dp else 12.dp)
            )
            .shadow(
                elevation = if (isSelected) 8.dp else 2.dp,
                shape = RoundedCornerShape(if (isRoot) 16.dp else 12.dp)
            )
            .testTag("node_${node.id}")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Check indicator
            if (node.isCompleted) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Completed",
                    tint = if (isRoot) Color.White else Color(0xFF16A34A),
                    modifier = Modifier.size(16.dp)
                )
            } else if (isRoot) {
                Icon(
                    imageVector = Icons.Default.BubbleChart,
                    contentDescription = "Central Topic",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }

            Column(modifier = Modifier.weight(1f, fill = false)) {
                Text(
                    text = node.text,
                    fontSize = if (isRoot) 14.sp else 12.sp,
                    fontWeight = if (isRoot) FontWeight.Bold else FontWeight.Medium,
                    color = if (isRoot) Color.White else Color(0xFF1D1B20),
                    textDecoration = if (node.isCompleted) TextDecoration.LineThrough else null,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // If node has collapsed child nodes, show small badge indicator
            if (node.isCollapsed) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(nodeBgColor.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("+", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = nodeBgColor)
                }
            }
        }
    }
}

// Simulated active live cursor
@Composable
fun CollaboratorCursor(
    collaborator: Collaborator,
    modifier: Modifier = Modifier
) {
    val cursorColor = Color(android.graphics.Color.parseColor(collaborator.colorHex))
    
    Box(
        modifier = modifier
            .wrapContentSize()
            .pointerInput(collaborator.id) {} // disable click pass through
    ) {
        Column(horizontalAlignment = Alignment.Start) {
            // Cursor pointer triangle
            Icon(
                imageVector = Icons.Default.Navigation,
                contentDescription = null,
                tint = cursorColor,
                modifier = Modifier
                    .size(20.dp)
                    .graphicsLayer(rotationZ = -45f)
            )
            
            // Text label
            Card(
                shape = RoundedCornerShape(4.dp),
                colors = CardDefaults.cardColors(containerColor = cursorColor),
                modifier = Modifier.absoluteOffset(x = 8.dp, y = (-4).dp)
            ) {
                Column(modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)) {
                    Text(
                        text = collaborator.name,
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (collaborator.status.isNotEmpty() && collaborator.status != "Idle") {
                        Text(
                            text = collaborator.status,
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 7.sp,
                            lineHeight = 8.sp
                        )
                    }
                }
            }
        }
    }
}

// Utility function to check if parent nodes are collapsed
fun isNodeVisible(node: MindMapNode, allNodes: List<MindMapNode>): Boolean {
    var currentParentId = node.parentId
    while (currentParentId != null) {
        val parentNode = allNodes.find { it.id == currentParentId } ?: return true
        if (parentNode.isCollapsed) {
            return false
        }
        currentParentId = parentNode.parentId
    }
    return true
}

@Composable
fun IconButtonWithLabel(
    imageVector: ImageVector,
    label: String,
    onClick: () -> Unit,
    tint: Color = Color(0xFF49454F),
    tag: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .clickable { onClick() }
            .padding(4.dp)
            .testTag(tag)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(Color(0xFFF3EDF7), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = imageVector,
                contentDescription = label,
                tint = tint,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label, fontSize = 10.sp, color = Color(0xFF49454F), fontWeight = FontWeight.Bold)
    }
}

// ==================== TEAM COLLABORATION TAB ====================
@Composable
fun TeamTab(viewModel: MindMapViewModel) {
    val collaborators by viewModel.collaborators.collectAsState()
    val simulationActive by viewModel.collabSimulationActive.collectAsState()
    val currentRoomCode by viewModel.currentRoomCode.collectAsState()
    val currentUserName by viewModel.currentUserName.collectAsState()

    var userNameInput by remember { mutableStateOf(currentUserName) }
    var roomCodeInput by remember { mutableStateOf(currentRoomCode) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Real-Time Collaboration",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF21005D)
            )
            Text(
                text = "Configure synchronization servers, rooms, and live editors.",
                fontSize = 12.sp,
                color = Color(0xFF49454F)
            )
        }

        // Room and identity Configuration
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFCAC4D0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Connection Configuration", fontWeight = FontWeight.Bold, color = Color(0xFF6750A4))

                    OutlinedTextField(
                        value = userNameInput,
                        onValueChange = {
                            userNameInput = it
                            viewModel.currentUserName.value = it
                        },
                        label = { Text("Your Username") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = roomCodeInput,
                        onValueChange = {
                            roomCodeInput = it
                            viewModel.currentRoomCode.value = it
                        },
                        label = { Text("Active Room Code") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        trailingIcon = {
                            Icon(Icons.Default.Key, contentDescription = null, tint = Color(0xFF6750A4))
                        }
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Simulate Multi-User Sync", fontWeight = FontWeight.Medium)
                        Switch(
                            checked = simulationActive,
                            onCheckedChange = { viewModel.toggleCollabSimulation() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF6750A4)
                            )
                        )
                    }
                }
            }
        }

        // Active Editors List
        item {
            Text("Active Editors in Room #${currentRoomCode}", fontWeight = FontWeight.Bold, color = Color(0xFF1D1B20))
        }

        if (simulationActive) {
            items(collaborators) { collab ->
                val col = Color(android.graphics.Color.parseColor(collab.colorHex))
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE8DEF8)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(col, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(collab.name.take(1), color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(collab.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(
                                text = "Device ID: NEXUS-DEV-${collab.id.uppercase()}",
                                fontSize = 11.sp,
                                color = Color(0xFF49454F)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFE8DEF8), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = collab.status,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF21005D)
                            )
                        }
                    }
                }
            }
        } else {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Simulation offline. Toggle 'Simulate Multi-User Sync' to see live collaborators.", textAlign = TextAlign.Center, fontSize = 13.sp, color = Color(0xFF49454F))
                }
            }
        }
    }
}

// ==================== ALERTS SYNC LOG TAB ====================
@Composable
fun AlertsTab(viewModel: MindMapViewModel) {
    val activities by viewModel.activities.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Activity Log & Alerts",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF21005D)
        )
        Text(
            text = "Real-time audit log of changes synced between users.",
            fontSize = 12.sp,
            color = Color(0xFF49454F)
        )

        Divider(color = Color(0xFFCAC4D0))

        if (activities.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("No sync activities logged yet. Edit nodes or go live to generate logs.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(activities) { log ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE8DEF8)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudSync,
                                contentDescription = null,
                                tint = Color(0xFF6750A4),
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = log,
                                fontSize = 12.sp,
                                color = Color(0xFF1D1B20),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==================== CONFIG / SETTINGS TAB ====================
@Composable
fun ConfigTab(viewModel: MindMapViewModel) {
    val selectedMap by viewModel.selectedMap.collectAsState()
    val nodes by viewModel.nodes.collectAsState()
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Nexus Configurations",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF21005D)
            )
            Text(
                text = "Set up visual layouts, map algorithms, or export data.",
                fontSize = 12.sp,
                color = Color(0xFF49454F)
            )
        }

        // Layout Type config
        if (selectedMap != null) {
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFCAC4D0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Visual Connection Layout", fontWeight = FontWeight.Bold, color = Color(0xFF6750A4))
                        Text(
                            "Choose connection styles inspired by Coggle or EdrawMind:",
                            fontSize = 11.sp,
                            color = Color(0xFF49454F)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            LayoutSelectorButton(
                                title = "Organic Curve",
                                desc = "Coggle style Bezier",
                                isSelected = selectedMap!!.layoutType == LayoutType.ORGANIC,
                                onClick = { viewModel.updateCurrentMapLayout(LayoutType.ORGANIC) },
                                modifier = Modifier.weight(1f)
                            )
                            LayoutSelectorButton(
                                title = "Orthogonal",
                                desc = "Edraw style grid",
                                isSelected = selectedMap!!.layoutType == LayoutType.ORTHOGONAL,
                                onClick = { viewModel.updateCurrentMapLayout(LayoutType.ORTHOGONAL) },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        LayoutSelectorButton(
                            title = "Standard Tree",
                            desc = "Linear hierarchy",
                            isSelected = selectedMap!!.layoutType == LayoutType.TREE,
                            onClick = { viewModel.updateCurrentMapLayout(LayoutType.TREE) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Export options
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFCAC4D0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Export Workspace", fontWeight = FontWeight.Bold, color = Color(0xFF6750A4))

                        Button(
                            onClick = {
                                val textOutline = generateTextOutline(nodes)
                                android.widget.Toast.makeText(context, "Copied outline to clipboard!", android.widget.Toast.LENGTH_SHORT).show()
                                val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                val clip = android.content.ClipData.newPlainText("MindMap Outline", textOutline)
                                clipboard.setPrimaryClip(clip)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4)),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Copy Text Outline to Clipboard")
                        }

                        Button(
                            onClick = {
                                val jsonExport = generateJsonExport(selectedMap!!, nodes)
                                android.widget.Toast.makeText(context, "Copied JSON to clipboard!", android.widget.Toast.LENGTH_SHORT).show()
                                val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                val clip = android.content.ClipData.newPlainText("MindMap JSON", jsonExport)
                                clipboard.setPrimaryClip(clip)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF21005D)),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Export as JSON Structure")
                        }
                    }
                }
            }

            // Delete current map
            item {
                Button(
                    onClick = { viewModel.deleteCurrentMap() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF87171)),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Delete This Active Canvas", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun LayoutSelectorButton(
    title: String,
    desc: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            width = if (isSelected) 2.5.dp else 1.dp,
            color = if (isSelected) Color(0xFF6750A4) else Color(0xFFCAC4D0)
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFFE8DEF8) else Color.White
        ),
        modifier = modifier.clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF21005D))
            Text(desc, fontSize = 9.sp, color = Color(0xFF49454F))
        }
    }
}

// Generate an indented string list representation of mind map
fun generateTextOutline(nodes: List<MindMapNode>): String {
    val root = nodes.find { it.parentId == null } ?: return "Empty mind map"
    val sb = java.lang.StringBuilder()
    sb.append("=== ${root.text} ===\n")
    appendChildrenOutline(root.id, nodes, sb, 1)
    return sb.toString()
}

fun appendChildrenOutline(parentId: String, nodes: List<MindMapNode>, sb: java.lang.StringBuilder, indent: Int) {
    val children = nodes.filter { it.parentId == parentId }
    children.forEach { node ->
        sb.append("  ".repeat(indent))
        sb.append("- ")
        sb.append(node.text)
        if (node.isCompleted) sb.append(" (Completed)")
        sb.append("\n")
        appendChildrenOutline(node.id, nodes, sb, indent + 1)
    }
}

// Simple custom manual JSON builder
fun generateJsonExport(map: MindMap, nodes: List<MindMapNode>): String {
    val sb = java.lang.StringBuilder()
    sb.append("{\n")
    sb.append("  \"canvasTitle\": \"${map.title}\",\n")
    sb.append("  \"layoutType\": \"${map.layoutType}\",\n")
    sb.append("  \"nodes\": [\n")
    nodes.forEachIndexed { index, node ->
        sb.append("    {\n")
        sb.append("      \"id\": \"${node.id}\",\n")
        sb.append("      \"parentId\": ${if (node.parentId != null) "\"${node.parentId}\"" else "null"},\n")
        sb.append("      \"text\": \"${node.text.replace("\"", "\\\"")}\",\n")
        sb.append("      \"color\": \"${node.colorHex}\",\n")
        sb.append("      \"completed\": ${node.isCompleted}\n")
        sb.append("    }${if (index < nodes.size - 1) "," else ""}\n")
    }
    sb.append("  ]\n")
    sb.append("}")
    return sb.toString()
}

// ==================== DIALOG OVERLAYS ====================

@Composable
fun CreateMapDialog(
    onDismiss: () -> Unit,
    onCreate: (String, String, LayoutType) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var layout by remember { mutableStateOf(LayoutType.ORGANIC) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = Color.White,
            tonalElevation = 6.dp,
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Create New Mind Map", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color(0xFF6750A4))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Connection Style", fontWeight = FontWeight.Medium, fontSize = 12.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { layout = LayoutType.ORGANIC },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (layout == LayoutType.ORGANIC) Color(0xFF6750A4) else Color(0xFFF3EDF7),
                            contentColor = if (layout == LayoutType.ORGANIC) Color.White else Color(0xFF49454F)
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Organic", fontSize = 11.sp)
                    }
                    Button(
                        onClick = { layout = LayoutType.ORTHOGONAL },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (layout == LayoutType.ORTHOGONAL) Color(0xFF6750A4) else Color(0xFFF3EDF7),
                            contentColor = if (layout == LayoutType.ORTHOGONAL) Color.White else Color(0xFF49454F)
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Orthogonal", fontSize = 11.sp)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { if (title.isNotBlank()) onCreate(title, desc, layout) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4)),
                        enabled = title.isNotBlank()
                    ) {
                        Text("Create")
                    }
                }
            }
        }
    }
}

@Composable
fun EditTextDialog(
    node: MindMapNode,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var text by remember { mutableStateOf(node.text) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Edit Node Text", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF6750A4))

                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Node Text") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { if (text.isNotBlank()) onSave(text) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4))
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}

@Composable
fun AddChildDialog(
    parentNode: MindMapNode,
    onDismiss: () -> Unit,
    onAdd: (String) -> Unit
) {
    var text by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Add Child Node", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF6750A4))
                Text("Adding branch to '${parentNode.text.take(20)}...'", fontSize = 11.sp, color = Color(0xFF49454F))

                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Node Text") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { if (text.isNotBlank()) onAdd(text) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4)),
                        enabled = text.isNotBlank()
                    ) {
                        Text("Add")
                    }
                }
            }
        }
    }
}

@Composable
fun ColorPickerDialog(
    node: MindMapNode,
    onDismiss: () -> Unit,
    onColorSelected: (String) -> Unit
) {
    val colors = listOf(
        "#6750A4", // Purple M3
        "#0284C7", // Sky blue
        "#16A34A", // Green
        "#EA580C", // Orange
        "#DB2777", // Pink
        "#E11D48", // Rose red
        "#4F46E5", // Indigo
        "#0D9488", // Teal
        "#CA8A04"  // Dark yellow
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Choose Branch Color", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF6750A4))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.height(180.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(colors) { hex ->
                        val parsedColor = Color(android.graphics.Color.parseColor(hex))
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(parsedColor)
                                .clickable { onColorSelected(hex) }
                                .border(
                                    width = if (node.colorHex == hex) 3.dp else 0.dp,
                                    color = Color.Black,
                                    shape = RoundedCornerShape(12.dp)
                                )
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Close")
                    }
                }
            }
        }
    }
}



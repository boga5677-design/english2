@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.lin.toeic3000.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lin.toeic3000.AppViewModel
import com.lin.toeic3000.data.Word

private enum class Tab(val title: String) {
    HOME("首頁"), QUIZ("測驗"), CARDS("單字卡"), SEARCH("搜尋"), MORE("更多")
}

@Composable
fun ToeicApp(vm: AppViewModel) {
    val state by vm.state.collectAsStateWithLifecycle()
    var tab by remember { mutableStateOf(Tab.HOME) }

    MaterialTheme(colorScheme = if (state.darkMode) darkColorScheme() else lightColorScheme()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("TOEIC 3000", fontWeight = FontWeight.Bold) },
                    actions = {
                        IconButton(onClick = { vm.setDarkMode(!state.darkMode) }) {
                            Icon(if (state.darkMode) Icons.Default.LightMode else Icons.Default.DarkMode, null)
                        }
                    }
                )
            },
            bottomBar = {
                NavigationBar {
                    val icons = listOf(
                        Icons.Default.Home, Icons.Default.Quiz, Icons.Default.Style,
                        Icons.Default.Search, Icons.Default.MoreHoriz
                    )
                    Tab.entries.forEachIndexed { index, item ->
                        NavigationBarItem(
                            selected = tab == item,
                            onClick = { tab = item },
                            icon = { Icon(icons[index], null) },
                            label = { Text(item.title) }
                        )
                    }
                }
            }
        ) { padding ->
            if (state.loading) {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                Box(Modifier.fillMaxSize().padding(padding)) {
                    when (tab) {
                        Tab.HOME -> HomeScreen(state.words.size, state.studied.size, state.answered, state.correct, state.mistakes.size, state.favorites.size)
                        Tab.QUIZ -> QuizScreen(state.words, vm)
                        Tab.CARDS -> FlashcardScreen(state.words, state.favorites, vm)
                        Tab.SEARCH -> SearchScreen(state.words, state.favorites, vm)
                        Tab.MORE -> MoreScreen(state, vm)
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeScreen(total: Int, studied: Int, answered: Int, correct: Int, mistakes: Int, favorites: Int) {
    val accuracy = if (answered == 0) 0 else correct * 100 / answered
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Text("今日學習總覽", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold) }
        item { ProgressCard("已學單字", "$studied / $total", studied.toFloat() / total.coerceAtLeast(1)) }
        item { ProgressCard("累計答題", "$answered 題", null) }
        item { ProgressCard("正確率", "$accuracy%", accuracy / 100f) }
        item { ProgressCard("錯題／收藏", "$mistakes / $favorites", null) }
        item {
            Card {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("每日目標", fontWeight = FontWeight.Bold)
                    Text("每天完成 20 題，逐步掌握 TOEIC 3000 單字。")
                }
            }
        }
    }
}

@Composable
private fun ProgressCard(title: String, value: String, progress: Float?) {
    Card(shape = RoundedCornerShape(20.dp)) {
        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.labelLarge)
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            if (progress != null) LinearProgressIndicator(progress = { progress.coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun QuizScreen(words: List<Word>, vm: AppViewModel) {
    var started by remember { mutableStateOf(false) }
    var mode by remember { mutableStateOf("英翻中") }
    var count by remember { mutableIntStateOf(20) }
    var level by remember { mutableStateOf("全部") }
    var index by remember { mutableIntStateOf(0) }
    var score by remember { mutableIntStateOf(0) }
    var quizWords by remember { mutableStateOf(emptyList<Word>()) }
    var feedback by remember { mutableStateOf<String?>(null) }

    if (!started) {
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item { Text("測驗設定", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold) }
            item { ChoiceChips(listOf("英翻中", "中翻英", "聽力"), mode) { mode = it } }
            item { ChoiceChips(listOf("10", "20", "50", "100"), count.toString()) { count = it.toInt() } }
            item { ChoiceChips(listOf("全部", "初級", "中級", "進階"), level) { level = it } }
            item {
                Button(
                    onClick = {
                        val pool = words.filter { level == "全部" || it.level == level }.shuffled()
                        quizWords = pool.take(count.coerceAtMost(pool.size))
                        index = 0; score = 0; feedback = null; started = true
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) { Text("開始測驗") }
            }
        }
    } else if (index >= quizWords.size) {
        Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text("測驗完成", style = MaterialTheme.typography.headlineMedium)
            Text("$score / ${quizWords.size}", style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(24.dp))
            Button(onClick = { started = false }) { Text("重新設定") }
        }
    } else {
        val current = quizWords[index]
        val options = remember(index, quizWords) {
            (listOf(current) + words.filter { it.id != current.id }.shuffled().take(3)).shuffled()
        }
        Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("第 ${index + 1} / ${quizWords.size} 題　得分 $score")
            Card(shape = RoundedCornerShape(24.dp)) {
                Column(Modifier.fillMaxWidth().padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        when (mode) {
                            "英翻中" -> current.english
                            "中翻英" -> current.chinese
                            else -> "聽音選答案"
                        },
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    if (mode == "聽力") {
                        Spacer(Modifier.height(12.dp))
                        FilledTonalButton(onClick = { vm.speak(current.english) }) {
                            Icon(Icons.Default.VolumeUp, null)
                            Spacer(Modifier.width(8.dp))
                            Text("播放發音")
                        }
                    }
                }
            }
            options.forEach { option ->
                val label = if (mode == "中翻英") option.english else option.chinese
                OutlinedButton(
                    onClick = {
                        val ok = option.id == current.id
                        vm.recordAnswer(current, ok)
                        if (ok) score++
                        feedback = if (ok) "答對了！" else "答案：${current.english}－${current.chinese}"
                    },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp)
                ) { Text(label) }
            }
            feedback?.let {
                Card {
                    Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(it, Modifier.weight(1f))
                        TextButton(onClick = { feedback = null; index++ }) { Text("下一題") }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChoiceChips(items: List<String>, selected: String, onSelect: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items.chunked(4).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach {
                    FilterChip(selected = selected == it, onClick = { onSelect(it) }, label = { Text(it) })
                }
            }
        }
    }
}

@Composable
private fun FlashcardScreen(words: List<Word>, favorites: Set<Int>, vm: AppViewModel) {
    var index by remember { mutableIntStateOf(0) }
    var flipped by remember { mutableStateOf(false) }
    if (words.isEmpty()) return
    val word = words[index % words.size]
    Column(
        Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("${index + 1} / ${words.size}　${word.level}")
        Card(
            onClick = { flipped = !flipped },
            modifier = Modifier.fillMaxWidth().weight(1f),
            shape = RoundedCornerShape(28.dp)
        ) {
            Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                Text(
                    if (!flipped) word.english else "${word.chinese}\n\n${word.exampleEnglish}\n${word.exampleChinese}",
                    style = if (!flipped) MaterialTheme.typography.displaySmall else MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            IconButton(onClick = { index = (index - 1 + words.size) % words.size; flipped = false }) { Icon(Icons.Default.ArrowBack, null) }
            IconButton(onClick = { vm.speak(word.english) }) { Icon(Icons.Default.VolumeUp, null) }
            IconButton(onClick = { vm.toggleFavorite(word.id) }) {
                Icon(if (word.id in favorites) Icons.Default.Star else Icons.Default.StarBorder, null)
            }
            IconButton(onClick = { index = (index + 1) % words.size; flipped = false }) { Icon(Icons.Default.ArrowForward, null) }
        }
    }
}

@Composable
private fun SearchScreen(words: List<Word>, favorites: Set<Int>, vm: AppViewModel) {
    var query by remember { mutableStateOf(TextFieldValue("")) }
    val results = remember(query.text, words) {
        if (query.text.isBlank()) emptyList()
        else words.filter { it.english.contains(query.text, true) || it.chinese.contains(query.text) }.take(100)
    }
    Column(Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            label = { Text("搜尋英文或中文") },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            singleLine = true
        )
        LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(results, key = { it.id }) { word ->
                Card {
                    Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(word.english, fontWeight = FontWeight.Bold)
                            Text("${word.chinese}　${word.level}")
                        }
                        IconButton(onClick = { vm.speak(word.english) }) { Icon(Icons.Default.VolumeUp, null) }
                        IconButton(onClick = { vm.toggleFavorite(word.id) }) {
                            Icon(if (word.id in favorites) Icons.Default.Star else Icons.Default.StarBorder, null)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MoreScreen(state: com.lin.toeic3000.AppState, vm: AppViewModel) {
    var showFavorites by remember { mutableStateOf(false) }
    var showMistakes by remember { mutableStateOf(false) }
    val list = when {
        showFavorites -> state.words.filter { it.id in state.favorites }
        showMistakes -> state.words.filter { it.id in state.mistakes }
        else -> emptyList()
    }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { Text("學習工具與設定", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold) }
        item {
            ListItem(
                headlineContent = { Text("收藏單字") },
                supportingContent = { Text("${state.favorites.size} 個") },
                leadingContent = { Icon(Icons.Default.Star, null) },
                modifier = Modifier.fillMaxWidth()
            )
            Button(onClick = { showFavorites = !showFavorites; showMistakes = false }, modifier = Modifier.fillMaxWidth()) { Text("顯示收藏") }
        }
        item {
            ListItem(
                headlineContent = { Text("錯題本") },
                supportingContent = { Text("${state.mistakes.size} 個") },
                leadingContent = { Icon(Icons.Default.ErrorOutline, null) }
            )
            Button(onClick = { showMistakes = !showMistakes; showFavorites = false }, modifier = Modifier.fillMaxWidth()) { Text("顯示錯題") }
        }
        if (showFavorites || showMistakes) {
            items(list, key = { it.id }) { word ->
                Card {
                    Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(word.english, fontWeight = FontWeight.Bold)
                            Text(word.chinese)
                        }
                        IconButton(onClick = { vm.speak(word.english) }) { Icon(Icons.Default.VolumeUp, null) }
                    }
                }
            }
        }
        item {
            HorizontalDivider()
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("深色模式", Modifier.weight(1f))
                Switch(checked = state.darkMode, onCheckedChange = vm::setDarkMode)
            }
        }
        item {
            OutlinedButton(onClick = vm::resetProgress, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.DeleteSweep, null)
                Spacer(Modifier.width(8.dp))
                Text("清除學習紀錄")
            }
        }
        item {
            Text("版本 4.0.0｜Material 3 全新專案", style = MaterialTheme.typography.bodySmall)
        }
    }
}

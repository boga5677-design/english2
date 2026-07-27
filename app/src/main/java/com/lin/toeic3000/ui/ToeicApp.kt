@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.lin.toeic3000.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lin.toeic3000.AppState
import com.lin.toeic3000.AppViewModel
import com.lin.toeic3000.R
import com.lin.toeic3000.data.Word
import kotlin.random.Random

private enum class Tab(val title: String) {
    HOME("首頁"), WORDS("單字"), QUIZ("測驗"), FAVORITES("收藏"), MORE("更多")
}

private val petPhotos = listOf(
    R.drawable.pet_tortoiseshell_1,
    R.drawable.pet_tortoiseshell_2,
    R.drawable.pet_tortoiseshell_3,
    R.drawable.pet_tabby_1,
    R.drawable.pet_tabby_2,
    R.drawable.pet_tabby_3,
    R.drawable.pet_tabby_4,
    R.drawable.pet_dog_1,
    R.drawable.pet_dog_2
)

@Composable
fun ToeicApp(vm: AppViewModel) {
    val state by vm.state.collectAsStateWithLifecycle()
    var tab by remember { mutableStateOf(Tab.HOME) }

    val light = lightColorScheme(
        primary = androidx.compose.ui.graphics.Color(0xFF8A5A44),
        secondary = androidx.compose.ui.graphics.Color(0xFFDB9D68),
        tertiary = androidx.compose.ui.graphics.Color(0xFF6D8B6B),
        background = androidx.compose.ui.graphics.Color(0xFFFFF8EE),
        surface = androidx.compose.ui.graphics.Color(0xFFFFFBF5)
    )

    MaterialTheme(colorScheme = if (state.darkMode) darkColorScheme() else light) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("TOEIC Pet 3000", fontWeight = FontWeight.ExtraBold) },
                    actions = {
                        IconButton(onClick = { vm.setDarkMode(!state.darkMode) }) {
                            Icon(
                                if (state.darkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = "切換深色模式"
                            )
                        }
                    }
                )
            },
            bottomBar = {
                NavigationBar {
                    val icons = listOf(
                        Icons.Default.Home,
                        Icons.Default.MenuBook,
                        Icons.Default.Quiz,
                        Icons.Default.Star,
                        Icons.Default.MoreHoriz
                    )
                    Tab.entries.forEachIndexed { i, item ->
                        NavigationBarItem(
                            selected = tab == item,
                            onClick = { tab = item },
                            icon = { Icon(icons[i], contentDescription = item.title) },
                            label = { Text(item.title) }
                        )
                    }
                }
            }
        ) { padding ->
            if (state.loading) {
                Box(
                    Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }
            } else {
                Box(Modifier.fillMaxSize().padding(padding)) {
                    when (tab) {
                        Tab.HOME -> HomeScreen(state, vm) { tab = it }
                        Tab.WORDS -> WordListScreen(state, vm)
                        Tab.QUIZ -> QuizScreen(state.words, vm)
                        Tab.FAVORITES -> FavoritesScreen(state, vm)
                        Tab.MORE -> MoreScreen(state, vm)
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeScreen(state: AppState, vm: AppViewModel, navigate: (Tab) -> Unit) {
    val daily = vm.dailyWord()
    val accuracy = if (state.answered == 0) 0 else state.correct * 100 / state.answered
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            PetHero(R.drawable.pet_tortoiseshell_1)
        }
        item {
            Text("今天也一起加油學習吧！🐾", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard("已學單字", "${state.studied.size}", Modifier.weight(1f))
                StatCard("收藏", "${state.favorites.size}", Modifier.weight(1f))
                StatCard("正確率", "$accuracy%", Modifier.weight(1f))
            }
        }
        item {
            Card(shape = RoundedCornerShape(24.dp)) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("學習進度", fontWeight = FontWeight.Bold)
                    LinearProgressIndicator(
                        progress = { state.studied.size.toFloat() / state.words.size.coerceAtLeast(1) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("${state.studied.size} / ${state.words.size} 單字")
                }
            }
        }
        if (daily != null) {
            item {
                Card(shape = RoundedCornerShape(24.dp)) {
                    Row(
                        Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        PetAvatar(R.drawable.pet_dog_2)
                        Column(Modifier.weight(1f)) {
                            Text("今日單字", style = MaterialTheme.typography.labelLarge)
                            Text(daily.english, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
                            Text(daily.chinese)
                        }
                        IconButton(onClick = { vm.speak(daily.english) }) {
                            Icon(Icons.Default.VolumeUp, "播放發音")
                        }
                    }
                }
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ActionButton("單字學習", Icons.Default.MenuBook, Modifier.weight(1f)) { navigate(Tab.WORDS) }
                ActionButton("練習測驗", Icons.Default.Quiz, Modifier.weight(1f)) { navigate(Tab.QUIZ) }
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ActionButton("我的收藏", Icons.Default.Star, Modifier.weight(1f)) { navigate(Tab.FAVORITES) }
                ActionButton("錯題複習", Icons.Default.Replay, Modifier.weight(1f)) { navigate(Tab.MORE) }
            }
        }
    }
}

@Composable
private fun PetHero(@DrawableRes image: Int) {
    Card(shape = RoundedCornerShape(28.dp)) {
        Box(Modifier.fillMaxWidth().height(210.dp)) {
            Image(
                painterResource(image),
                contentDescription = "寵物學習夥伴",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                Modifier.align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.42f))
                    .padding(16.dp)
            ) {
                Text("每天進步一點點，就是最棒的自己！", color = androidx.compose.ui.graphics.Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun StatCard(title: String, value: String, modifier: Modifier) {
    Card(modifier, shape = RoundedCornerShape(18.dp)) {
        Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(title, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun ActionButton(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier, onClick: () -> Unit) {
    ElevatedButton(onClick = onClick, modifier = modifier.height(58.dp), shape = RoundedCornerShape(18.dp)) {
        Icon(icon, null)
        Spacer(Modifier.width(8.dp))
        Text(title)
    }
}

@Composable
private fun WordListScreen(state: AppState, vm: AppViewModel) {
    var query by remember { mutableStateOf("") }
    val filtered = remember(query, state.words) {
        if (query.isBlank()) state.words
        else state.words.filter {
            it.english.contains(query, ignoreCase = true) || it.chinese.contains(query)
        }
    }
    Column(Modifier.fillMaxSize().padding(14.dp)) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("搜尋英文或中文") },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            singleLine = true
        )
        Spacer(Modifier.height(10.dp))
        Text("共 ${filtered.size} 筆", style = MaterialTheme.typography.labelLarge)
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(vertical = 10.dp)) {
            items(filtered.take(500), key = { it.id }) { word ->
                WordCard(word, state.favorites.contains(word.id), vm)
            }
        }
    }
}

@Composable
private fun WordCard(word: Word, favorite: Boolean, vm: AppViewModel) {
    Card(shape = RoundedCornerShape(20.dp)) {
        Row(
            Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PetAvatar(petPhotos[word.id % petPhotos.size])
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(word.english, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(word.chinese)
                Text(word.level, style = MaterialTheme.typography.labelSmall)
            }
            IconButton(onClick = { vm.speak(word.english); vm.markStudied(word.id) }) {
                Icon(Icons.Default.VolumeUp, "播放")
            }
            IconButton(onClick = { vm.toggleFavorite(word.id) }) {
                Icon(if (favorite) Icons.Default.Star else Icons.Default.StarBorder, "收藏")
            }
        }
    }
}

@Composable
private fun PetAvatar(@DrawableRes image: Int) {
    Image(
        painterResource(image),
        contentDescription = null,
        modifier = Modifier.size(54.dp).clip(CircleShape),
        contentScale = ContentScale.Crop
    )
}

@Composable
private fun QuizScreen(words: List<Word>, vm: AppViewModel) {
    var current by remember(words) { mutableStateOf(words.randomOrNull()) }
    var options by remember(current) { mutableStateOf(makeOptions(words, current)) }
    var message by remember { mutableStateOf("選出正確中文意思") }
    var answered by remember { mutableStateOf(false) }

    Column(
        Modifier.fillMaxSize().padding(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        PetAvatar(R.drawable.pet_tabby_3)
        Text("練習測驗", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        current?.let { word ->
            Card(shape = RoundedCornerShape(28.dp), modifier = Modifier.fillMaxWidth()) {
                Column(
                    Modifier.fillMaxWidth().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(word.english, style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.ExtraBold)
                    IconButton(onClick = { vm.speak(word.english) }) { Icon(Icons.Default.VolumeUp, "播放") }
                    Text(message)
                }
            }
            options.forEach { option ->
                OutlinedButton(
                    onClick = {
                        if (!answered) {
                            val correct = option.id == word.id
                            vm.recordAnswer(word, correct)
                            message = if (correct) "答對了！🐾" else "答案是：${word.chinese}"
                            answered = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp)
                ) { Text(option.chinese, modifier = Modifier.padding(7.dp)) }
            }
            Button(
                onClick = {
                    current = words.randomOrNull()
                    options = makeOptions(words, current)
                    message = "選出正確中文意思"
                    answered = false
                },
                enabled = answered
            ) { Text("下一題") }
        }
    }
}

private fun makeOptions(words: List<Word>, current: Word?): List<Word> {
    if (current == null) return emptyList()
    return (words.filter { it.id != current.id }.shuffled().take(3) + current).shuffled()
}

@Composable
private fun FavoritesScreen(state: AppState, vm: AppViewModel) {
    val favorites = state.words.filter { it.id in state.favorites }
    if (favorites.isEmpty()) {
        EmptyPetState(R.drawable.pet_dog_1, "還沒有收藏單字", "點擊單字旁的星星，就能加入收藏。")
    } else {
        LazyColumn(
            contentPadding = PaddingValues(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(favorites, key = { it.id }) { word ->
                WordCard(word, true, vm)
            }
        }
    }
}

@Composable
private fun MoreScreen(state: AppState, vm: AppViewModel) {
    var showMistakes by remember { mutableStateOf(false) }
    val mistakes = state.words.filter { it.id in state.mistakes }
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { PetHero(R.drawable.pet_tabby_4) }
        item {
            Card(shape = RoundedCornerShape(22.dp)) {
                Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.RecordVoiceOver, null)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("英文發音", fontWeight = FontWeight.Bold)
                        Text(if (state.ttsReady) "美式英文語音已就緒" else "手機尚未提供英文語音資料")
                    }
                }
            }
        }
        item {
            ElevatedButton(onClick = { showMistakes = !showMistakes }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Replay, null)
                Spacer(Modifier.width(8.dp))
                Text("錯題本（${mistakes.size}）")
            }
        }
        if (showMistakes) {
            items(mistakes, key = { it.id }) { word -> WordCard(word, state.favorites.contains(word.id), vm) }
        }
        item {
            OutlinedButton(onClick = vm::resetProgress, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.RestartAlt, null)
                Spacer(Modifier.width(8.dp))
                Text("重設學習進度")
            }
        }
        item {
            Text(
                "單字庫目前載入 ${state.words.size} 筆。可直接替換 assets/toeic_words.tsv 擴充。",
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun EmptyPetState(@DrawableRes image: Int, title: String, message: String) {
    Column(
        Modifier.fillMaxSize().padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painterResource(image),
            contentDescription = null,
            modifier = Modifier.size(190.dp).clip(RoundedCornerShape(28.dp)),
            contentScale = ContentScale.Crop
        )
        Spacer(Modifier.height(16.dp))
        Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(message, textAlign = TextAlign.Center)
    }
}

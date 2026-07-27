package com.lin.toeic3000

import android.app.Application
import android.speech.tts.TextToSpeech
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lin.toeic3000.data.Word
import com.lin.toeic3000.data.WordRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.Locale

data class AppState(
    val words: List<Word> = emptyList(),
    val favorites: Set<Int> = emptySet(),
    val mistakes: Set<Int> = emptySet(),
    val studied: Set<Int> = emptySet(),
    val answered: Int = 0,
    val correct: Int = 0,
    val darkMode: Boolean = false,
    val loading: Boolean = true,
    val ttsReady: Boolean = false
)

class AppViewModel(application: Application) :
    AndroidViewModel(application), TextToSpeech.OnInitListener {

    private val prefs = application.getSharedPreferences("toeic_pet_v6", 0)
    private val _state = MutableStateFlow(
        AppState(
            favorites = readSet("favorites"),
            mistakes = readSet("mistakes"),
            studied = readSet("studied"),
            answered = prefs.getInt("answered", 0),
            correct = prefs.getInt("correct", 0),
            darkMode = prefs.getBoolean("dark", false)
        )
    )
    val state: StateFlow<AppState> = _state.asStateFlow()
    private val tts = TextToSpeech(application, this)

    init {
        viewModelScope.launch {
            val loaded = WordRepository.load(application)
            _state.value = _state.value.copy(words = loaded, loading = false)
        }
    }

    override fun onInit(status: Int) {
        val ready = status == TextToSpeech.SUCCESS &&
            tts.setLanguage(Locale.US) >= TextToSpeech.LANG_AVAILABLE
        if (ready) {
            tts.setSpeechRate(0.85f)
            tts.setPitch(1.0f)
        }
        _state.value = _state.value.copy(ttsReady = ready)
    }

    fun speak(text: String) {
        if (_state.value.ttsReady) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "toeic-$text")
        }
    }

    fun dailyWord(): Word? {
        val words = _state.value.words
        if (words.isEmpty()) return null
        val index = (LocalDate.now().toEpochDay() % words.size).toInt()
        return words[index]
    }

    fun toggleFavorite(id: Int) {
        val next = _state.value.favorites.toMutableSet().apply {
            if (!add(id)) remove(id)
        }
        saveSet("favorites", next)
        _state.value = _state.value.copy(favorites = next)
    }

    fun markStudied(id: Int) {
        val next = _state.value.studied + id
        saveSet("studied", next)
        _state.value = _state.value.copy(studied = next)
    }

    fun recordAnswer(word: Word, correctAnswer: Boolean) {
        val studied = _state.value.studied + word.id
        val mistakes = _state.value.mistakes.toMutableSet().apply {
            if (correctAnswer) remove(word.id) else add(word.id)
        }
        val answered = _state.value.answered + 1
        val correct = _state.value.correct + if (correctAnswer) 1 else 0
        saveSet("studied", studied)
        saveSet("mistakes", mistakes)
        prefs.edit().putInt("answered", answered).putInt("correct", correct).apply()
        _state.value = _state.value.copy(
            studied = studied,
            mistakes = mistakes,
            answered = answered,
            correct = correct
        )
    }

    fun setDarkMode(enabled: Boolean) {
        prefs.edit().putBoolean("dark", enabled).apply()
        _state.value = _state.value.copy(darkMode = enabled)
    }

    fun resetProgress() {
        prefs.edit().clear().apply()
        _state.value = _state.value.copy(
            favorites = emptySet(),
            mistakes = emptySet(),
            studied = emptySet(),
            answered = 0,
            correct = 0
        )
    }

    private fun readSet(key: String): Set<Int> =
        prefs.getStringSet(key, emptySet()).orEmpty().mapNotNull { it.toIntOrNull() }.toSet()

    private fun saveSet(key: String, values: Set<Int>) {
        prefs.edit().putStringSet(key, values.map(Int::toString).toSet()).apply()
    }

    override fun onCleared() {
        tts.stop()
        tts.shutdown()
        super.onCleared()
    }
}

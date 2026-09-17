package com.pp.Quickcalc.ui.game

import androidx.lifecycle.SavedStateHandle
import com.pp.Quickcalc.data.GameRepository
import com.pp.Quickcalc.data.HighScoreRepository
import com.pp.Quickcalc.model.Difficulty
import com.pp.Quickcalc.model.Feedback
import com.pp.Quickcalc.model.GameEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GameViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private fun createViewModel(
        repo: GameRepository = HighScoreRepository(),
        difficulty: Difficulty = Difficulty.EASY
    ): GameViewModel {
        val handle = SavedStateHandle(mapOf("difficulty" to difficulty.name))
        return GameViewModel(repo = repo, feedbackManager = null, savedStateHandle = handle)
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialState_loadsRoundAndDifficulty() = runTest {
        val repo = HighScoreRepository()

        val viewModel = createViewModel(repo = repo, difficulty = Difficulty.EASY)
        val state = viewModel.uiState.value

        assertEquals(0, state.chain)
        assertEquals(Difficulty.EASY, state.difficulty)
        assertFalse(state.isGameOver)
    }

    @Test
    fun handleCorrectAnswer_incrementsChainScoreAndUpdatesRound() = runTest {
        val viewModel = createViewModel(difficulty = Difficulty.EASY)
        val initialRound = viewModel.uiState.value.round
        val correct = initialRound.correctAnswer

        viewModel.onEvent(GameEvent.OptionSelected(correct))

        val state = viewModel.uiState.value
        assertEquals(1, state.chain)
        assertTrue(state.score > 0)
        assertEquals(Feedback.CORRECT, state.feedback)
        assertFalse(state.isGameOver)
    }

    @Test
    fun handleWrongAnswer_deductsHeartAndShowsModal() = runTest {
        val viewModel = createViewModel(difficulty = Difficulty.EASY)
        val initialRound = viewModel.uiState.value.round
        val wrong = initialRound.options.first { it != initialRound.correctAnswer }

        viewModel.onEvent(GameEvent.OptionSelected(wrong))

        val state = viewModel.uiState.value
        assertEquals(Feedback.WRONG, state.feedback)
        assertEquals(2, state.hearts)
        assertTrue(state.showWrongAnswerModal)
        assertFalse(state.isGameOver)
    }

    @Test
    fun handleWrongAnswerWhenNoHearts_showsOutofLivesModal() = runTest {
        val viewModel = createViewModel(difficulty = Difficulty.EASY)
        val wrong1 = viewModel.uiState.value.round.options.first { it != viewModel.uiState.value.round.correctAnswer }
        viewModel.onEvent(GameEvent.OptionSelected(wrong1)) // 1st wrong -> 2 hearts
        viewModel.onEvent(GameEvent.WrongAnswerContinueClicked)

        val wrong2 = viewModel.uiState.value.round.options.first { it != viewModel.uiState.value.round.correctAnswer }
        viewModel.onEvent(GameEvent.OptionSelected(wrong2)) // 2nd wrong -> 1 heart
        viewModel.onEvent(GameEvent.WrongAnswerContinueClicked)

        val wrong3 = viewModel.uiState.value.round.options.first { it != viewModel.uiState.value.round.correctAnswer }
        viewModel.onEvent(GameEvent.OptionSelected(wrong3)) // 3rd wrong -> 0 hearts -> out of lives modal

        val state = viewModel.uiState.value
        assertTrue(state.showOutofLivesModal)
    }

    @Test
    fun watchAdForHeart_resetsModalAndDoesNotAddHeart() = runTest {
        val viewModel = createViewModel(difficulty = Difficulty.EASY)
        val wrong1 = viewModel.uiState.value.round.options.first { it != viewModel.uiState.value.round.correctAnswer }
        viewModel.onEvent(GameEvent.OptionSelected(wrong1))
        viewModel.onEvent(GameEvent.WrongAnswerContinueClicked)

        val wrong2 = viewModel.uiState.value.round.options.first { it != viewModel.uiState.value.round.correctAnswer }
        viewModel.onEvent(GameEvent.OptionSelected(wrong2))
        viewModel.onEvent(GameEvent.WrongAnswerContinueClicked)

        val wrong3 = viewModel.uiState.value.round.options.first { it != viewModel.uiState.value.round.correctAnswer }
        viewModel.onEvent(GameEvent.OptionSelected(wrong3))

        assertEquals(0, viewModel.uiState.value.hearts)
        assertTrue(viewModel.uiState.value.showOutofLivesModal)

        viewModel.onEvent(GameEvent.WatchAdForHeartClicked)

        val updatedState = viewModel.uiState.value
        assertFalse(updatedState.showOutofLivesModal)
        assertEquals(0, updatedState.hearts)
        assertEquals(null, updatedState.feedback)
    }

    @Test
    fun timeUpEvent_showsTimeOutModal() = runTest {
        val viewModel = createViewModel(difficulty = Difficulty.EASY)

        viewModel.onEvent(GameEvent.TimeUp)

        val state = viewModel.uiState.value
        assertTrue(state.showTimeOutModal)
        assertEquals(null, state.feedback)
    }

    @Test
    fun restartGame_resetsChainAndState() = runTest {
        val viewModel = createViewModel(difficulty = Difficulty.EASY)
        val correct = viewModel.uiState.value.round.correctAnswer

        viewModel.onEvent(GameEvent.OptionSelected(correct))
        assertEquals(1, viewModel.uiState.value.chain)

        viewModel.restartGame()

        val state = viewModel.uiState.value
        assertEquals(0, state.chain)
        assertEquals(3, state.hearts)
        assertEquals(0, state.score)
        assertEquals(1, state.currentLevel)
        assertEquals(1, state.currentQuestion)
        assertFalse(state.isGameOver)
    }

    @Test
    fun handle20Questions_advancesToLevel2Question1() = runTest {
        val repo = HighScoreRepository()
        val viewModel = createViewModel(repo = repo, difficulty = Difficulty.EASY)

        assertEquals(1, viewModel.uiState.value.currentLevel)
        assertEquals(1, viewModel.uiState.value.currentQuestion)

        // Answer 19 questions correctly -> level 1, question 20
        repeat(19) {
            val correct = viewModel.uiState.value.round.correctAnswer
            viewModel.onEvent(GameEvent.OptionSelected(correct))
        }

        assertEquals(1, viewModel.uiState.value.currentLevel)
        assertEquals(20, viewModel.uiState.value.currentQuestion)

        // Answer 20th question correctly -> level 2, question 1
        val correct20 = viewModel.uiState.value.round.correctAnswer
        viewModel.onEvent(GameEvent.OptionSelected(correct20))

        assertEquals(2, viewModel.uiState.value.currentLevel)
        assertEquals(1, viewModel.uiState.value.currentQuestion)
    }

    @Test
    fun perDifficultyProgress_independentPerDifficulty() = runTest {
        val repo = HighScoreRepository()

        // Advance EASY to Level 4, Question 2
        repo.saveProgress(Difficulty.EASY, level = 4, question = 2)

        // Create ViewModel for MEDIUM -> starts at Level 1, Question 1
        val mediumVm = createViewModel(repo = repo, difficulty = Difficulty.MEDIUM)
        assertEquals(1, mediumVm.uiState.value.currentLevel)
        assertEquals(1, mediumVm.uiState.value.currentQuestion)

        // Create ViewModel for EASY -> resumes at Level 4, Question 2
        val easyVm = createViewModel(repo = repo, difficulty = Difficulty.EASY)
        assertEquals(4, easyVm.uiState.value.currentLevel)
        assertEquals(2, easyVm.uiState.value.currentQuestion)
    }
}

const state = {question: null, expectedAnswer: null, questionNumber: 1};
let totalQuestions = 0;
const questionPanel = document.querySelector('#question-panel');
const answerInput = document.querySelector('#answer-input');
const submitButton = document.querySelector('#submit-btn');
const evaluationPanel = document.querySelector('#evaluation-panel');
const progressLabel = document.querySelector('#progress-label');
const progressBar = document.querySelector('#progress-bar');
const toast = document.querySelector('#toast');
const historyList = document.querySelector('#history-list');
const historyCount = document.querySelector('#history-count');
const scoreSummary = document.querySelector('#score-summary');
const SESSION_STORAGE_KEY = 'quiz-agent-session';
const session = loadOrCreateSession();
const history = session.history;

function loadOrCreateSession() {
  try {
    const stored = localStorage.getItem(SESSION_STORAGE_KEY);
    if (stored) {
      const parsed = JSON.parse(stored);
      if (parsed && typeof parsed.id === 'string' && Array.isArray(parsed.history)) {
        return parsed;
      }
    }
  } catch (error) {
    console.warn('Could not restore the quiz session.', error);
  }

  const newSession = {id: crypto.randomUUID(), history: []};
  persistSession(newSession);
  return newSession;
}

function persistSession(value = session) {
  try {
    localStorage.setItem(SESSION_STORAGE_KEY, JSON.stringify(value));
  } catch (error) {
    console.warn('Could not persist the quiz session.', error);
  }
}

function sessionHeaders() {
  return {'X-Quiz-Session-Id': session.id};
}

async function loadQuestion() {
  setBusy(true);
  try {
    const response = await fetch('/api/quiz/question', {headers: sessionHeaders()});
    if (response.status === 204) {
      throw new Error(
          'No questions are available yet.');
    }
    if (!response.ok) {
      throw new Error('Could not load the next question.');
    }
    const data = await response.json();
    state.question = data.question;
    state.expectedAnswer = data.expectedAnswer;
    totalQuestions = data.totalQuestions;
    questionPanel.innerHTML = `<p class="question-text">${escapeHtml(
        data.question)}</p>`;
    document.querySelector(
        '#question-tag').textContent = `QUESTION ${state.questionNumber}`;
    progressLabel.textContent = `Question ${state.questionNumber} of ${totalQuestions}`;
    progressBar.style.width = `${Math.min((state.questionNumber / totalQuestions) * 100, 100)}%`;
    answerInput.value = '';
    evaluationPanel.classList.add('d-none');
  } catch (error) {
    showToast(error.message);
    questionPanel.innerHTML = `<p class="question-text">Your next question is waiting for you.</p>`;
  }
  setBusy(false);
}

document.querySelector('#answer-form').addEventListener('submit',
    async (event) => {
      event.preventDefault();
      if (!answerInput.value.trim() || !state.question) {
        return;
      }
      submitButton.disabled = true;
      submitButton.innerHTML = 'Analyzing <span>…</span>';
      try {
        const response = await fetch('/api/quiz/evaluate', {
          method: 'POST',
          headers: {'Content-Type': 'application/json', ...sessionHeaders()},
          body: JSON.stringify({
            question: state.question,
            expectedAnswer: state.expectedAnswer || '',
            answer: answerInput.value.trim()
          })
        });
        if (!response.ok) {
          throw new Error(
              'The evaluation could not be completed.');
        }
        const result = await response.json();
        renderEvaluation(result);
        addHistoryEntry(answerInput.value.trim(), result);
      } catch (error) {
        showToast(error.message);
      }
      submitButton.disabled = false;
      submitButton.innerHTML = 'Get evaluation <span>↗</span>';
    });

document.querySelector('#finish-btn').addEventListener('click', () => {
  clearSessionHistory();
  state.questionNumber = 1;
  state.question = null;
  questionPanel.innerHTML = '<p class="question-text">Session complete. Ready for another round?</p>';
  answerInput.value = '';
  setBusy(true);
  document.querySelector('#question-tag').textContent = 'FINISHED';
  progressLabel.textContent = 'Session complete';
  progressBar.style.width = '100%';
  evaluationPanel.classList.add('d-none');
  document.querySelector('#finish-btn').textContent = 'Start new session  →';
  document.querySelector('#finish-btn').onclick = () => {
    state.questionNumber = 1;
    document.querySelector('#finish-btn').textContent = 'Finish session  →';
    loadQuestion();
  };
});

function clearSessionHistory() {
  history.length = 0;
  localStorage.removeItem(SESSION_STORAGE_KEY);
  session.id = crypto.randomUUID();
  persistSession();
  historyList.innerHTML = '<div id="history-empty" class="history-empty">Your completed answers will appear here.</div>';
  historyCount.textContent = '0 answered';
  renderScore();
}

function renderEvaluation(result) {
  const tips = (result.tips || []).map(
      item => `<li>${escapeHtml(item)}</li>`).join('');
  const enhancements = (result.enhancementPoints || []).map(
      item => `<li>${escapeHtml(item)}</li>`).join('');
  evaluationPanel.innerHTML = `<div class="evaluation-result"><div class="result-icon">${result.isCorrect
      ? '✓' : '!'}</div><div><h3>${result.isCorrect ? 'Correct answer'
      : 'Answer needs review'}</h3><p>${escapeHtml(
      result.scoreJustification || 'Your answer has been reviewed.')}</p>${tips
      ? `<div class="section-label mt-3 mb-2">TIPS</div><ul>${tips}</ul>`
      : ''}${enhancements
      ? `<div class="section-label mt-3 mb-2">LEVEL UP</div><ul>${enhancements}</ul>`
      : ''}</div></div><button id="next-btn" class="btn btn-outline-light mt-4">Next question <span>→</span></button>`;
  evaluationPanel.classList.remove('d-none');
  document.querySelector('#next-btn').onclick = () => {
    state.questionNumber += 1;
    loadQuestion();
  };
}

function addHistoryEntry(answer, result) {
  const entry = {
    question: state.question,
    correctAnswer: state.expectedAnswer,
    answer,
    result
  };
  history.push(entry);
  persistSession();
  document.querySelector('#history-empty')?.remove();
  historyCount.textContent = `${history.length} answered`;
  renderScore();
  const item = document.createElement('details');
  item.className = 'history-item';
  item.open = true;
  const tips = (result.tips || []).map(item => `<li>${escapeHtml(item)}</li>`).join('');
  const enhancements = (result.enhancementPoints || []).map(item => `<li>${escapeHtml(item)}</li>`).join('');
  item.innerHTML = `<summary><div class="history-summary"><span class="history-number">${history.length}</span><span class="history-question">${escapeHtml(state.question)}</span><span class="history-result ${result.isCorrect ? '' : 'incorrect'}">${result.isCorrect ? 'Correct' : 'Review'}</span></div></summary><div class="history-body"><div class="history-label">Your answer</div><p class="history-answer">${escapeHtml(answer)}</p><div class="history-label">Correct answer</div><p class="history-answer">${escapeHtml(entry.correctAnswer || 'Not provided.')}</p><div class="history-label">Agent evaluation</div><p class="history-justification">${escapeHtml(result.scoreJustification || 'No justification provided.')}</p>${tips ? `<div class="history-label">Tips</div><ul>${tips}</ul>` : ''}${enhancements ? `<div class="history-label mt-3">Enhancement points</div><ul>${enhancements}</ul>` : ''}</div>`;
  historyList.prepend(item);
}

function renderScore() {
  const correct = history.filter(entry => entry.result?.isCorrect).length;
  const incorrect = history.length - correct;
  const percentage = history.length ? Math.round((correct / history.length) * 100) : 0;
  scoreSummary.textContent = `${correct} correct · ${incorrect} incorrect · ${percentage}%`;
}

function restoreHistory() {
  const savedHistory = [...history];
  history.length = 0;
  savedHistory.forEach(entry => {
    const question = state.question;
    const expectedAnswer = state.expectedAnswer;
    state.question = entry.question;
    state.expectedAnswer = entry.correctAnswer;
    addHistoryEntry(entry.answer, entry.result);
    state.question = question;
    state.expectedAnswer = expectedAnswer;
  });
}

function setBusy(busy) {
  answerInput.disabled = busy;
  submitButton.disabled = busy;
}

function showToast(message) {
  toast.textContent = message;
  toast.classList.add('show');
  setTimeout(() => toast.classList.remove('show'), 3500);
}

function escapeHtml(value) {
  return String(value).replace(/[&<>'"]/g, char => ({
    '&': '&amp;',
    '<': '&lt;',
    '>': '&gt;',
    "'": '&#39;',
    '"': '&quot;'
  }[char]));
}

restoreHistory();
renderScore();
loadQuestion();

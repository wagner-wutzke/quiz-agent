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
const SESSION_ID_KEY = 'quiz-agent-session-id';
const session = {id: null, active: false};
const sessionButton = document.querySelector('#session-btn');
const interviewTopicLabel = document.querySelector('#interview-topic');
let history = [];

function sessionHeaders() {
  return {'X-Quiz-Session-Id': session.id};
}

sessionButton.addEventListener('click', async () => {
  if (session.active) {
    await finishSession();
  } else {
    await startSession();
  }
});

async function startSession() {
  session.id = crypto.randomUUID();
  sessionStorage.setItem(SESSION_ID_KEY, session.id);
  session.active = true;
  state.questionNumber = 1;
  history = [];
  renderHistory();
  sessionButton.disabled = true;
  sessionButton.textContent = 'Finish session  →';
  await loadQuestion();
  sessionButton.disabled = false;
}

async function loadQuestion() {
  if (!session.active) {
    return;
  }
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

async function loadHistory() {
  const requestedSessionId = session.id;
  try {
    if (!session.active) {
      history = [];
      renderHistory();
      return;
    }
    const response = await fetch('/api/quiz/history', {headers: sessionHeaders()});
    if (!response.ok) {
      throw new Error('Could not load answer history.');
    }
    const records = await response.json();
    if (!session.active || requestedSessionId !== session.id) {
      return;
    }
    history = records;
    renderHistory();
  } catch (error) {
    showToast(error.message);
  }
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
        await loadHistory();
      } catch (error) {
        showToast(error.message);
      }
      submitButton.disabled = false;
      submitButton.innerHTML = 'Get evaluation <span>↗</span>';
    });

async function finishSession() {
  sessionButton.disabled = true;
  try {
    const response = await fetch('/api/quiz/history', {
      method: 'DELETE',
      headers: sessionHeaders()
    });
    if (!response.ok) {
      throw new Error('Could not finish the session.');
    }
  } catch (error) {
    showToast(error.message);
    sessionButton.disabled = false;
    return;
  }

  session.active = false;
  sessionStorage.removeItem(SESSION_ID_KEY);
  session.id = null;
  history = [];
  state.questionNumber = 1;
  state.question = null;
  state.expectedAnswer = null;
  questionPanel.innerHTML = '<p class="question-text">Start a session to see your first question.</p>';
  answerInput.value = '';
  setBusy(true);
  document.querySelector('#question-tag').textContent = 'NOT STARTED';
  progressLabel.textContent = 'No active session';
  progressBar.style.width = '0%';
  evaluationPanel.classList.add('d-none');
  sessionButton.textContent = 'Start session  →';
  sessionButton.disabled = false;
  renderHistory();
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

function renderHistory() {
  historyList.innerHTML = '';
  if (!history.length) {
    historyList.innerHTML = '<div id="history-empty" class="history-empty">Your completed answers will appear here.</div>';
  }
  historyCount.textContent = `${history.length} answered`;
  renderScore();
  [...history].reverse().forEach((entry, index) => {
    const result = entry.result || entry;
    const item = document.createElement('details');
    item.className = 'history-item';
    item.open = index === 0;
    const tips = (result.tips || []).map(value => `<li>${escapeHtml(value)}</li>`).join('');
    const enhancements = (result.enhancementPoints || []).map(value => `<li>${escapeHtml(value)}</li>`).join('');
    item.innerHTML = `<summary><div class="history-summary"><span class="history-number">${history.length - index}</span><span class="history-question">${escapeHtml(entry.question)}</span><span class="history-result ${result.isCorrect ? '' : 'incorrect'}">${result.isCorrect ? 'Correct' : 'Review'}</span></div></summary><div class="history-body"><div class="history-label">Your answer</div><p class="history-answer">${escapeHtml(entry.answer)}</p><div class="history-label">Correct answer</div><p class="history-answer">${escapeHtml(entry.expectedAnswer || 'Not provided.')}</p><div class="history-label">Agent evaluation</div><p class="history-justification">${escapeHtml(result.scoreJustification || 'No justification provided.')}</p>${tips ? `<div class="history-label">Tips</div><ul>${tips}</ul>` : ''}${enhancements ? `<div class="history-label mt-3">Enhancement points</div><ul>${enhancements}</ul>` : ''}</div>`;
    historyList.append(item);
  });
}

function renderScore() {
  const correct = history.filter(entry => (entry.result || entry).isCorrect).length;
  const incorrect = history.length - correct;
  const percentage = history.length ? Math.round((correct / history.length) * 100) : 0;
  scoreSummary.textContent = `${correct} correct · ${incorrect} incorrect · ${percentage}%`;
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

document.querySelector('#question-tag').textContent = 'NOT STARTED';
progressLabel.textContent = 'No active session';
setBusy(true);
renderHistory();
loadInterviewTopic();

async function loadInterviewTopic() {
  try {
    const response = await fetch('/api/quiz/topic');
    if (!response.ok) {
      throw new Error('Could not load the interview topic.');
    }
    const data = await response.json();
    interviewTopicLabel.textContent = `Interview practice / ${data.interviewTopic || 'Java'}`;
  } catch (error) {
    interviewTopicLabel.textContent = 'Interview practice / Java';
    console.warn(error.message);
  }
}

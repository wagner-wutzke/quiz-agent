const SESSION_ID_KEY = "quiz-agent-session-id";
const API = {
  question: "/api/quiz/question",
  evaluation: "/api/quiz/evaluate",
  history: "/api/quiz/history",
  topic: "/api/quiz/topic",
};
const LABELS = {
  startSession: "Start session  →",
  finishSession: "Finish session  →",
  evaluate: "Get evaluation <span>↗</span>",
  evaluating: "Analyzing <span>…</span>",
  evaluated: "Evaluated <span>✓</span>",
};

const state = {
  question: null,
  expectedAnswer: null,
  questionNumber: 1,
  totalQuestions: 0,
  sessionId: null,
  sessionActive: false,
  history: [],
};

const elements = {
  answerForm: document.querySelector("#answer-form"),
  answerInput: document.querySelector("#answer-input"),
  cancelFinishButton: document.querySelector("#cancel-finish-btn"),
  confirmFinishButton: document.querySelector("#confirm-finish-btn"),
  evaluationPanel: document.querySelector("#evaluation-panel"),
  finishModal: document.querySelector("#finish-modal"),
  historyCount: document.querySelector("#history-count"),
  historyList: document.querySelector("#history-list"),
  interviewTopic: document.querySelector("#interview-topic"),
  progressBar: document.querySelector("#progress-bar"),
  progressLabel: document.querySelector("#progress-label"),
  questionPanel: document.querySelector("#question-panel"),
  questionTag: document.querySelector("#question-tag"),
  scoreSummary: document.querySelector("#score-summary"),
  sessionButton: document.querySelector("#session-btn"),
  submitButton: document.querySelector("#submit-btn"),
  toast: document.querySelector("#toast"),
};

function sessionHeaders() {
  return { "X-Quiz-Session-Id": state.sessionId };
}

function setAnswerFormBusy(isBusy) {
  elements.answerInput.disabled = isBusy;
  elements.submitButton.disabled = isBusy;
}

function showToast(message) {
  elements.toast.textContent = message;
  elements.toast.classList.add("show");
  setTimeout(() => elements.toast.classList.remove("show"), 3500);
}

function escapeHtml(value) {
  const htmlEscapes = {
    "&": "&amp;",
    "<": "&lt;",
    ">": "&gt;",
    "'": "&#39;",
    '"': "&quot;",
  };

  return String(value).replace(
    /[&<>'"]/g,
    (character) => htmlEscapes[character],
  );
}

async function requestJson(url, options = {}) {
  const { errorMessage, ...requestOptions } = options;
  const response = await fetch(url, requestOptions);
  if (!response.ok) {
    throw new Error(errorMessage || "The request could not be completed.");
  }
  return response.json();
}

function openFinishModal() {
  elements.finishModal.classList.remove("d-none");
  elements.confirmFinishButton.focus();
}

function closeFinishModal() {
  elements.finishModal.classList.add("d-none");
  elements.sessionButton.focus();
}

async function handleSessionButtonClick() {
  if (state.sessionActive) {
    openFinishModal();
    return;
  }

  await startSession();
}

function setupEventHandlers() {
  elements.sessionButton.addEventListener("click", handleSessionButtonClick);
  elements.cancelFinishButton.addEventListener("click", closeFinishModal);
  elements.confirmFinishButton.addEventListener("click", async () => {
    closeFinishModal();
    await finishSession();
  });
  elements.finishModal.addEventListener("click", (event) => {
    if (event.target === elements.finishModal) {
      closeFinishModal();
    }
  });
  document.addEventListener("keydown", (event) => {
    const modalIsOpen = !elements.finishModal.classList.contains("d-none");
    if (event.key === "Escape" && modalIsOpen) {
      closeFinishModal();
    }
  });
  elements.answerForm.addEventListener("submit", handleAnswerSubmit);
}

async function startSession() {
  state.sessionId = crypto.randomUUID();
  state.sessionActive = true;
  state.questionNumber = 1;
  state.history = [];
  sessionStorage.setItem(SESSION_ID_KEY, state.sessionId);

  renderHistory();
  elements.sessionButton.disabled = true;
  elements.sessionButton.textContent = LABELS.finishSession;
  await loadQuestion();
  elements.sessionButton.disabled = false;
}

async function loadQuestion() {
  if (!state.sessionActive) {
    return;
  }

  setAnswerFormBusy(true);
  try {
    const response = await fetch(API.question, { headers: sessionHeaders() });
    if (response.status === 204) {
      throw new Error("No questions are available yet.");
    }
    if (!response.ok) {
      throw new Error("Could not load the next question.");
    }

    const question = await response.json();
    displayQuestion(question);
  } catch (error) {
    showToast(error.message);
    elements.questionPanel.innerHTML =
      '<p class="question-text">Your next question is waiting for you.</p>';
  } finally {
    setAnswerFormBusy(false);
  }
}

function displayQuestion(question) {
  state.question = question.question;
  state.expectedAnswer = question.expectedAnswer;
  state.totalQuestions = question.totalQuestions;

  elements.questionPanel.innerHTML = `<p class="question-text">${escapeHtml(question.question)}</p>`;
  elements.questionTag.textContent = `QUESTION ${state.questionNumber}`;
  elements.progressLabel.textContent = `Question ${state.questionNumber} of ${state.totalQuestions}`;

  const progress = Math.min(
    (state.questionNumber / state.totalQuestions) * 100,
    100,
  );
  elements.progressBar.style.width = `${progress}%`;
  elements.answerInput.value = "";
  elements.submitButton.innerHTML = LABELS.evaluate;
  elements.evaluationPanel.classList.add("d-none");
}

async function loadHistory() {
  if (!state.sessionActive) {
    state.history = [];
    renderHistory();
    return;
  }

  const requestedSessionId = state.sessionId;
  try {
    const records = await requestJson(API.history, {
      headers: sessionHeaders(),
      errorMessage: "Could not load answer history.",
    });

    // Ignore a response if the user ended or replaced this session meanwhile.
    if (!state.sessionActive || requestedSessionId !== state.sessionId) {
      return;
    }

    state.history = records;
    renderHistory();
  } catch (error) {
    showToast(error.message);
  }
}

async function handleAnswerSubmit(event) {
  event.preventDefault();
  const answer = elements.answerInput.value.trim();
  if (!answer || !state.question) {
    return;
  }

  elements.submitButton.disabled = true;
  elements.submitButton.innerHTML = LABELS.evaluating;

  try {
    const evaluation = await requestJson(API.evaluation, {
      method: "POST",
      headers: { "Content-Type": "application/json", ...sessionHeaders() },
      body: JSON.stringify({
        question: state.question,
        expectedAnswer: state.expectedAnswer || "",
        answer,
      }),
      errorMessage: "The evaluation could not be completed.",
    });

    renderEvaluation(evaluation);
    await loadHistory();
    elements.submitButton.disabled = true;
    elements.submitButton.innerHTML = LABELS.evaluated;
  } catch (error) {
    showToast(error.message);
    elements.submitButton.disabled = false;
    elements.submitButton.innerHTML = LABELS.evaluate;
  }
}

async function finishSession() {
  elements.sessionButton.disabled = true;

  try {
    const response = await fetch(API.history, {
      method: "DELETE",
      headers: sessionHeaders(),
    });
    if (!response.ok) {
      throw new Error("Could not finish the session.");
    }
  } catch (error) {
    showToast(error.message);
    elements.sessionButton.disabled = false;
    return;
  }

  resetSession();
}

function resetSession() {
  state.sessionActive = false;
  state.sessionId = null;
  state.questionNumber = 1;
  state.question = null;
  state.expectedAnswer = null;
  state.history = [];
  sessionStorage.removeItem(SESSION_ID_KEY);

  elements.questionPanel.innerHTML =
    '<p class="question-text">Start a session to see your first question.</p>';
  elements.answerInput.value = "";
  setAnswerFormBusy(true);
  elements.questionTag.textContent = "NOT STARTED";
  elements.progressLabel.textContent = "No active session";
  elements.progressBar.style.width = "0%";
  elements.evaluationPanel.classList.add("d-none");
  elements.sessionButton.textContent = LABELS.startSession;
  elements.sessionButton.disabled = false;
  renderHistory();
}

function renderEvaluation(evaluation) {
  const tips = renderList(evaluation.tips);
  const enhancements = renderList(evaluation.enhancementPoints);
  const title = evaluation.isCorrect ? "Correct answer" : "Answer needs review";
  const icon = evaluation.isCorrect ? "✓" : "!";
  const justification = escapeHtml(
    evaluation.scoreJustification || "Your answer has been reviewed.",
  );

  elements.evaluationPanel.innerHTML = `
    <div class="evaluation-result">
      <div class="result-icon">${icon}</div>
      <div>
        <h3>${title}</h3>
        <p>${justification}</p>
        ${tips ? `<div class="section-label mt-3 mb-2">TIPS</div><ul>${tips}</ul>` : ""}
        ${enhancements ? `<div class="section-label mt-3 mb-2">LEVEL UP</div><ul>${enhancements}</ul>` : ""}
      </div>
    </div>
    <button id="next-btn" class="btn btn-outline-light mt-4" type="button">
      Next question <span>→</span>
    </button>`;

  elements.evaluationPanel.classList.remove("d-none");
  document.querySelector("#next-btn").addEventListener("click", () => {
    state.questionNumber += 1;
    loadQuestion();
  });
}

function renderList(items = []) {
  return (items || []).map((item) => `<li>${escapeHtml(item)}</li>`).join("");
}

function renderHistory() {
  elements.historyList.innerHTML = "";
  if (state.history.length === 0) {
    elements.historyList.innerHTML =
      '<div id="history-empty" class="history-empty">Your completed answers will appear here.</div>';
  }

  elements.historyCount.textContent = `${state.history.length} answered`;
  renderScore();

  [...state.history].reverse().forEach((entry, index) => {
    elements.historyList.append(createHistoryItem(entry, index));
  });
}

function createHistoryItem(entry, index) {
  const evaluation = entry.result || entry;
  const item = document.createElement("details");
  item.className = "history-item";
  item.open = false;
  item.innerHTML = `
    <summary>
      <div class="history-summary">
        <span class="history-number">${state.history.length - index}</span>
        <span class="history-question">${escapeHtml(entry.question)}</span>
        <span class="history-result ${evaluation.isCorrect ? "" : "incorrect"}">
          ${evaluation.isCorrect ? "Correct" : "Review"}
        </span>
      </div>
    </summary>
    <div class="history-body">
      <div class="history-label">Your answer</div>
      <p class="history-answer">${escapeHtml(entry.answer)}</p>
      <div class="history-label">Correct answer</div>
      <p class="history-answer">${escapeHtml(entry.expectedAnswer || "Not provided.")}</p>
      <div class="history-label">Agent evaluation</div>
      <p class="history-justification">${escapeHtml(evaluation.scoreJustification || "No justification provided.")}</p>
      ${renderHistorySection("Tips", evaluation.tips)}
      ${renderHistorySection("Enhancement points", evaluation.enhancementPoints)}
    </div>`;

  return item;
}

function renderHistorySection(title, items = []) {
  const list = renderList(items);
  if (!list) {
    return "";
  }

  const spacing = title === "Enhancement points" ? " mt-3" : "";
  return `<div class="history-label${spacing}">${title}</div><ul>${list}</ul>`;
}

function renderScore() {
  const correctAnswers = state.history.filter(
    (entry) => (entry.result || entry).isCorrect,
  ).length;
  const incorrectAnswers = state.history.length - correctAnswers;
  const percentage = state.history.length
    ? Math.round((correctAnswers / state.history.length) * 100)
    : 0;

  elements.scoreSummary.textContent = `${correctAnswers} correct · ${incorrectAnswers} incorrect · ${percentage}%`;
}

async function loadInterviewTopic() {
  try {
    const topic = await requestJson(API.topic, {
      errorMessage: "Could not load the interview topic.",
    });
    elements.interviewTopic.textContent = `Interview practice / ${topic.interviewTopic || "Java"}`;
  } catch (error) {
    elements.interviewTopic.textContent = "Interview practice / Java";
    console.warn(error.message);
  }
}

function initializePage() {
  elements.questionTag.textContent = "NOT STARTED";
  elements.progressLabel.textContent = "No active session";
  setAnswerFormBusy(true);
  renderHistory();
  setupEventHandlers();
  loadInterviewTopic();
}

initializePage();

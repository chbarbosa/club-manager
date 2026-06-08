import api from './axios.js'

export function createEvaluation(data) {
  return api.post('/evaluations', data).then((response) => response.data)
}

export function getAllEvaluations(params = {}) {
  return api.get('/evaluations', { params }).then((response) => response.data)
}

export function getEvaluation(uuid) {
  return api.get(`/evaluations/${uuid}`).then((response) => response.data)
}

export function getEvaluationResults(uuid) {
  return api.get(`/evaluations/${uuid}/results`).then((response) => response.data)
}

export function updateEvaluationResult(evaluationUuid, playerUuid, data) {
  return api.put(`/evaluations/${evaluationUuid}/results/${playerUuid}`, data).then((response) => response.data)
}

export function updateEvaluation(uuid, data) {
  return api.put(`/evaluations/${uuid}`, data).then((response) => response.data)
}

export function startEvaluation(uuid) {
  return api.patch(`/evaluations/${uuid}/start`).then((response) => response.data)
}

export function finalizeEvaluation(uuid) {
  return api.patch(`/evaluations/${uuid}/finalize`).then((response) => response.data)
}

export function getEvaluationPlayers(evaluationUuid) {
  return api.get(`/evaluations/${evaluationUuid}/players`).then((response) => response.data)
}

export function assignEvaluationPlayer(evaluationUuid, playerUuid) {
  return api.post(`/evaluations/${evaluationUuid}/players`, { playerUuid }).then((response) => response.data)
}

export function removeEvaluationPlayer(evaluationUuid, assignmentUuid) {
  return api.delete(`/evaluations/${evaluationUuid}/players/${assignmentUuid}`).then((response) => response.data)
}

export function getEvaluationEvents(evaluationUuid) {
  return api.get(`/evaluations/${evaluationUuid}/events`).then((response) => response.data)
}

export function createEvaluationEvent(evaluationUuid, data) {
  return api.post(`/evaluations/${evaluationUuid}/events`, data).then((response) => response.data)
}

export function getEventAttendance(eventUuid) {
  return api.get(`/evaluation-events/${eventUuid}/attendance`).then((response) => response.data)
}

export function updateEventAttendance(eventUuid, playerUuid, data) {
  return api.put(`/evaluation-events/${eventUuid}/attendance/${playerUuid}`, data).then((response) => response.data)
}

export function completeEvaluationEvent(eventUuid) {
  return api.patch(`/evaluation-events/${eventUuid}/complete`).then((response) => response.data)
}

export function cancelEvaluationEvent(eventUuid, cancelReason = '') {
  return api.patch(`/evaluation-events/${eventUuid}/cancel`, { cancelReason }).then((response) => response.data)
}

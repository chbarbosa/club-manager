import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { getClubAnalysis } from '../api/clubAnalysis.js'
import { AnalysisItems, AnalysisSummary } from './ClubAnalysisPage.jsx'

export default function ClubAnalysisDetailPage() {
  const { uuid } = useParams()
  const [analysis, setAnalysis] = useState(null)
  const [error, setError] = useState('')

  useEffect(() => {
    loadAnalysis()
  }, [uuid])

  async function loadAnalysis() {
    setError('')
    try {
      setAnalysis(await getClubAnalysis(uuid))
    } catch (requestError) {
      setError(requestError.response?.data?.message ?? requestError.message ?? 'Unable to load club analysis detail.')
    }
  }

  return (
    <main className="container py-5">
      <Link to="/club-analysis">&larr; Back to club analysis</Link>
      <h1 className="mt-3">Club Analysis Detail</h1>
      <p className="text-muted">Persisted snapshot. This page does not recalculate findings.</p>

      {error && <p className="alert alert-danger">{error}</p>}
      {!analysis && !error && <p>Loading club analysis detail...</p>}
      {analysis && (
        <>
          <AnalysisSummary analysis={analysis} />
          <AnalysisItems items={analysis.items} />
        </>
      )}
    </main>
  )
}

import { useState } from 'react'

export default function SetupTagEditor({ setup, onSave }) {
  const [values, setValues] = useState(() => JSON.parse(setup.jsonData))
  const [newValue, setNewValue] = useState('')
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')

  function addValue() {
    const value = newValue.trim()
    setMessage('')
    if (!value) {
      setError('Enter a value before adding it.')
      return
    }
    if (values.includes(value)) {
      setError('That value already exists.')
      return
    }
    setValues([...values, value])
    setNewValue('')
    setError('')
  }

  function removeValue(value) {
    setValues(values.filter((entry) => entry !== value))
    setMessage('')
  }

  async function save() {
    setError('')
    setMessage('')
    try {
      await onSave(setup.uuid, { jsonData: JSON.stringify(values) })
      setMessage('Saved.')
    } catch (requestError) {
      setError(requestError.response?.data?.message ?? 'Unable to save setup values.')
    }
  }

  return (
    <section className="card mb-3">
      <div className="card-body">
        <h3 className="h5">{setup.type.replaceAll('_', ' ')}</h3>
        <div className="d-flex flex-wrap gap-2 mb-3">
          {values.map((value) => (
            <span className="badge text-bg-secondary d-inline-flex align-items-center gap-2" key={value}>
              {value}
              <button
                aria-label={`Remove ${value}`}
                className="btn-close btn-close-white"
                onClick={() => removeValue(value)}
                type="button"
              />
            </span>
          ))}
        </div>
        <div className="input-group mb-2">
          <input
            aria-label={`New ${setup.type.replaceAll('_', ' ').toLowerCase()} value`}
            className="form-control"
            onChange={(event) => setNewValue(event.target.value)}
            onKeyDown={(event) => {
              if (event.key === 'Enter') {
                event.preventDefault()
                addValue()
              }
            }}
            value={newValue}
          />
          <button className="btn btn-outline-secondary" onClick={addValue} type="button">
            Add
          </button>
        </div>
        {error && <p className="text-danger mb-2">{error}</p>}
        {message && <p className="text-success mb-2">{message}</p>}
        <button className="btn btn-primary" onClick={save} type="button">
          Save values
        </button>
      </div>
    </section>
  )
}


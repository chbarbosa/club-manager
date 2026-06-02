import { createContext, useContext, useEffect, useMemo, useState } from 'react'
import { getClub } from '../api/club.js'

const DEFAULT_CLUB = {
  name: 'Club Manager',
  description: '',
  colour1: '#2d2d2d',
  colour2: '#f0f0f0',
}

const ClubContext = createContext(DEFAULT_CLUB)

function applyTheme(club) {
  document.documentElement.style.setProperty('--club-primary', club.colour1)
  document.documentElement.style.setProperty('--club-secondary', club.colour2)
}

export function ClubProvider({ children }) {
  const [club, setClub] = useState(DEFAULT_CLUB)

  useEffect(() => {
    applyTheme(DEFAULT_CLUB)
    getClub()
      .then((loadedClub) => {
        setClub(loadedClub)
        applyTheme(loadedClub)
      })
      .catch(() => {
        setClub(DEFAULT_CLUB)
        applyTheme(DEFAULT_CLUB)
      })
  }, [])

  const value = useMemo(() => club, [club])

  return <ClubContext.Provider value={value}>{children}</ClubContext.Provider>
}

export function useClub() {
  return useContext(ClubContext)
}


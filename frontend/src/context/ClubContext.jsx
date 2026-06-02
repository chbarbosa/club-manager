import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react'
import { getClub } from '../api/club.js'

const DEFAULT_CLUB = {
  name: 'Club Manager',
  description: '',
  colour1: '#2d2d2d',
  colour2: '#f0f0f0',
}

const ClubContext = createContext({
  club: DEFAULT_CLUB,
  refreshClub: async () => DEFAULT_CLUB,
  updateClubContext: () => {},
})

function applyTheme(club) {
  document.documentElement.style.setProperty('--club-primary', club.colour1)
  document.documentElement.style.setProperty('--club-secondary', club.colour2)
}

export function ClubProvider({ children }) {
  const [club, setClub] = useState(DEFAULT_CLUB)

  const updateClubContext = useCallback((updatedClub) => {
    setClub(updatedClub)
    applyTheme(updatedClub)
  }, [])

  const refreshClub = useCallback(async () => {
    try {
      const loadedClub = await getClub()
      updateClubContext(loadedClub)
      return loadedClub
    } catch {
      updateClubContext(DEFAULT_CLUB)
      return DEFAULT_CLUB
    }
  }, [updateClubContext])

  useEffect(() => {
    applyTheme(DEFAULT_CLUB)
    refreshClub()
  }, [refreshClub])

  const value = useMemo(
    () => ({ club, refreshClub, updateClubContext }),
    [club, refreshClub, updateClubContext],
  )

  return <ClubContext.Provider value={value}>{children}</ClubContext.Provider>
}

export function useClub() {
  return useContext(ClubContext)
}

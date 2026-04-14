import React, { createContext, useContext, useState, useEffect } from 'react'

const ThemeContext = createContext(null)

const lightTheme = {
  token: {
    colorPrimary:       '#000000',
    colorLink:          '#000000',
    colorSuccess:       '#333333',
    colorWarning:       '#666666',
    colorError:         '#000000',
    colorInfo:          '#000000',
    colorBgContainer:   '#ffffff',
    colorBgLayout:      '#ffffff',
    colorText:          '#000000',
    colorTextSecondary: '#666666',
    colorBorder:        '#e0e0e0',
    borderRadius:       0,
    fontFamily:         "'Ubuntu Mono', 'Courier New', monospace",
    fontSize:           14,
    controlHeight:      36,
  },
  components: {
    Layout: { siderBg: '#000000', headerBg: '#ffffff' },
    Menu:   { darkItemBg: '#000000', darkItemSelectedBg: 'rgba(255,255,255,0.1)', darkItemColor: 'rgba(255,255,255,0.65)', darkItemHoverBg: 'rgba(255,255,255,0.06)' },
    Button: { borderRadius: 0, primaryShadow: 'none' },
    Card:   { borderRadius: 0, boxShadow: 'none' },
    Table:  { headerBg: '#f5f5f5', borderRadius: 0 },
    Modal:  { borderRadius: 0 },
    Input:  { borderRadius: 0, activeShadow: 'none' },
    Select: { borderRadius: 0 },
  }
}

const darkTheme = {
  token: {
    colorPrimary:       '#ffffff',
    colorLink:          '#ffffff',
    colorSuccess:       '#cccccc',
    colorWarning:       '#999999',
    colorError:         '#ff4d4f',
    colorInfo:          '#ffffff',
    colorBgContainer:   '#141414',
    colorBgLayout:      '#0a0a0a',
    colorText:          '#e0e0e0',
    colorTextSecondary: '#999999',
    colorBorder:        '#303030',
    borderRadius:       0,
    fontFamily:         "'Ubuntu Mono', 'Courier New', monospace",
    fontSize:           14,
    controlHeight:      36,
  },
  components: {
    Layout: { siderBg: '#000000', headerBg: '#141414' },
    Menu:   { darkItemBg: '#000000', darkItemSelectedBg: 'rgba(255,255,255,0.1)', darkItemColor: 'rgba(255,255,255,0.65)', darkItemHoverBg: 'rgba(255,255,255,0.06)' },
    Button: { borderRadius: 0, primaryShadow: 'none' },
    Card:   { borderRadius: 0, boxShadow: 'none' },
    Table:  { headerBg: '#1a1a1a', borderRadius: 0 },
    Modal:  { borderRadius: 0 },
    Input:  { borderRadius: 0, activeShadow: 'none' },
    Select: { borderRadius: 0 },
  }
}

export function ThemeProvider({ children }) {
  const [isDark, setIsDark] = useState(() => localStorage.getItem('theme') === 'dark')

  useEffect(() => {
    localStorage.setItem('theme', isDark ? 'dark' : 'light')
    document.body.style.background = isDark ? '#0a0a0a' : '#ffffff'
    document.body.style.color = isDark ? '#e0e0e0' : '#000000'
    if (isDark) {
      document.body.classList.add('dark-mode')
    } else {
      document.body.classList.remove('dark-mode')
    }
  }, [isDark])

  const toggle = () => setIsDark(prev => !prev)
  const theme = isDark ? darkTheme : lightTheme

  return (
    <ThemeContext.Provider value={{ isDark, toggle, theme }}>
      {children}
    </ThemeContext.Provider>
  )
}

export const useTheme = () => useContext(ThemeContext)

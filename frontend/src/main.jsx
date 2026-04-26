import React from 'react'
import ReactDOM from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom'
import { ConfigProvider } from 'antd'
import App from './App'
import { AuthProvider } from './context/AuthContext'
import { ThemeProvider, useTheme } from './context/ThemeContext'
import CustomCursor from './components/CustomCursor'
import './index.css'

class ErrorBoundary extends React.Component {
  constructor(props) { super(props); this.state = { error: null } }
  static getDerivedStateFromError(error) { return { error } }
  componentDidCatch(error, info) { console.error('App crashed:', error, info) }
  render() {
    if (this.state.error) {
      return (
        <div style={{ padding: 24, fontFamily: 'monospace', color: '#000', background: '#fff' }}>
          <h2>Something went wrong</h2>
          <pre style={{ color: '#c00' }}>{String(this.state.error?.stack || this.state.error)}</pre>
        </div>
      )
    }
    return this.props.children
  }
}

function ThemedApp() {
  const ctx = useTheme()
  return (
    <ConfigProvider theme={ctx?.theme}>
      <BrowserRouter>
        <AuthProvider>
          <CustomCursor />
          <App />
        </AuthProvider>
      </BrowserRouter>
    </ConfigProvider>
  )
}

ReactDOM.createRoot(document.getElementById('root')).render(
  <ErrorBoundary>
    <ThemeProvider>
      <ThemedApp />
    </ThemeProvider>
  </ErrorBoundary>
)

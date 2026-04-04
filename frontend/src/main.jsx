import React from 'react'
import ReactDOM from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom'
import { ConfigProvider } from 'antd'
import App from './App'
import { AuthProvider } from './context/AuthContext'
import CustomCursor from './components/CustomCursor'
import './index.css'

const theme = {
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

ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <ConfigProvider theme={theme}>
      <BrowserRouter>
        <AuthProvider>
          <CustomCursor />
          <App />
        </AuthProvider>
      </BrowserRouter>
    </ConfigProvider>
  </React.StrictMode>
)

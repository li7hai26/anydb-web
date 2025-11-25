import React from 'react'
import { Routes, Route, Navigate } from 'react-router-dom'
// import { Layout } from 'antd' // 暂时注释未使用的导入
import MainLayout from './components/layout/MainLayout'
import Home from './pages/Home'
import DatabaseManager from './pages/DatabaseManager'
import SQLEditor from './pages/SQLEditor'
import Monitor from './pages/Monitor'
import Settings from './pages/Settings'

const App: React.FC = () => {
  return (
    <MainLayout>
      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/databases" element={<DatabaseManager />} />
        <Route path="/sql-editor" element={<SQLEditor />} />
        <Route path="/monitor" element={<Monitor />} />
        <Route path="/settings" element={<Settings />} />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </MainLayout>
  )
}

export default App
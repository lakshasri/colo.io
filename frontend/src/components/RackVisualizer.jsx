import React from 'react'
import { Tooltip, Typography } from 'antd'

const { Text } = Typography

const SLOT_HEIGHT = 20 // px per U

function buildSlotMap(servers, totalU) {
  const map = new Array(totalU + 1).fill(null) // index 1..totalU
  servers.forEach(s => {
    for (let u = s.uPosition; u < s.uPosition + s.uSize; u++) {
      if (u >= 1 && u <= totalU) map[u] = s
    }
  })
  return map
}

export default function RackVisualizer({ rack, servers = [] }) {
  const totalU = rack?.totalUSpace ?? 42
  const slotMap = buildSlotMap(servers, totalU)

  const rendered = new Set()

  return (
    <div style={{ display: 'flex', gap: 8, alignItems: 'flex-start' }}>
      {/* U-number ruler */}
      <div style={{ display: 'flex', flexDirection: 'column', paddingTop: 0 }}>
        {Array.from({ length: totalU }, (_, i) => i + 1).map(u => (
          <div key={u} style={{
            height: SLOT_HEIGHT,
            width: 28,
            display: 'flex', alignItems: 'center', justifyContent: 'flex-end',
            paddingRight: 4,
            fontSize: 10,
            color: '#999'
          }}>
            {u}
          </div>
        ))}
      </div>

      {/* Rack body */}
      <div style={{
        width: 220,
        border: '2px solid #434343',
        borderRadius: 4,
        background: '#141414',
        overflow: 'hidden'
      }}>
        {Array.from({ length: totalU }, (_, i) => i + 1).map(u => {
          const server = slotMap[u]

          if (server && rendered.has(server.serverId)) return null
          if (server) rendered.add(server.serverId)

          const height = server ? server.uSize * SLOT_HEIGHT : SLOT_HEIGHT

          if (server) {
            return (
              <Tooltip key={u} title={
                <div>
                  <div><b>{server.hostname}</b></div>
                  <div>{server.cpuCores} vCPU · {server.ramGb}GB RAM · {server.diskTb}TB</div>
                  <div>Status: {server.status}</div>
                  <div>U{server.uPosition}–U{server.uPosition + server.uSize - 1}</div>
                </div>
              }>
                <div style={{
                  height,
                  background: server.status === 'OPERATIONAL' ? '#177ddc'
                            : server.status === 'FAULTY'      ? '#a61d24'
                            : '#d46b08',
                  borderBottom: '1px solid #141414',
                  display: 'flex', alignItems: 'center',
                  padding: '0 8px',
                  cursor: 'pointer',
                  overflow: 'hidden'
                }}>
                  <Text style={{ color: '#fff', fontSize: 11, whiteSpace: 'nowrap',
                                 overflow: 'hidden', textOverflow: 'ellipsis' }}>
                    {server.hostname}
                  </Text>
                </div>
              </Tooltip>
            )
          }

          return (
            <div key={u} style={{
              height: SLOT_HEIGHT,
              background: '#1f1f1f',
              borderBottom: '1px solid #2a2a2a',
              display: 'flex', alignItems: 'center',
              padding: '0 8px'
            }}>
              <Text style={{ color: '#3a3a3a', fontSize: 10 }}>Empty</Text>
            </div>
          )
        })}
      </div>
    </div>
  )
}

import { useEffect, useRef } from 'react'

export default function CustomCursor() {
  const dotRef = useRef(null)
  const ringRef = useRef(null)

  useEffect(() => {
    const dot = dotRef.current
    const ring = ringRef.current
    let mouseX = 0, mouseY = 0
    let ringX = 0, ringY = 0
    let frame

    const onMove = (e) => {
      mouseX = e.clientX
      mouseY = e.clientY
      dot.style.left = mouseX + 'px'
      dot.style.top  = mouseY + 'px'
    }

    const animate = () => {
      ringX += (mouseX - ringX) * 0.15
      ringY += (mouseY - ringY) * 0.15
      ring.style.left = ringX + 'px'
      ring.style.top  = ringY + 'px'
      frame = requestAnimationFrame(animate)
    }

    const onEnterInteractive = () => {
      ring.style.width  = '48px'
      ring.style.height = '48px'
      ring.style.opacity = '0.5'
    }

    const onLeaveInteractive = () => {
      ring.style.width  = '28px'
      ring.style.height = '28px'
      ring.style.opacity = '1'
    }

    document.addEventListener('mousemove', onMove)
    frame = requestAnimationFrame(animate)

    const interactiveEls = () => document.querySelectorAll('a, button, [role="button"], input, select, .ant-menu-item, .ant-btn')

    const attach = () => {
      interactiveEls().forEach(el => {
        el.addEventListener('mouseenter', onEnterInteractive)
        el.addEventListener('mouseleave', onLeaveInteractive)
      })
    }

    attach()
    const observer = new MutationObserver(attach)
    observer.observe(document.body, { childList: true, subtree: true })

    return () => {
      document.removeEventListener('mousemove', onMove)
      cancelAnimationFrame(frame)
      observer.disconnect()
    }
  }, [])

  return (
    <>
      <div id="cursor-dot" ref={dotRef} />
      <div id="cursor-ring" ref={ringRef} />
    </>
  )
}

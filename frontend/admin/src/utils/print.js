/**
 * 打印当前页面（隐藏侧边栏和导航，仅保留内容）
 */
export function printPage(title) {
  document.title = title || '财务报表'
  window.print()
}

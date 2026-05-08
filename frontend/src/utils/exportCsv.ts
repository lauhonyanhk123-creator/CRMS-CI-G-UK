export const exportCsv = (filename: string, rows: Record<string, any>[], columns: { label: string; key: string }[]) => {
  const header = columns.map(c => c.label).join(',')
  const body = rows.map(row =>
    columns.map(c => {
      const val = row[c.key] ?? ''
      return typeof val === 'string' && (val.includes(',') || val.includes('"'))
        ? `"${val.replace(/"/g, '""')}"`
        : val
    }).join(',')
  ).join('\n')
  const blob = new Blob([header + '\n' + body], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = filename
  a.click()
  URL.revokeObjectURL(url)
}
